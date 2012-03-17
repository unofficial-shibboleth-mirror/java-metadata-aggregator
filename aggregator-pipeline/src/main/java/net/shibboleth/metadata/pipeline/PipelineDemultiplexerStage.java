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

import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

/**
 * A stage which, given an item collection and a list of {@link Pipeline} and {@link ItemSelectionStrategy} pairs, sends
 * the collection of item copies selected by the selection strategy to the associated pipeline. This stage is similar to
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
 * If no {@link ExecutorService} is provided, one will be created using {@link Executors#newFixedThreadPool(int)} with 6
 * threads.
 * 
 * If no {@link ItemCollectionFactory} is given, then {@link SimpleItemCollectionFactory} is used.
 * 
 * @param <ItemType> type of items upon which this stage operates
 */
@ThreadSafe
public class PipelineDemultiplexerStage<ItemType extends Item<?>> extends BaseStage<ItemType> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(PipelineDemultiplexerStage.class);

    /** Service used to execute the selected and/or non-selected item pipelines. */
    private ExecutorService executorService;

    /** Whether this child waits for all the invoked pipelines to complete before proceeding. */
    private boolean waitingForPipelines;

    /** Factory used to create the Item collection that is then given to the pipelines. */
    private Supplier<Collection<ItemType>> collectionFactory;

    /** The pipelines through which items are sent and the selection strategy used for that pipeline. */
    private List<Pair<Pipeline<ItemType>, Predicate<ItemType>>> pipelineAndStrategies;

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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        executorService = service;
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
    public synchronized void setWaitingForPipelines(boolean isWaiting) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        waitingForPipelines = isWaiting;
    }

    /**
     * Gets the factory used to create the Item collection that is then given to the pipelines.
     * 
     * @return factory used to create the Item collection that is then given to the pipelines
     */
    public Supplier<Collection<ItemType>> getCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Sets the factory used to create the Item collection that is then given to the pipelines.
     * 
     * @param factory factory used to create the Item collection that is then given to the pipelines
     */
    public synchronized void setCollectionFactory(Supplier<Collection<ItemType>> factory) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        collectionFactory = factory;
    }

    /**
     * Gets the pipeline and item selection strategies used to demultiplex item collections within this stage.
     * 
     * @return pipeline and item selection strategies used to demultiplex item collections within this stage
     */
    public List<Pair<Pipeline<ItemType>, Predicate<ItemType>>> getPipelineAndSelectionStrategies() {
        return pipelineAndStrategies;
    }

    /**
     * Sets the pipeline and item selection strategies used to demultiplex item collections within this stage.
     * 
     * @param pass pipeline and item selection strategies used to demultiplex item collections within this stage
     */
    public synchronized void
            setPipelineAndSelectionStrategies(List<Pair<Pipeline<ItemType>, Predicate<ItemType>>> pass) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        pipelineAndStrategies = pass;
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<ItemType> itemCollection) throws StageProcessingException {
        Pipeline<ItemType> pipeline;
        Predicate<ItemType> selectionStrategy;
        Collection<ItemType> selectedItems;
        ArrayList<Future> pipelineFutures = new ArrayList<Future>();

        for (Pair<Pipeline<ItemType>, Predicate<ItemType>> pipelineAndStrategy : pipelineAndStrategies) {
            pipeline = pipelineAndStrategy.getFirst();
            selectionStrategy = pipelineAndStrategy.getSecond();
            selectedItems = collectionFactory.get();

            for (ItemType item : itemCollection) {
                if (selectionStrategy.apply(item)) {
                    selectedItems.add((ItemType) item.copy());
                }
            }

            pipelineFutures.add(executorService.submit(new PipelineCallable(pipeline, selectedItems)));
        }

        if (isWaitingForPipelines()) {
            for (Future pipelineFuture : pipelineFutures) {
                try {
                    pipelineFuture.get();
                } catch (ExecutionException e) {
                    log.error("Pipeline threw an unexpected exception", e);
                } catch (InterruptedException e) {
                    log.error("Execution service was interrupted", e);
                }
            }
        }
    }

    /** {@inheritDoc} */
    protected void doDestroy() {
        executorService = null;
        collectionFactory = null;
        pipelineAndStrategies = null;

        super.doDestroy();
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

        if (pipelineAndStrategies == null || pipelineAndStrategies.isEmpty()) {
            throw new ComponentInitializationException(
                    "Pipeline and selection strategy collection can not be null or empty");
        }

        Pipeline<ItemType> pipeline;
        for (Pair<Pipeline<ItemType>, Predicate<ItemType>> pipelineAndStrategy : pipelineAndStrategies) {
            pipeline = pipelineAndStrategy.getFirst();
            if (pipeline == null) {
                throw new ComponentInitializationException(
                        "Pipeline of pipeline and selection strategy collection entry can not be null");
            }

            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }

            if (pipelineAndStrategy.getSecond() == null) {
                throw new ComponentInitializationException(
                        "Item selection strategy of pipeline and selection strategy collection entry can not be null");
            }
        }
    }
}