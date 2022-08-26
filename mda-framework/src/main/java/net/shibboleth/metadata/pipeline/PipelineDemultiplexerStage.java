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
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A stage which, given an item collection and a list of {@link Pipeline} and {@link Predicate} pairs, sends the
 * collection of item copies selected by the predicate to the associated pipeline. This stage is similar to
 * {@link SplitMergeStage} but a given item, or more precisely a copy of it, may end up going to more than one pipeline
 * (or no pipeline).
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>PipelineAndSelectionStrategies</code></li>
 * </ul>
 * 
 * <p>
 * If no {@link #executorService} is provided, one will be created using {@link Executors#newFixedThreadPool(int)} with
 * 6 threads.
 * 
 * If no {@link #collectionFactory} is given, then {@link SimpleItemCollectionFactory} is used.
 * 
 * @param <T> type of items upon which this stage operates
 */
@ThreadSafe
public class PipelineDemultiplexerStage<T> extends AbstractStage<T> {

    /** Service used to execute the selected and/or non-selected item pipelines. */
    @Nonnull @GuardedBy("this")
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Whether this child waits for all the invoked pipelines to complete before proceeding.
     *
     * Default: <code>true</code>.
     */
    @GuardedBy("this") private boolean waitingForPipelines = true;

    /** Factory used to create the Item collection that is then given to the pipelines. */
    @Nonnull @GuardedBy("this")
    private Supplier<List<Item<T>>> collectionFactory = new SimpleItemCollectionFactory<>();

    /** The pipelines through which items are sent and the selection strategy used for that pipeline. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Pair<Pipeline<T>, Predicate<Item<T>>>> pipelineAndStrategies = List.of();

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

        pipelineAndStrategies = List.copyOf(passes);
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        final ArrayList<Future<List<Item<T>>>> pipelineFutures = new ArrayList<>();

        for (final Pair<Pipeline<T>, Predicate<Item<T>>> pipelineAndStrategy : getPipelineAndSelectionStrategies()) {
            final Pipeline<T> pipeline = pipelineAndStrategy.getFirst();
            final Predicate<Item<T>> selectionStrategy = pipelineAndStrategy.getSecond();
            final List<Item<T>> selectedItems = getCollectionFactory().get();

            for (final Item<T> item : items) {
                if (selectionStrategy.test(item)) {
//                    @SuppressWarnings("unchecked") final ItemType copied = (ItemType) item.copy();
//                    selectedItems.add(copied);
                    selectedItems.add(item.copy());
                }
            }

            pipelineFutures.add(getExecutorService().submit(new PipelineCallable<>(pipeline, selectedItems)));
        }

        if (isWaitingForPipelines()) {
            for (final Future<List<Item<T>>> pipelineFuture : pipelineFutures) {
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
            final var pipeline = pipelineAndStrategy.getFirst();
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }
}
