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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.metadata.CollectionMergeStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.SimpleCollectionMergeStrategy;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

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
public class SplitMergeStage<T> extends AbstractStage<T> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(SplitMergeStage.class);

    /** Service used to execute the selected and/or non-selected item pipelines. */
    @Nonnull @GuardedBy("this")
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /** Factory used to create the Item collection that is then given to the pipelines. */
    @Nonnull @GuardedBy("this")
    private Supplier<List<Item<T>>> collectionFactory = new SimpleItemCollectionFactory<>();

    /** Strategy used to split the given item collection. */
    @Nonnull @GuardedBy("this")
    private Predicate<Item<T>> selectionStrategy = x -> false;

    /** Pipeline that receives the selected items. */
    @Nullable @GuardedBy("this")
    private Pipeline<T> selectedItemPipeline;

    /** Pipeline that receives the non-selected items. */
    @Nullable @GuardedBy("this")
    private Pipeline<T> nonselectedItemPipeline;

    /** Strategy used to merge all the joined pipeline results in to the final Item collection. */
    @Nonnull @GuardedBy("this")
    private CollectionMergeStrategy mergeStrategy = new SimpleCollectionMergeStrategy();

    /**
     * Gets the executor service used to run the selected and non-selected item pipelines.
     * 
     * @return executor service used to run the selected and non-selected item pipelines
     */
    @Nonnull public final synchronized ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Sets the executor service used to run the selected and non-selected item pipelines.
     * 
     * @param service executor service used to run the selected and non-selected item pipelines
     */
    public synchronized void setExecutorService(@Nonnull final ExecutorService service) {
        checkSetterPreconditions();
        executorService = Constraint.isNotNull(service, "ExecutorService can not be null");
    }

    /**
     * Gets the factory used to create the Item collection that is then given to the pipelines.
     * 
     * @return factory used to create the Item collection that is then given to the pipelines
     */
    @Nonnull public final synchronized Supplier<List<Item<T>>> getCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Sets the factory used to create the Item collection that is then given to the pipelines.
     * 
     * @param factory factory used to create the Item collection that is then given to the pipelines
     */
    public synchronized void setCollectionFactory(@Nonnull final Supplier<List<Item<T>>> factory) {
        checkSetterPreconditions();
        collectionFactory = Constraint.isNotNull(factory, "Collection factory can not be null");
    }

    /**
     * Gets the strategy used to split the given item collection.
     * 
     * @return strategy used to split the given item collection
     */
    @Nonnull public final synchronized Predicate<Item<T>> getSelectionStrategy() {
        return selectionStrategy;
    }

    /**
     * Sets the strategy used to split the given item collection.
     * 
     * @param strategy strategy used to split the given item collection, never null
     */
    public synchronized void setSelectionStrategy(@Nonnull final Predicate<Item<T>> strategy) {
        checkSetterPreconditions();
        selectionStrategy = Constraint.isNotNull(strategy, "Item selection strategy can not be null");
    }

    /**
     * Gets the pipeline that receives the selected items.
     * 
     * @return pipeline that receives the selected items
     */
    @Nullable public final synchronized Pipeline<T> getSelectedItemPipeline() {
        return selectedItemPipeline;
    }

    /**
     * Sets the pipeline that receives the selected items.
     * 
     * @param pipeline pipeline that receives the selected items
     */
    public synchronized void setSelectedItemPipeline(@Nullable final Pipeline<T> pipeline) {
        checkSetterPreconditions();
        selectedItemPipeline = pipeline;
    }

    /**
     * Gets the pipeline that receives the non-selected items.
     * 
     * @return pipeline that receives the non-selected items
     */
    @Nullable public final synchronized Pipeline<T> getNonselectedItemPipeline() {
        return nonselectedItemPipeline;
    }

    /**
     * Sets the pipeline that receives the non-selected items.
     * 
     * @param pipeline pipeline that receives the non-selected items
     */
    public synchronized void setNonselectedItemPipeline(@Nullable final Pipeline<T> pipeline) {
        checkSetterPreconditions();
        nonselectedItemPipeline = pipeline;
    }

    /**
     * Gets the strategy used to merge all the joined pipeline results in to the final Item collection.
     * 
     * @return strategy used to merge all the joined pipeline results in to the final Item collection, never null
     */
    @Nonnull public final synchronized CollectionMergeStrategy getCollectionMergeStrategy() {
        return mergeStrategy;
    }

    /**
     * Sets the strategy used to merge all the joined pipeline results in to the final Item collection.
     * 
     * @param strategy strategy used to merge all the joined pipeline results in to the final Item collection, never
     *            null
     */
    public synchronized void setCollectionMergeStrategy(@Nonnull final CollectionMergeStrategy strategy) {
        checkSetterPreconditions();
        mergeStrategy = Constraint.isNotNull(strategy, "Collection merge strategy can not be null");
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        final List<Item<T>> selectedItems = getCollectionFactory().get();
        final List<Item<T>> nonselectedItems = getCollectionFactory().get();

        final var strategy = getSelectionStrategy();
        for (final Item<T> item : items) {
            if (strategy.test(item)) {
                selectedItems.add(item);
            } else {
                nonselectedItems.add(item);
            }
        }

        final Future<List<Item<T>>> selectedItemFuture =
                executePipeline(getSelectedItemPipeline(), selectedItems);
        final Future<List<Item<T>>> nonselectedItemFuture =
                executePipeline(getNonselectedItemPipeline(), nonselectedItems);

        final List<List<Item<T>>> pipelineResults = new ArrayList<>();
        
        // resolve results from the pipelines
        pipelineResults.add(FutureSupport.futureItems(selectedItemFuture));
        pipelineResults.add(FutureSupport.futureItems(nonselectedItemFuture));

        items.clear();
        getCollectionMergeStrategy().merge(items, pipelineResults);
    }

    /**
     * Executes a pipeline.
     * 
     * @param pipeline the pipeline, may be null
     * @param items the collections of items
     * 
     * @return the token representing the background execution of the pipeline
     */
    @Nonnull protected Future<List<Item<T>>> executePipeline(final Pipeline<T> pipeline,
            final List<Item<T>> items) {

        /*
         * If no pipeline has been specified, just return the collection unchanged via
         * a {@link Future}.
         */
        if (pipeline == null) {
            return FutureSupport.futureNow(items);
        }

        final PipelineCallable<T> callable = new PipelineCallable<>(pipeline, items);
        return getExecutorService().submit(callable);
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
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
