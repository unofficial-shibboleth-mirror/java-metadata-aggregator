/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.ItemTag;
import net.shibboleth.metadata.pipeline.Pipeline;
import net.shibboleth.metadata.pipeline.PipelineProcessingException;
import net.shibboleth.metadata.pipeline.SimplePipeline;
import net.shibboleth.metadata.pipeline.Stage;
import net.shibboleth.utilities.java.support.collection.LazyList;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Controller that responds to Metadata Query requests.
 * 
 * @param <T> type of metadata this controller operates upon
 */
@Controller
public class QueryController<T> {

    /** Name of model attribute to which metadata from the query is bound. */
    public static final String METADATA_MODEL_ATTRIB = "metadata";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(QueryController.class);

    /** Length of time, in milliseconds, between metadata refreshes. */
    private final long mdUpdatePeriod;

    /** Background task that updates the indexed metadata. */
    private final MetadataRefreshTask refreshTask;

    /** Pipeline that produces metadata searched by this controller. */
    private final Pipeline<T> mdPipeline;

    /**
     * Cache of search terms to associated metadata element. Value may be null indicating the search term does match any
     * metadata element.
     */
    private HashMap<String, List<Item<T>>> termIndex;

    /** The pipeline stages through which query results are sent prior to being serialized and returned. */
    private final List<Stage<T>> resultPostProcessStages;

