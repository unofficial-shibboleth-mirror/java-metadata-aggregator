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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import org.opensaml.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.internet2.middleware.shibboleth.metadata.core.EntityIdInfo;
import edu.internet2.middleware.shibboleth.metadata.core.Metadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.Pipeline;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.PipelineProcessingException;

/**
 *
 */
@Controller
public class QueryController {

    private ReadWriteLock metadataLock;

    private Timer mdUpdateTimer;
    
    private long mdUpdateInterval;

    private Pipeline<Metadata<?>> mdPipeline;

    private MetadataCollection<Metadata<?>> searchableMetadata;

    private HashMap<String, Metadata<?>> searchTermCache;

    private HashSet<String> searchTermNCache;

    /**
     * Constructor.
     * 
     * @param pipeline the pipeline used to produce the metadata that is searched
     */
    @Autowired
    public QueryController(Pipeline<Metadata<?>> pipeline, int updateInterval) {
        this(pipeline, updateInterval, new Timer(true));
    }

    /**
     * Constructor.
     * 
     * @param pipeline the pipeline used to produce the metadata that is searched
     * @param backgroundTaskTimer the {@link Timer} used to run the metadata update refresh task
     */
    @Autowired
    public QueryController(Pipeline<Metadata<?>> pipeline, int updateInterval, Timer backgroundTaskTimer) {
        Assert.isNotNull(pipeline, "Metadata pipeline may not be null");
        mdPipeline = pipeline;

        Assert.isGreaterThan(0, updateInterval, "Update interval must be a positive number");
        mdUpdateInterval = updateInterval * 60 * 1000;
        
        Assert.isNotNull(backgroundTaskTimer, "Metadata refresh timer may not be null");
        mdUpdateTimer = backgroundTaskTimer;

        metadataLock = new ReentrantReadWriteLock();
        searchTermCache = new HashMap<String, Metadata<?>>();
        searchTermNCache = new HashSet<String>();
    }

    /**
     * Searches a collection of metadata for all the elements that meet a set of search terms given on the URL.
     * 
     * @param request the HTTP request
     * 
     * @return the metadata elements that meet the search terms given in the request
     */
    @RequestMapping(value = "/entities", method = RequestMethod.GET)
    public MetadataCollection<Metadata<?>> queryMetadata(HttpServletRequest request) {
        String[] searchTerms = getSearchTerms(request);
        return getMetadataElements(searchTerms);
    }

    /**
     * Gets the search terms given on the request URL.
     * 
     * @param request the current request
     * 
     * @return the search terms given on the request URL
     */
    protected String[] getSearchTerms(HttpServletRequest request) {
        String requestPath = request.getPathInfo();
        int operationNameIndex = requestPath.indexOf("entities");

        String searchTerms = requestPath.substring(operationNameIndex + 7);
        return searchTerms.split("+");
    }

    /**
     * Gets all of the metadata elements that labeled with all of the given search terms.
     * 
     * @param searchTerms search terms
     * 
     * @return metadata elements labelled with all the given search terms
     */
    protected MetadataCollection<Metadata<?>> getMetadataElements(String[] searchTerms) {
        Lock readLock = metadataLock.readLock();
        readLock.lock();
        MetadataCollection<Metadata<?>> metadata = searchableMetadata;
        HashMap<String, Metadata<?>> cache = searchTermCache;
        HashSet<String> ncache = searchTermNCache;
        readLock.unlock();

        SimpleMetadataCollection<Metadata<?>> searchResults = new SimpleMetadataCollection<Metadata<?>>();

        if (metadata != null) {
            elements: for (Metadata<?> element : metadata) {
                for (String searchTerm : searchTerms) {
                    if (!isEntityId(searchTerm, element)) {
                        continue elements;
                    }
                }
                // if we looped through all the terms and nothing kicked us out
                // of the current loop, then we matched all the search terms so
                // the current element is a positive match
                searchResults.add(element);
            }
        }

        return searchResults;
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

    /** A task that updates the cached metadata. */
    private class MetadataRefreshTask extends TimerTask {

        /** {@inheritDoc} */
        public void run() {
            try {
                MetadataCollection<Metadata<?>> newSearchableMetadata = mdPipeline.execute();

                Lock writeLock = metadataLock.writeLock();
                writeLock.lock();
                searchableMetadata = newSearchableMetadata;
                searchTermCache = new HashMap<String, Metadata<?>>();
                searchTermNCache = new HashSet<String>();
                writeLock.unlock();
            } catch (PipelineProcessingException e) {
                // TODO
            }
        }
    }
}