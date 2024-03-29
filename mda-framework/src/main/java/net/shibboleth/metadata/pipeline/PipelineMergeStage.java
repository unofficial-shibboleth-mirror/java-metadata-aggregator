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
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.CollectionMergeStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.SimpleCollectionMergeStrategy;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * This {@link Stage} allows the merging of multiple pipeline outputs into a single {@link List} that can then be
 * used as the input source for another pipeline.
 * 
 * This source works producing a {@link List} by means of the registered {@link Supplier} . Then each of its
 * registered {@link Pipeline} is invoked in turn (no ordering is guaranteed and pipelines may execute concurrently).
 * After each pipeline has completed the results are merged in to the Item collection given to this stage by means of
 * the an {@link CollectionMergeStrategy}.
 * 
 * @param <T> the type of items processed by the stage
 */
@ThreadSafe
public class PipelineMergeStage<T> extends AbstractStage<T> {

    /** Service used to execute the pipelines whose results will be merged. */
    @Nonnull @GuardedBy("this")
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * The factory used to create the item returned by this source. Default implementation is
     * {@link SimpleItemCollectionFactory}.
     */
    @Nonnull @GuardedBy("this")
    private Supplier<List<Item<T>>> collectionFactory = new SimpleItemCollectionFactory<>();

    /** Strategy used to merge all the joined pipeline results in to the final Item collection. */
    @Nonnull @GuardedBy("this")
    private CollectionMergeStrategy mergeStrategy = new SimpleCollectionMergeStrategy();

    /** Pipelines whose results become the output of this source. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Pipeline<T>> mergedPipelines = List.of();

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
     * Gets the unmodifiable set of pipelines used by this stage.
     * 
     * @return unmodifiable set of pipelines used by this stage
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized List<Pipeline<T>> getMergedPipelines() {
        return mergedPipelines;
    }

    /**
     * Sets the pipelines joined by this stage.
     * 
     * @param pipelines pipelines joined by this stage
     */
    public synchronized void setMergedPipelines(
            @Nonnull @NonnullElements @Unmodifiable final List<? extends Pipeline<T>> pipelines) {
        checkSetterPreconditions();
        mergedPipelines = List.copyOf(pipelines);
    }

    /**
     * Gets the factory used to create the {@link Item} collection produced by this source.
     * 
     * @return factory used to create the {@link Item} collection produced by this source
     */
    @Nonnull public final synchronized Supplier<List<Item<T>>> getCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Sets the factory used to create the {@link Item} collection produced by this source.
     * 
     * @param factory factory used to create the {@link Item} collection produced by this source
     */
    public synchronized void setCollectionFactory(@Nonnull final Supplier<List<Item<T>>> factory) {
        checkSetterPreconditions();
        collectionFactory = Constraint.isNotNull(factory, "Collection factory may not be null");
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
        mergeStrategy = strategy;
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        final ArrayList<Future<List<Item<T>>>> pipelineResultFutures = new ArrayList<>();

        for (final Pipeline<T> pipeline : getMergedPipelines()) {
            pipelineResultFutures.add(getExecutorService().submit(
                    new PipelineCallable<>(pipeline, getCollectionFactory().get())));
        }

        final ArrayList<List<Item<T>>> pipelineResults = new ArrayList<>();
        for (final Future<List<Item<T>>> future : pipelineResultFutures) {
            pipelineResults.add(FutureSupport.futureItems(future));
        }

        getCollectionMergeStrategy().merge(items, pipelineResults);
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        for (final Pipeline<T> pipeline : mergedPipelines) {
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }
}
