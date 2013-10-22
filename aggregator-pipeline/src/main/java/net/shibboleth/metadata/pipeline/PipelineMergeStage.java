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
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * This {@link Stage} allows the merging of multiple pipeline outputs into a single {@link Collection} that can then be
 * used as the input source for another pipeline.
 * 
 * This source works producing a {@link Collection} by means of the registered {@link Supplier} . Then each of its
 * registered {@link Pipeline} is invoked in turn (no ordering is guaranteed and pipelines may execute concurrently).
 * After each pipeline has completed the results are merged in to the Item collection given to this stage by means of
 * the an {@link CollectionMergeStrategy}.
 * 
 * @param <T> the type of items processed by the stage
 */
@ThreadSafe
public class PipelineMergeStage<T> extends BaseStage<T> {

    /** Service used to execute the pipelines whose results will be merged. */
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * The factory used to create the item returned by this source. Default implementation is
     * {@link SimpleItemCollectionFactory}.
     */
    private Supplier<Collection<Item<T>>> collectionFactory = new SimpleItemCollectionFactory<T>();

    /** Strategy used to merge all the joined pipeline results in to the final Item collection. */
    private CollectionMergeStrategy mergeStrategy = new SimpleCollectionMergeStrategy();

    /** Pipelines whose results become the output of this source. */
    private List<Pipeline<T>> mergedPipelines = Collections.emptyList();

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
     * Gets the unmodifiable set of pipelines used by this stage.
     * 
     * @return unmodifiable set of pipelines used by this stage
     */
    @Nonnull @NonnullElements @Unmodifiable public List<Pipeline<T>> getMergedPipelines() {
        return mergedPipelines;
    }

    /**
     * Sets the pipelines joined by this stage.
     * 
     * @param pipelines pipelines joined by this stage
     */
    public synchronized void setMergedPipelines(
            @Nullable @NullableElements final List<? extends Pipeline<T>> pipelines) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (pipelines == null || pipelines.isEmpty()) {
            mergedPipelines = Collections.emptyList();
        } else {
            mergedPipelines = ImmutableList.copyOf(Iterables.filter(pipelines, Predicates.notNull()));
        }

    }

    /**
     * Gets the factory used to create the {@link Item} collection produced by this source.
     * 
     * @return factory used to create the {@link Item} collection produced by this source
     */
    @Nonnull public Supplier<Collection<Item<T>>> getCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Sets the factory used to create the {@link Item} collection produced by this source.
     * 
     * @param factory factory used to create the {@link Item} collection produced by this source
     */
    public synchronized void setCollectionFactory(@Nonnull final Supplier<Collection<Item<T>>> factory) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        collectionFactory = Constraint.isNotNull(factory, "Collection factory may not be null");
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

        mergeStrategy = strategy;
    }

    /** {@inheritDoc} */
    protected void doExecute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection)
            throws StageProcessingException {
        final ArrayList<Future<Collection<Item<T>>>> pipelineResultFutures = new ArrayList<>();

        for (Pipeline<T> pipeline : mergedPipelines) {
            pipelineResultFutures.add(executorService.submit(
                    new PipelineCallable<T>(pipeline, collectionFactory.get())));
        }

        final ArrayList<Collection<Item<T>>> pipelineResults = new ArrayList<>();
        for (Future<Collection<Item<T>>> future : pipelineResultFutures) {
            pipelineResults.add(FutureSupport.futureItems(future));
        }

        mergeStrategy.mergeCollection(itemCollection, pipelineResults);
    }

    /** {@inheritDoc} */
    protected void doDestroy() {
        executorService = null;
        collectionFactory = null;
        mergeStrategy = null;
        mergedPipelines = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        for (Pipeline<T> pipeline : mergedPipelines) {
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }
}