/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.metadata.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.opensaml.util.Assert;
import org.opensaml.util.Strings;
import org.opensaml.util.collections.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.internet2.middleware.shibboleth.metadata.EntityIdInfo;
import edu.internet2.middleware.shibboleth.metadata.Metadata;
import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.TagInfo;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Pipeline;
import edu.internet2.middleware.shibboleth.metadata.pipeline.PipelineProcessingException;
import edu.internet2.middleware.shibboleth.metadata.pipeline.SimplePipeline;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Source;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Stage;
import edu.internet2.middleware.shibboleth.metadata.pipeline.StaticSource;

/** Controller that responds to Metadata Query requests.*/
@Controller
public class QueryController {

    /** Name of model attribute to which metadata from the query is bound. */
    public final static String METADATA_MODEL_ATTRIB = "metadata";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(QueryController.class);

    /** Length of time, in milliseconds, between metadata refreshes. */
    private final long mdUpdatePeriod;

    /** Background task that updates the indexed metadata. */
    private final MetadataRefreshTask refreshTask;

    /** Pipeline that produces metadata searched by this controller. */
    private final Pipeline<?> mdPipeline;

    /**
     * Cache of search terms to associated metadata element. Value may be null indicating the search term does match any
     * metadata element.
     */
    private HashMap<String, List<Metadata<?>>> termIndex;

    /** The pipeline stages through which query results are sent prior to being serialized and returned. */
    private final List<Stage<?>> resultPostProcessStages;

    /**
     * Constructor.
     * 
     * @param pipeline the pipeline used to produce the metadata that is searched
     * @param updateInterval length of time, in minutes, between metadata refresh updates
     * @param postProcessStages set of stages used to process a query result to prepare it for being returned to the
     *            requester
     */
    public QueryController(Pipeline<?> pipeline, int updateInterval, List<Stage<?>> postProcessStages) {
        this(pipeline, updateInterval, new Timer(true), postProcessStages);
    }

    /**
     * Constructor.
     * 
     * @param pipeline the pipeline used to produce the metadata that is searched
     * @param updateInterval length of time, in minutes, between metadata refresh updates
     * @param backgroundTaskTimer the {@link Timer} used to run the metadata update refresh task
     * @param postProcessStages set of stages used to process a query result to prepare it for being returned to the
     *            requester
     */
    public QueryController(Pipeline<?> pipeline, int updateInterval, Timer backgroundTaskTimer,
            List<Stage<?>> postProcessStages) {
        Assert.isNotNull(pipeline, "Metadata pipeline may not be null");
        mdPipeline = pipeline;

        Assert.isGreaterThan(0, updateInterval, "Update interval must be a positive number");
        Assert.isNotNull(backgroundTaskTimer, "Metadata refresh timer may not be null");
        mdUpdatePeriod = updateInterval * 60 * 1000;
        refreshTask = new MetadataRefreshTask();
        backgroundTaskTimer.schedule(refreshTask, 0, mdUpdatePeriod);

        Assert.isNotNull(postProcessStages, "Result post-processing stages may not be null");
        resultPostProcessStages = new ArrayList<Stage<?>>(postProcessStages);

        termIndex = new HashMap<String, List<Metadata<?>>>();
    }