    /**
     * Constructor.
     * 
     * @param pipeline the pipeline used to produce the metadata that is searched
     * @param updateInterval length of time, in minutes, between metadata refresh updates
     * @param postProcessStages set of stages used to process a query result to prepare it for being returned to the
     *            requester
     */
    public QueryController(final Pipeline<T> pipeline, final int updateInterval,
            final List<Stage<T>> postProcessStages) {
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
    public QueryController(final Pipeline<T> pipeline, final int updateInterval, final Timer backgroundTaskTimer,
            final List<Stage<T>> postProcessStages) {
        Constraint.isNotNull(pipeline, "Metadata pipeline may not be null");
        mdPipeline = pipeline;

        Constraint.isGreaterThan(0, updateInterval, "Update interval must be a positive number");
        Constraint.isNotNull(backgroundTaskTimer, "Metadata refresh timer may not be null");
        mdUpdatePeriod = updateInterval * 60 * 1000;
        refreshTask = new MetadataRefreshTask();
        backgroundTaskTimer.schedule(refreshTask, 0, mdUpdatePeriod);

        Constraint.isNotNull(postProcessStages, "Result post-processing stages may not be null");
        resultPostProcessStages = new ArrayList<>(postProcessStages);

        termIndex = new HashMap<>();
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
    @RequestMapping(value = "/entities", method = RequestMethod.GET)
    @ModelAttribute(METADATA_MODEL_ATTRIB)
    public Collection<Item<T>> queryMetadata(final HttpServletRequest request) throws PipelineProcessingException {
        final List<String> searchTerms = getSearchTerms(request);
        final Collection<Item<T>> results = getMetadataElements(searchTerms);

        if (results != null && !results.isEmpty()) {
            final SimplePipeline<T> resultPostProcess = new SimplePipeline<>();
            resultPostProcess.setId("postProcess");
            resultPostProcess.setStages(resultPostProcessStages);
            try {
                resultPostProcess.initialize();
            } catch (ComponentInitializationException e) {
                throw new PipelineProcessingException("Unable to initialize post-processing pipeline", e);
            }

            resultPostProcess.execute(results);
        }

        return results;
    }

    /**
     * Handles the exception that occurs when the post processing pipeline is working on a query result set.
     * 
     * @param pe the thrown exception
     * @param request current HTTP request
     * @param response current HTTP response
     */
    @ExceptionHandler(PipelineProcessingException.class)
    public void handlePipelineProcessingException(final PipelineProcessingException pe,
            final HttpServletRequest request, final HttpServletResponse response) {
        try {
            log.debug("Error post-processing result set", pe);
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
    protected List<String> getSearchTerms(final HttpServletRequest request) {
        final String requestPath = request.getPathInfo();
        log.debug("Extracting search terms from path '{}'", requestPath);

        final int operationNameIndex = requestPath.indexOf("entities");
        final String concatSearchTerms = requestPath.substring(operationNameIndex + 9);

        final String[] terms = concatSearchTerms.split("\\+");

        String trimmedTerm;
        final ArrayList<String> searchTerms = new ArrayList<>();
        for (String term : terms) {
            trimmedTerm = StringSupport.trimOrNull(term);
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
    protected Collection<Item<T>> getMetadataElements(final List<String> searchTerms) {
        log.debug("Searching for metaata elements matching the search terms: {}", searchTerms);

        final HashMap<String, List<Item<T>>> index = termIndex;

        final Collection<Item<T>> mdc = new ArrayList<>();

        final String firstTerm = searchTerms.get(0);
        if (!index.containsKey(firstTerm)) {
            return mdc;
        }

        final List<Item<T>> searchResults = new ArrayList<>(index.get(firstTerm));
        searchTerms.remove(firstTerm);
        log.debug("Starting with result list for search term '{}' containing {} elements", firstTerm,
                searchResults.size());

        String searchTerm;
        final Iterator<String> termItr = searchTerms.iterator();
        while (termItr.hasNext() && !searchResults.isEmpty()) {
            searchTerm = termItr.next();
            if (index.containsKey(searchTerm)) {
                searchResults.retainAll(index.get(searchTerm));
                log.debug("{} results left after removing results not indexed by search term '{}'",
                        searchResults.size(), searchTerm);
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
     * Checks if the given search term matches any {@link ItemId} for the given metadata element.
     * 
     * @param searchTerm search term to check
     * @param element element whose {@link ItemId} will be checked
     * 
     * @return true if the search term matched any {@link ItemId} for the given element, false otherwise
     */
    protected boolean isEntityId(final String searchTerm, final Item<T> element) {
        final List<ItemId> idInfos = element.getItemMetadata().get(ItemId.class);
        if (idInfos == null || idInfos.isEmpty()) {
            return false;
        }

        for (ItemId idInfo : idInfos) {
            if (idInfo.getId().equals(searchTerm)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates an index that indexes all {@link Item}s by {@link ItemId}.
     * 
     * @param collection collection of metadata to be indexed
     */
    protected void indexMetadata(final Collection<Item<T>> collection) {
        log.debug("Indexing metadata collection containing {} elements", collection.size());
        final HashMap<String, List<Item<T>>> newIndex = new HashMap<>();

        if (collection != null) {
            for (Item<T> metadata : collection) {
                indexByEntityIds(newIndex, metadata);
                indexByTag(newIndex, metadata);
            }
        }

        log.debug("New search term index contains entries for {} terms", newIndex.size());
        termIndex = newIndex;
    }

    /**
     * Adds an entry to the given index for each of given metadata's {@link ItemId}.
     * 
     * @param index index to be populated
     * @param metadata metadata to be added to the index
     */
    protected void indexByEntityIds(final HashMap<String, List<Item<T>>> index, final Item<T> metadata) {
        List<Item<T>> itemsForTerm;

        final List<ItemId> idInfos = metadata.getItemMetadata().get(ItemId.class);
        if (idInfos == null || idInfos.isEmpty()) {
            return;
        }

        for (ItemId idInfo : idInfos) {
            final String entityId = idInfo.getId();
            itemsForTerm = index.get(entityId);
            if (itemsForTerm == null) {
                itemsForTerm = new LazyList<>();
                index.put(entityId, itemsForTerm);
            }
            itemsForTerm.add(metadata);
        }
    }

    /**
     * Adds an entry to the given index for each of given metadata's {@link ItemTag}.
     * 
     * @param index index to be populated
     * @param metadata metadata to be added to the index
     */
    protected void indexByTag(final HashMap<String, List<Item<T>>> index, final Item<T> metadata) {
        List<Item<T>> metadatasForTerm;

        final List<ItemTag> tagInfos = metadata.getItemMetadata().get(ItemTag.class);
        if (tagInfos == null || tagInfos.isEmpty()) {
            return;
        }

        for (ItemTag tagInfo : tagInfos) {
            final String tag = tagInfo.getTag();
            metadatasForTerm = index.get(tag);
            if (metadatasForTerm == null) {
                metadatasForTerm = new LazyList<>();
                index.put(tag, metadatasForTerm);
            }
            metadatasForTerm.add(metadata);
        }
    }

    /** A task that updates the cached metadata. */
    private final class MetadataRefreshTask extends TimerTask {

        /** {@inheritDoc} */
        @Override
        public void run() {
            try {
                log.debug("Metadata refresh starting");
                final Collection<Item<T>> items = new ArrayList<>();
                mdPipeline.execute(items);
                indexMetadata(items);
                log.debug("Metadata refresh completed, next refresh will occur at approximately {}",
                        new DateTime().plus(mdUpdatePeriod));
            } catch (PipelineProcessingException e) {
                // TODO
            }
        }
    }
}