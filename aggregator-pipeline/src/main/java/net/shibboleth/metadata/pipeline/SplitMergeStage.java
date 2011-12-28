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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.shibboleth.metadata.CollectionMergeStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemCollectionFactory;
import net.shibboleth.metadata.SimpleCollectionMergeStrategy;
import net.shibboleth.metadata.SimpleItemCollectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

/**
 * A stage which splits a given collection and passes selected items to one pipeline and non-selected items to another.
 * The selected and non-selected item pipelines are executed via the set {@link ExecutorService} and operate on
 * collections that contains clones of the (non-)selected items.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>selectionStrategy</code></li>
 * <li><code>selectedItemPipeline</code> or <code>nonselectedItemPipeline</code></li>
 * </ul>
 * 
 * <p>
 * If no {@link ExecutorService} is provided, one will be created using {@link Executors#newFixedThreadPool(int)} with 6
 * threads.
 * 
 * If no {@link ItemCollectionFactory} is given, then {@link SimpleItemCollectionFactory} is used.
 * 
 * If one or the other pipeline is null then no objects will be passed to it (obviously).
 * 
 * @param <ItemType> type of items upon which this stage operates
 */
public class SplitMergeStage<ItemType extends Item<?>> extends BaseStage<ItemType> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(SplitMergeStage.class);

    /** Service used to execute the selected and/or non-selected item pipelines. */
    private ExecutorService executorService;

    /** Factory used to create the Item collection that is then given to the pipelines. */
    private ItemCollectionFactory<ItemType> collectionFactory;

    /** Strategy used to split the given item collection. */
    private Predicate<ItemType> selectionStrategy;

    /** Pipeline that receives the selected items. */
    private Pipeline<ItemType> selectedItemPipeline;

    /** Pipeline that receives the non-selected items. */
    private Pipeline<ItemType> nonselectedItemPipeline;

    /** Strategy used to merge all the joined pipeline results in to the final Item collection. */
    private CollectionMergeStrategy mergeStrategy;

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
     * Gets the factory used to create the Item collection that is then given to the pipelines.
     * 
     * @return factory used to create the Item collection that is then given to the pipelines
     */
    public ItemCollectionFactory getCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Sets the factory used to create the Item collection that is then given to the pipelines.
     * 
     * @param factory factory used to create the Item collection that is then given to the pipelines
     */
    public synchronized void setCollectionFactory(ItemCollectionFactory<ItemType> factory) {
        if (isInitialized()) {
            return;
        }

        collectionFactory = factory;
    }

    /**
     * Gets the strategy used to split the given item collection.
     * 
     * @return strategy used to split the given item collection
     */
    public Predicate<ItemType> getSelectionStrategy() {
        return selectionStrategy;
    }

    /**
     * Sets the strategy used to split the given item collection.
     * 
     * @param strategy strategy used to split the given item collection, never null
     */
    public synchronized void setSelectionStrategy(Predicate<ItemType> strategy) {
        if (isInitialized()) {
            return;
        }

        selectionStrategy = strategy;
    }

    /**
     * Gets the pipeline that receives the selected items.
     * 
     * @return pipeline that receives the selected items
     */
    public Pipeline<ItemType> getSelectedItemPipeline() {
        return selectedItemPipeline;
    }

    /**
     * Sets the pipeline that receives the selected items.
     * 
     * @param pipeline pipeline that receives the selected items
     */
    public synchronized void setSelectedItemPipeline(Pipeline<ItemType> pipeline) {
        if (isInitialized()) {
            return;
        }

        selectedItemPipeline = pipeline;
    }

    /**
     * Gets the pipeline that receives the non-selected items.
     * 
     * @return pipeline that receives the non-selected items
     */
    public Pipeline<ItemType> getNonselectedItemPipeline() {
        return nonselectedItemPipeline;
    }

    /**
     * Sets the pipeline that receives the non-selected items.
     * 
     * @param pipeline pipeline that receives the non-selected items
     */
    public synchronized void setNonselectedItemPipeline(Pipeline<ItemType> pipeline) {
        if (isInitialized()) {
            return;
        }

        nonselectedItemPipeline = pipeline;
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
    protected void doExecute(Collection<ItemType> itemCollection) throws StageProcessingException {
        Collection<ItemType> selectedItems = collectionFactory.newCollection();
        Collection<ItemType> nonselectedItems = collectionFactory.newCollection();

        for (ItemType item : itemCollection) {
            if (item == null) {
                continue;
            }

            if (selectionStrategy.apply(item)) {
                selectedItems.add((ItemType) item.copy());
            } else {
                nonselectedItems.add((ItemType) item.copy());
            }
        }

        Future<Collection<? extends Item>> selectedItemFuture = executePipeline(selectedItemPipeline, selectedItems);
        Future<Collection<? extends Item>> nonselectedItemFuture =
                executePipeline(nonselectedItemPipeline, nonselectedItems);

        ArrayList<Collection<? extends Item>> pipelineResults = new ArrayList<Collection<? extends Item>>();
        try {
            if (selectedItemFuture != null) {
                pipelineResults.add(selectedItemFuture.get());
            } else {
                pipelineResults.add(selectedItems);
            }

            if (nonselectedItemFuture != null) {
                pipelineResults.add(nonselectedItemFuture.get());
            } else {
                pipelineResults.add(nonselectedItems);
            }
        } catch (ExecutionException e) {
            log.error("Pipeline threw an unexpected exception", e);
        } catch (InterruptedException e) {
            log.error("Execution service was interrupted", e);
        }

        itemCollection.clear();
        mergeStrategy.mergeCollection((Collection<Item<?>>) itemCollection,
                pipelineResults.toArray(new Collection[pipelineResults.size()]));
    }

    /**
     * Executes a pipeline.
     * 
     * @param pipeline the pipeline, may be null
     * @param items the collections of items
     * 
     * @return the token representing the background execution of the pipeline
     */
    protected Future<Collection<? extends Item>>
            executePipeline(Pipeline<ItemType> pipeline, Collection<ItemType> items) {
        if (pipeline == null) {
            return null;
        }

        PipelineCallable callable = new PipelineCallable(pipeline, items);
        return executorService.submit(callable);
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

        if (selectionStrategy == null) {
            throw new ComponentInitializationException(getId() + " selection strategy is null");
        }

        if (selectedItemPipeline == null && nonselectedItemPipeline == null) {
            throw new ComponentInitializationException(getId() + " selected and non-selected pipelines are null");
        }

        if (selectedItemPipeline != null && !selectedItemPipeline.isInitialized()) {
            log.debug("Selected item pipeline was not initialized, initializing it now.");
            selectedItemPipeline.initialize();
        }

        if (nonselectedItemPipeline != null && !nonselectedItemPipeline.isInitialized()) {
            log.debug("Non-selected item pipeline was not initialized, initializing it now.");
            nonselectedItemPipeline.initialize();
        }

        if (mergeStrategy == null) {
            log.debug("No collection merge strategy specified, using {}", SimpleCollectionMergeStrategy.class.getName());
            mergeStrategy = new SimpleCollectionMergeStrategy();
        }
    }
}