    /**
     * Searches a collection of metadata for all the elements that meet a set of search terms given on the URL.
     * 
     * @param request the HTTP request
     * 
     * @return the metadata elements that meet the search terms given in the request
     * 
     * @throws PipelineProcessingException thrown if there is a problem running a query result set through a
     *             post-processing pipeline
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/entities", method = RequestMethod.GET)
    @ModelAttribute(METADATA_MODEL_ATTRIB)
    public MetadataCollection<Metadata<?>> queryMetadata(HttpServletRequest request) throws PipelineProcessingException {
        List<String> searchTerms = getSearchTerms(request);
        MetadataCollection<Metadata<?>> results = getMetadataElements(searchTerms);

        if (results != null && !results.isEmpty()) {
            Source<Metadata<?>> resultSource = new StaticSource<Metadata<?>>("postProcessSource", results);
            SimplePipeline<Metadata<?>> resultPostProcess = new SimplePipeline("postprocess", resultSource,
                    resultPostProcessStages);
            results = resultPostProcess.execute();
        }

        return results;
    }

    /**
     * Handles the exception that occurs when the post processing pipeline is working on a query result set.
     * 
     * @param e the thrown exception
     * @param request current HTTP request
     * @param response current HTTP response
     */
    @ExceptionHandler(PipelineProcessingException.class)
    public void handlePipelineProcessingException(PipelineProcessingException pe, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            log.debug("Error post-prcossing result set", pe);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "error post-processing query results");
        } catch (IOException e) {
            log.error("Unble to handle post-processing exception", e);
        }
    }

    /**
     * Gets the search terms given on the request URL.
     * 
     * @param request the current request
     * 
     * @return the search terms given on the request URL
     */
    protected List<String> getSearchTerms(HttpServletRequest request) {
        String requestPath = request.getPathInfo();
        log.debug("Extracting search terms from path '{}'", requestPath);

        int operationNameIndex = requestPath.indexOf("entities");
        String concatSearchTerms = requestPath.substring(operationNameIndex + 9);

        String[] terms = concatSearchTerms.split("\\+");

        String trimmedTerm;
        ArrayList<String> searchTerms = new ArrayList<String>();
        for (String term : terms) {
            trimmedTerm = Strings.trimOrNull(term);
            if (trimmedTerm != null) {
                searchTerms.add(trimmedTerm);
            }
        }
        return searchTerms;
    }

    /**
     * Gets all of the metadata elements that labeled with all of the given search terms.
     * 
     * @param searchTerms search terms
     * 
     * @return metadata elements labeled with all the given search terms
     */
    protected MetadataCollection<Metadata<?>> getMetadataElements(List<String> searchTerms) {
        log.debug("Searching for metaata elements matching the search terms: {}", searchTerms);

        HashMap<String, List<Metadata<?>>> index = termIndex;

        SimpleMetadataCollection<Metadata<?>> mdc = new SimpleMetadataCollection<Metadata<?>>();

        String firstTerm = searchTerms.get(0);
        if (!index.containsKey(firstTerm)) {
            return mdc;
        }

        List<Metadata<?>> searchResults = new ArrayList<Metadata<?>>(index.get(firstTerm));
        searchTerms.remove(firstTerm);
        log.debug("Starting with result list for search term '{}' containing {} elements", firstTerm, searchResults
                .size());

        String searchTerm;
        Iterator<String> termItr = searchTerms.iterator();
        while (termItr.hasNext() && !searchResults.isEmpty()) {
            searchTerm = termItr.next();
            if (index.containsKey(searchTerm)) {
                searchResults.retainAll(index.get(searchTerm));
                log.debug("{} results left after removing results not indexed by search term '{}'", searchResults
                        .size(), searchTerm);
            } else {
                log.debug("No search results associated with term '{}', clearing search result list", searchTerm);
                searchResults.clear();
            }
        }

        log.debug("{} metadata elements match all search terms", searchResults.size());
        mdc.addAll(searchResults);
        return mdc;
    }

    /**
     * Checks if the given search term matches any {@link EntityIdInfo} for the given metadata element.
     * 
     * @param searchTerm search term to check
     * @param element element whose {@link EntityIdInfo} will be checked
     * 
     * @return true if the search term matched any {@link EntityIdInfo} for the given element, false otherwise
     */
    protected boolean isEntityId(String searchTerm, Metadata<?> element) {
        List<EntityIdInfo> idInfos = element.getMetadataInfo().get(EntityIdInfo.class);
        if (idInfos == null || idInfos.isEmpty()) {
            return false;
        }

        for (EntityIdInfo idInfo : idInfos) {
            if (idInfo.getEntityId().equals(searchTerm)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates an index that indexes all {@link Metadata} by {@link EntityIdInfo}.
     * 
     * @param collection collection of metadata to be indexed
     */
    protected void indexMetadata(MetadataCollection<?> collection) {
        log.debug("Indexing metadata collection containing {} elements", collection.size());
        HashMap<String, List<Metadata<?>>> newIndex = new HashMap<String, List<Metadata<?>>>();

        if (collection != null) {
            for (Metadata<?> metadata : collection) {
                indexByEntityIds(newIndex, metadata);
                indexByTag(newIndex, metadata);
            }
        }

        log.debug("New search term index contains entries for {} terms", newIndex.size());
        termIndex = newIndex;
    }

    /**
     * Adds an entry to the given index for each of given metadata's {@link EntityIdInfo}.
     * 
     * @param index index to be populated
     * @param metadata metadata to be added to the index
     */
    protected void indexByEntityIds(HashMap<String, List<Metadata<?>>> index, Metadata<?> metadata) {
        List<Metadata<?>> metadatasForTerm;

        List<EntityIdInfo> idInfos = metadata.getMetadataInfo().get(EntityIdInfo.class);
        if (idInfos == null || idInfos.isEmpty()) {
            return;
        }

        String entityId;
        for (EntityIdInfo idInfo : idInfos) {
            entityId = idInfo.getEntityId();
            metadatasForTerm = index.get(entityId);
            if (metadatasForTerm == null) {
                metadatasForTerm = new LazyList<Metadata<?>>();
                index.put(entityId, metadatasForTerm);
            }
            metadatasForTerm.add(metadata);
        }
    }

    /**
     * Adds an entry to the given index for each of given metadata's {@link TagInfo}.
     * 
     * @param index index to be populated
     * @param metadata metadata to be added to the index
     */
    protected void indexByTag(HashMap<String, List<Metadata<?>>> index, Metadata<?> metadata) {
        List<Metadata<?>> metadatasForTerm;

        List<TagInfo> tagInfos = metadata.getMetadataInfo().get(TagInfo.class);
        if (tagInfos == null || tagInfos.isEmpty()) {
            return;
        }

        String tag;
        for (TagInfo tagInfo : tagInfos) {
            tag = tagInfo.getTag();
            metadatasForTerm = index.get(tag);
            if (metadatasForTerm == null) {
                metadatasForTerm = new LazyList<Metadata<?>>();
                index.put(tag, metadatasForTerm);
            }
            metadatasForTerm.add(metadata);
        }
    }

    /** A task that updates the cached metadata. */
    private class MetadataRefreshTask extends TimerTask {

        /** {@inheritDoc} */
        public void run() {
            try {
                log.debug("Metadata refresh starting");
                indexMetadata(mdPipeline.execute());
                log.debug("Metadata refressh completed, next refresh will occur at approximately {}", new DateTime()
                        .plus(mdUpdatePeriod));
            } catch (PipelineProcessingException e) {
                // TODO
            }
        }
    }
}