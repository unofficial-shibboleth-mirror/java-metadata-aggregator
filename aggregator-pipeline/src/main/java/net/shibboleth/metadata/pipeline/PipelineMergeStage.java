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

package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.CollectionMergeStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemCollectionFactory;
import net.shibboleth.metadata.SimpleCollectionMergeStrategy;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.collection.LazyList;
import net.shibboleth.utilities.java.support.logic.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link Stage} allows the merging of multiple pipeline outputs into a single {@link Collection} that can then be
 * used as the input source for another pipeline.
 * 
 * This source works producing a {@link Collection} by means of the registered
 * {@link net.shibboleth.metadata.ItemCollectionFactory} . Then each of its registered {@link Pipeline} is invoked in
 * turn (no ordering is guaranteed and pipelines may execute concurrently). After each pipeline has completed the
 * results are merged in to the Item collection given to this stage by means of the an {@link CollectionMergeStrategy}.
 */
@ThreadSafe
public class PipelineMergeStage extends BaseStage<Item<?>> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(PipelineMergeStage.class);

    /** Service used to execute the pipelines whose results will be merged. */
    private ExecutorService executorService;

    /**
     * The factory used to create the item returned by this source. Default implementation is
     * {@link SimpleItemCollectionFactory}.
     */
    private ItemCollectionFactory collectionFactory;

    /** Strategy used to merge all the joined pipeline results in to the final Item collection. */
    private CollectionMergeStrategy mergeStrategy;

    /** Pipelines whose results become the output of this source. */
    private List<Pipeline<Item<?>>> mergedPipelines = new LazyList<Pipeline<Item<?>>>();

    /**
     * Gets the executor service used to run the selected and non-selected item pipelines.
     * 
     * @return executor service used to run the selected and non-selected item pipelines
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Sets the executor service used to run the selected and non-selected item pipelines.
     * 
     * @param service executor service used to run the selected and non-selected item pipelines
     */
    public synchronized void setExecutorService(ExecutorService service) {
        if (isInitialized()) {
            return;
        }

        executorService = service;
    }

    /**
     * Gets the unmodifiable set of pipelines used by this stage.
     * 
     * @return unmodifiable set of pipelines used by this stage
     */
    public List<Pipeline<Item<?>>> getMergedPipelines() {
        return mergedPipelines;
    }

    /**
     * Sets the pipelines joined by this stage.
     * 
     * @param pipelines pipelines joined by this stage
     */
    public synchronized void setMergedPipelines(final List<Pipeline<Item<?>>> pipelines) {
        if (isInitialized()) {
            return;
        }
        mergedPipelines =
                Collections
                        .unmodifiableList(CollectionSupport.nonNullAdd(pipelines, new LazyList<Pipeline<Item<?>>>()));
    }

    /**
     * Gets the factory used to create the {@link Item} collection produced by this source.
     * 
     * @return factory used to create the {@link Item} collection produced by this source
     */
    public ItemCollectionFactory getCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Sets the factory used to create the {@link Item} collection produced by this source.
     * 
     * @param factory factory used to create the {@link Item} collection produced by this source
     */
    public synchronized void setCollectionFactory(final ItemCollectionFactory factory) {
        if (isInitialized()) {
            return;
        }
        Assert.isNotNull(factory, "Collection factory may not be null");
        collectionFactory = factory;
    }

    /**
     * Gets the strategy used to merge all the joined pipeline results in to the final Item collection.
     * 
     * @return strategy used to merge all the joined pipeline results in to the final Item collection, never null
     */
    public CollectionMergeStrategy getCollectionMergeStrategy() {
        return mergeStrategy;
    }

    /**
     * Sets the strategy used to merge all the joined pipeline results in to the final Item collection.
     * 
     * @param strategy strategy used to merge all the joined pipeline results in to the final Item collection, never
     *            null
     */
    public synchronized void setCollectionMergeStrategy(final CollectionMergeStrategy strategy) {
        if (isInitialized()) {
            return;
        }

        mergeStrategy = strategy;
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<Item<?>> itemCollection) throws StageProcessingException {
        ArrayList<Future<Collection<? extends Item>>> pipelineResultFutures =
                new ArrayList<Future<Collection<? extends Item>>>();

        for (Pipeline<Item<?>> pipeline : mergedPipelines) {
            pipelineResultFutures.add(executorService.submit(new PipelineCallable(pipeline, collectionFactory
                    .newCollection())));
        }

        ArrayList<Collection<? extends Item>> pipelineResults = new ArrayList<Collection<? extends Item>>();
        for (Future<Collection<? extends Item>> future : pipelineResultFutures) {
            try {
                pipelineResults.add(future.get());
            } catch (ExecutionException e) {
                throw new StageProcessingException(e);
            } catch (InterruptedException e) {
                throw new StageProcessingException(e);
            }
        }

        mergeStrategy.mergeCollection(itemCollection, pipelineResults.toArray(new Collection[pipelineResults.size()]));
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (executorService == null) {
            log.debug("No ExecutorService specified, creating a fixed thread pool service with 6 threads");
            executorService = Executors.newFixedThreadPool(6);
        }

        if (collectionFactory == null) {
            log.debug("No collection factory specified, using {}", SimpleItemCollectionFactory.class.getName());
            collectionFactory = new SimpleItemCollectionFactory();
        }

        if (mergeStrategy == null) {
            log.debug("No collection merge strategy specified, using {}",
                    SimpleCollectionMergeStrategy.class.getName());
            mergeStrategy = new SimpleCollectionMergeStrategy();
        }

        for (Pipeline<Item<?>> pipeline : mergedPipelines) {
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }
}