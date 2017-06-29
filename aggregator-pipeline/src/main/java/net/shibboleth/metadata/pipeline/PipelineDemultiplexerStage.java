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
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList.Builder;

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
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /** Whether this child waits for all the invoked pipelines to complete before proceeding. */
    private boolean waitingForPipelines;

    /** Factory used to create the Item collection that is then given to the pipelines. */
    private Supplier<Collection<Item<T>>> collectionFactory = new SimpleItemCollectionFactory<>();

    /** The pipelines through which items are sent and the selection strategy used for that pipeline. */
    private List<Pair<Pipeline<T>, Predicate<Item<T>>>> pipelineAndStrategies = Collections.emptyList();

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
     * Gets whether this child waits for all the invoked pipelines to complete before proceeding.
     * 
     * @return whether this child waits for all the invoked pipelines to complete before proceeding
     */
    public boolean isWaitingForPipelines() {
        return waitingForPipelines;
    }

    /**
     * Sets whether this child waits for all the invoked pipelines to complete before proceeding.
     * 
     * @param isWaiting whether this child waits for all the invoked pipelines to complete before proceeding
     */
    public synchronized void setWaitingForPipelines(final boolean isWaiting) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        waitingForPipelines = isWaiting;
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
     * Gets the pipeline and item selection strategies used to demultiplex item collections within this stage.
     * 
     * @return pipeline and item selection strategies used to demultiplex item collections within this stage
     */
    @Nonnull @NonnullElements @Unmodifiable public List<Pair<Pipeline<T>, Predicate<Item<T>>>>
            getPipelineAndSelectionStrategies() {
        return pipelineAndStrategies;
    }

    /**
     * Sets the pipeline and item selection strategies used to demultiplex item collections within this stage.
     * 
     * @param passes pipeline and item selection strategies used to demultiplex item collections within this stage
     */
    public synchronized void setPipelineAndSelectionStrategies(
            @Nonnull @NonnullElements final List<Pair<Pipeline<T>, Predicate<Item<T>>>> passes) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (passes == null || passes.isEmpty()) {
            pipelineAndStrategies = Collections.emptyList();
            return;
        }

        final Builder<Pair<Pipeline<T>, Predicate<Item<T>>>> checkedPasses = new Builder<>();
        for (final Pair<Pipeline<T>, Predicate<Item<T>>> pass : passes) {
            Constraint.isNotNull(pass.getFirst(), "Pipeline can not be null");
            Constraint.isNotNull(pass.getSecond(), "Predicate can not be null");

            checkedPasses.add(new Pair<>(pass));
        }

        pipelineAndStrategies = checkedPasses.build();
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection)
            throws StageProcessingException {
        Collection<Item<T>> selectedItems;
        final ArrayList<Future<Collection<Item<T>>>> pipelineFutures = new ArrayList<>();

        for (final Pair<Pipeline<T>, Predicate<Item<T>>> pipelineAndStrategy : pipelineAndStrategies) {
            final Pipeline<T> pipeline = pipelineAndStrategy.getFirst();
            final Predicate<Item<T>> selectionStrategy = pipelineAndStrategy.getSecond();
            selectedItems = collectionFactory.get();

            for (final Item<T> item : itemCollection) {
                if (selectionStrategy.apply(item)) {
//                    @SuppressWarnings("unchecked") final ItemType copied = (ItemType) item.copy();
//                    selectedItems.add(copied);
                    selectedItems.add(item.copy());
                }
            }

            pipelineFutures.add(executorService.submit(new PipelineCallable<>(pipeline, selectedItems)));
        }

        if (isWaitingForPipelines()) {
            for (final Future<Collection<Item<T>>> pipelineFuture : pipelineFutures) {
                FutureSupport.futureItems(pipelineFuture);
            }
        }
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        executorService = null;
        collectionFactory = null;
        pipelineAndStrategies = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (pipelineAndStrategies.isEmpty()) {
            throw new ComponentInitializationException(
                    "Pipeline and selection strategy collection can not be null or empty");
        }

        Pipeline<T> pipeline;
        for (final Pair<Pipeline<T>, Predicate<Item<T>>> pipelineAndStrategy : pipelineAndStrategies) {
            pipeline = pipelineAndStrategy.getFirst();
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }
}