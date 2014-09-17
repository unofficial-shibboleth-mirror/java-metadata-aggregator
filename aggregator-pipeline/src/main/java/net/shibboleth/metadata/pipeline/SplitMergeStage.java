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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.CollectionMergeStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.SimpleCollectionMergeStrategy;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;

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
 * If no {@link #executorService} is provided, one will be created using {@link Executors#newFixedThreadPool(int)} with
 * 6 threads.
 * 
 * If no {@link #collectionFactory} is given, then {@link SimpleItemCollectionFactory} is used.
 * 
 * If one or the other pipeline is null then no objects will be passed to it (obviously).
 * 
 * @param <T> type of items upon which this stage operates
 */
@ThreadSafe
public class SplitMergeStage<T> extends BaseStage<T> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(SplitMergeStage.class);

    /** Service used to execute the selected and/or non-selected item pipelines. */
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /** Factory used to create the Item collection that is then given to the pipelines. */
    private Supplier<Collection<Item<T>>> collectionFactory = new SimpleItemCollectionFactory<>();

    /** Strategy used to split the given item collection. */
    private Predicate<Item<T>> selectionStrategy = Predicates.alwaysFalse();

    /** Pipeline that receives the selected items. */
    private Pipeline<T> selectedItemPipeline;

    /** Pipeline that receives the non-selected items. */
    private Pipeline<T> nonselectedItemPipeline;

    /** Strategy used to merge all the joined pipeline results in to the final Item collection. */
    private CollectionMergeStrategy mergeStrategy = new SimpleCollectionMergeStrategy();

    /**
     * Gets the executor service used to run the selected and non-selected item pipelines.
     * 
     * @return executor service used to run the selected and non-selected item pipelines
     */
    @Nonnull public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Sets the executor service used to run the selected and non-selected item pipelines.
     * 
     * @param service executor service used to run the selected and non-selected item pipelines
     */
    public synchronized void setExecutorService(@Nonnull final ExecutorService service) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        executorService = Constraint.isNotNull(service, "ExecutorService can not be null");
    }

    /**
     * Gets the factory used to create the Item collection that is then given to the pipelines.
     * 
     * @return factory used to create the Item collection that is then given to the pipelines
     */
    @Nonnull public Supplier<Collection<Item<T>>> getCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Sets the factory used to create the Item collection that is then given to the pipelines.
     * 
     * @param factory factory used to create the Item collection that is then given to the pipelines
     */
    public synchronized void setCollectionFactory(@Nonnull final Supplier<Collection<Item<T>>> factory) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        collectionFactory = Constraint.isNotNull(factory, "Collection factory can not be null");
    }

    /**
     * Gets the strategy used to split the given item collection.
     * 
     * @return strategy used to split the given item collection
     */
    @Nonnull public Predicate<Item<T>> getSelectionStrategy() {
        return selectionStrategy;
    }

    /**
     * Sets the strategy used to split the given item collection.
     * 
     * @param strategy strategy used to split the given item collection, never null
     */
    public synchronized void setSelectionStrategy(@Nonnull final Predicate<Item<T>> strategy) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        selectionStrategy = Constraint.isNotNull(strategy, "Item selection strategy can not be null");
    }

    /**
     * Gets the pipeline that receives the selected items.
     * 
     * @return pipeline that receives the selected items
     */
    @Nullable public Pipeline<T> getSelectedItemPipeline() {
        return selectedItemPipeline;
    }

    /**
     * Sets the pipeline that receives the selected items.
     * 
     * @param pipeline pipeline that receives the selected items
     */
    public synchronized void setSelectedItemPipeline(@Nullable final Pipeline<T> pipeline) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        selectedItemPipeline = pipeline;
    }

    /**
     * Gets the pipeline that receives the non-selected items.
     * 
     * @return pipeline that receives the non-selected items
     */
    @Nullable public Pipeline<T> getNonselectedItemPipeline() {
        return nonselectedItemPipeline;
    }

    /**
     * Sets the pipeline that receives the non-selected items.
     * 
     * @param pipeline pipeline that receives the non-selected items
     */
    public synchronized void setNonselectedItemPipeline(@Nullable final Pipeline<T> pipeline) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        nonselectedItemPipeline = pipeline;
    }

    /**
     * Gets the strategy used to merge all the joined pipeline results in to the final Item collection.
     * 
     * @return strategy used to merge all the joined pipeline results in to the final Item collection, never null
     */
    @Nonnull public CollectionMergeStrategy getCollectionMergeStrategy() {
        return mergeStrategy;
    }

    /**
     * Sets the strategy used to merge all the joined pipeline results in to the final Item collection.
     * 
     * @param strategy strategy used to merge all the joined pipeline results in to the final Item collection, never
     *            null
     */
    public synchronized void setCollectionMergeStrategy(@Nonnull final CollectionMergeStrategy strategy) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        mergeStrategy = Constraint.isNotNull(strategy, "Collection merge strategy can not be null");
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection)
            throws StageProcessingException {
        final Collection<Item<T>> selectedItems = collectionFactory.get();
        final Collection<Item<T>> nonselectedItems = collectionFactory.get();

        for (Item<T> item : itemCollection) {
            if (item == null) {
                continue;
            }

            if (selectionStrategy.apply(item)) {
                selectedItems.add(item);
            } else {
                nonselectedItems.add(item);
            }
        }

        final Future<Collection<Item<T>>> selectedItemFuture = executePipeline(selectedItemPipeline, selectedItems);
        final Future<Collection<Item<T>>> nonselectedItemFuture =
                executePipeline(nonselectedItemPipeline, nonselectedItems);

        final ArrayList<Collection<Item<T>>> pipelineResults = new ArrayList<>();
        
        // resolve results from the pipelines
        pipelineResults.add(FutureSupport.futureItems(selectedItemFuture));
        pipelineResults.add(FutureSupport.futureItems(nonselectedItemFuture));

        itemCollection.clear();
        mergeStrategy.mergeCollection(itemCollection, pipelineResults);
    }

    /**
     * Executes a pipeline.
     * 
     * @param pipeline the pipeline, may be null
     * @param items the collections of items
     * 
     * @return the token representing the background execution of the pipeline
     */
    @Nonnull protected Future<Collection<Item<T>>> executePipeline(Pipeline<T> pipeline,
            final Collection<Item<T>> items) {

        /*
         * If no pipeline has been specified, just return the collection unchanged via
         * a {@link Future}.
         */
        if (pipeline == null) {
            return new FutureSupport.FutureNow<>(items);
        }

        final PipelineCallable<T> callable = new PipelineCallable<>(pipeline, items);
        return executorService.submit(callable);
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        executorService = null;
        collectionFactory = null;
        selectionStrategy = null;
        selectedItemPipeline = null;
        nonselectedItemPipeline = null;
        mergeStrategy = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

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
    }
}