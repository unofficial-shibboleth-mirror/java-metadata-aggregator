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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.metadata.pipeline.impl.DirectExecutor;
import net.shibboleth.metadata.pipeline.impl.FutureSupport;
import net.shibboleth.metadata.pipeline.impl.PipelineCallable;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.collection.Pair;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.DeprecationSupport;
import net.shibboleth.shared.primitive.DeprecationSupport.ObjectType;

/**
 * A stage which, given an item collection and a list of {@link Pipeline} and {@link Predicate} pairs, sends the
 * collection of item copies selected by the predicate to the associated pipeline.
 *
 * <p>
 * This stage is similar to
 * {@link SplitMergeStage} but a given item, or more precisely a copy of it, may end up going to more than one pipeline
 * (or no pipeline).
 * </p>
 *
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>PipelineAndSelectionStrategies</code></li>
 * </ul>
 * 
 * <p>
 * If an {@link #executor} is provided, it will be used to execute the pipelines,
 * potentially concurrently. By default, the pipelines will be executed sequentially
 * on the calling thread.
 * </p>
 *
 * <p>
 * The caller is responsible for the lifecycle of any provided {@link Executor},
 * including the lifecycle of any threads or thread pools associated with it.
 * </p>
 *
 * <p>
 * If no {@link #collectionFactory} is given, then {@link SimpleItemCollectionFactory} is used.
 * </p>
 *
 * @param <T> type of items upon which this stage operates
 */
@ThreadSafe
public class PipelineDemultiplexerStage<T> extends AbstractStage<T> {

    /** {@link Executor} used to execute the selected and/or non-selected item pipelines. */
    @Nonnull @GuardedBy("this")
    private Executor executor = new DirectExecutor();

    /**
     * Whether this stage waits for all the invoked pipelines to complete before proceeding.
     *
     * Default: <code>true</code>.
     */
    @GuardedBy("this") private boolean waitingForPipelines = true;

    /** Factory used to create the Item collection that is then given to the pipelines. */
    @Nonnull @GuardedBy("this")
    private Supplier<List<Item<T>>> collectionFactory = new SimpleItemCollectionFactory<>();

    /** The pipelines through which items are sent and the selection strategy used for that pipeline. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Pair<Pipeline<T>, Predicate<Item<T>>>> pipelineAndStrategies = CollectionSupport.emptyList();

    /**
     * Gets the executor used to run the selected and non-selected item pipelines.
     * 
     * @return executor used to run the selected and non-selected item pipelines
     *
     * @since 0.10.0
     */
    @Nonnull public final synchronized Executor getExecutor() {
        return executor;
    }

    /**
     * Gets the executor service used to run the selected and non-selected item pipelines.
     * 
     * @return executor service used to run the selected and non-selected item pipelines
     *
     * @deprecated
     */
    @Deprecated(since="0.10.0", forRemoval=true)
    @Nonnull public final synchronized Executor getExecutorService() {
        DeprecationSupport.warnOnce(ObjectType.METHOD, "getExecutorService",
                "PipelineDemultiplexerStage", "getExecutor");
        return executor;
    }

    /**
     * Sets the executor used to run the selected and non-selected item pipelines.
     * 
     * @param exec executor used to run the selected and non-selected item pipelines
     *
     * @since 0.10.0
     */
    public synchronized void setExecutor(@Nonnull final Executor exec) {
        checkSetterPreconditions();
        executor = Constraint.isNotNull(exec, "executor can not be null");
    }

    /**
     * Sets the executor service used to run the selected and non-selected item pipelines.
     * 
     * @param service executor service used to run the selected and non-selected item pipelines
     *
     * @deprecated
     */
    @Deprecated(since="0.10.0", forRemoval=true)
    public synchronized void setExecutorService(@Nonnull final ExecutorService service) {
        DeprecationSupport.warnOnce(ObjectType.METHOD, "setExecutorService",
                "PipelineDemultiplexerStage", "setExecutor");
        checkSetterPreconditions();
        executor = Constraint.isNotNull(service, "ExecutorService can not be null");
    }

    /**
     * Gets whether this child waits for all the invoked pipelines to complete before proceeding.
     * 
     * @return whether this child waits for all the invoked pipelines to complete before proceeding
     */
    public final synchronized boolean isWaitingForPipelines() {
        return waitingForPipelines;
    }

    /**
     * Sets whether this child waits for all the invoked pipelines to complete before proceeding.
     * 
     * @param isWaiting whether this child waits for all the invoked pipelines to complete before proceeding
     */
    public synchronized void setWaitingForPipelines(final boolean isWaiting) {
        checkSetterPreconditions();
        waitingForPipelines = isWaiting;
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
     * Gets the pipeline and item selection strategies used to demultiplex item collections within this stage.
     * 
     * @return pipeline and item selection strategies used to demultiplex item collections within this stage
     */
    @Nonnull @NonnullElements @Unmodifiable public final synchronized List<Pair<Pipeline<T>, Predicate<Item<T>>>>
            getPipelineAndSelectionStrategies() {
        return pipelineAndStrategies;
    }

    /**
     * Sets the pipeline and item selection strategies used to demultiplex item collections within this stage.
     * 
     * @param passes pipeline and item selection strategies used to demultiplex item collections within this stage
     */
    public synchronized void setPipelineAndSelectionStrategies(
            @Nonnull @NonnullElements @Unmodifiable final List<Pair<Pipeline<T>, Predicate<Item<T>>>> passes) {
        checkSetterPreconditions();

        for (final Pair<Pipeline<T>, Predicate<Item<T>>> pass : passes) {
            Constraint.isNotNull(pass.getFirst(), "Pipeline can not be null");
            Constraint.isNotNull(pass.getSecond(), "Predicate can not be null");
        }

        pipelineAndStrategies = CollectionSupport.copyToList(passes);
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        final @Nonnull @NonnullElements List<Future<List<Item<T>>>> pipelineFutures = new ArrayList<>();

        for (final Pair<Pipeline<T>, Predicate<Item<T>>> pipelineAndStrategy : getPipelineAndSelectionStrategies()) {
            final @Nonnull Pipeline<T> pipeline =
                    Constraint.isNotNull(pipelineAndStrategy.getFirst(), "pipeline may not be null");
            final @Nonnull Predicate<Item<T>> selectionStrategy =
                    Constraint.isNotNull(pipelineAndStrategy.getSecond(), "strategy may not be null");
            final List<Item<T>> selectedItems = getCollectionFactory().get();
            assert selectedItems != null;

            for (final Item<T> item : items) {
                if (selectionStrategy.test(item)) {
                    selectedItems.add(item.copy());
                }
            }

            final @Nonnull var callable = new PipelineCallable<T>(pipeline, selectedItems);
            final @Nonnull var future = new FutureTask<List<Item<T>>>(callable);
            getExecutor().execute(future);
            pipelineFutures.add(future);
        }

        if (isWaitingForPipelines()) {
            for (final Future<List<Item<T>>> pipelineFuture : pipelineFutures) {
                assert pipelineFuture != null;
                FutureSupport.futureItems(pipelineFuture);
            }
        }
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (pipelineAndStrategies.isEmpty()) {
            throw new ComponentInitializationException(
                    "Pipeline and selection strategy collection can not be empty");
        }

        for (final Pair<Pipeline<T>, Predicate<Item<T>>> pipelineAndStrategy : pipelineAndStrategies) {
            final var pipeline = Constraint.isNotNull(pipelineAndStrategy.getFirst(), "pipeline may not be null");
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }
}
