/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Metadata;

import org.opensaml.util.Assert;
import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;

/**
 * This {@link Stage} allows the joining of multiple pipeline outputs into a single {@link Collection} that can then be
 * used as the input source for another pipeline.
 * 
 * This source works producing a {@link Collection} by means of the registered
 * {@link net.shibboleth.metadata.pipeline.PipelineJoinerStage.MetadataCollectionFactory} . Then each of its registered
 * {@link Pipeline} is invoked in turn (no ordering is guaranteed and pipelines may execute concurrently). After each
 * pipeline has completed the results are merged in to the metadata collection given to this stage by means of the an
 * {@link CollectionMergeStrategy}.
 */
@ThreadSafe
public class PipelineJoinerStage extends BaseStage<Metadata<?>> {

    /**
     * The factory used to create the {@link MetadataCollection} returned by this source. Default implementation is
     * {@link SimpleMetadataCollectionFacotry}.
     */
    private MetadataCollectionFactory collectionFactory = new SimpleMetadataCollectionFacotry();

    /** Strategy used to merge all the joined pipeline results in to the final metadata collection. */
    private CollectionMergeStrategy mergeStrategy = new SimpleCollectionMergeStrategy();

    /** Pipelines whose results become the output of this source. */
    private List<Pipeline<Metadata<?>>> joinedPipelines = new LazyList<Pipeline<Metadata<?>>>();

    /**
     * Gets the unmodifiable set of pipelines used by this source.
     * 
     * @return unmodifiable set of pipelines used by this source
     */
    public List<Pipeline<Metadata<?>>> getJoinedPipelines() {
        return joinedPipelines;
    }

    /**
     * Sets the pipelines joined by this source.
     * 
     * @param pipelines pipelines joined by this source
     */
    public synchronized void setJoinedPipelines(final List<Pipeline<Metadata<?>>> pipelines) {
        if (isInitialized()) {
            return;
        }
        joinedPipelines = Collections.unmodifiableList(CollectionSupport.addNonNull(pipelines,
                new LazyList<Pipeline<Metadata<?>>>()));
    }

    /**
     * Gets the factory used to create the {@link MetadataCollection} produced by this source.
     * 
     * @return factory used to create the {@link MetadataCollection} produced by this source
     */
    public MetadataCollectionFactory getCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Sets the factory used to create the {@link MetadataCollection} produced by this source.
     * 
     * @param factory factory used to create the {@link MetadataCollection} produced by this source
     */
    public synchronized void setCollectionFactory(final MetadataCollectionFactory factory) {
        if (isInitialized()) {
            return;
        }
        Assert.isNotNull(factory, "Collection factory may not be null");
        collectionFactory = factory;
    }

    /**
     * Gets the strategy used to merge all the joined pipeline results in to the final metadata collection.
     * 
     * @return strategy used to merge all the joined pipeline results in to the final metadata collection, never null
     */
    public CollectionMergeStrategy getCollectionMergeStrategy() {
        return mergeStrategy;
    }

    /**
     * Sets the strategy used to merge all the joined pipeline results in to the final metadata collection.
     * 
     * @param strategy strategy used to merge all the joined pipeline results in to the final metadata collection, never
     *            null
     */
    public synchronized void setCollectionMergeStrategy(final CollectionMergeStrategy strategy) {
        if (isInitialized()) {
            return;
        }
        Assert.isNotNull(strategy, "Collection merge strategy may not be null");
        mergeStrategy = strategy;
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<Metadata<?>> metadataCollection) throws StageProcessingException {
        final ArrayList<Collection<Metadata<?>>> pipelineResults = new ArrayList<Collection<Metadata<?>>>();
        Collection<Metadata<?>> pipelineResult;
        for (Pipeline<Metadata<?>> pipeline : joinedPipelines) {
            try {
                pipelineResult = collectionFactory.newCollection();
                pipeline.execute(pipelineResult);
                pipelineResults.add(pipelineResult);
            } catch (PipelineProcessingException e) {
                throw new StageProcessingException(e);
            }
        }

        mergeStrategy.mergeCollection(metadataCollection,
                pipelineResults.toArray(new Collection[pipelineResults.size()]));
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        for (Pipeline<Metadata<?>> pipeline : joinedPipelines) {
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }

    /** Factory used to create the {@link Collection} that will be passed in to each child pipeline. */
    public static interface MetadataCollectionFactory {

        /**
         * Creates the {@link Collection}.
         * 
         * @return the {@link Collection}
         */
        public Collection<Metadata<?>> newCollection();
    }

    /**
     * Implementation {@link net.shibboleth.metadata.pipeline.PipelineJoinerStage.MetadataCollectionFactory} that
     * produces {@link ArrayList} instances.
     */
    public static class SimpleMetadataCollectionFacotry implements MetadataCollectionFactory {

        /** {@inheritDoc} */
        public Collection<Metadata<?>> newCollection() {
            return new ArrayList<Metadata<?>>();
        }
    }

    /**
     * Strategy used to merge the results of each child pipeline in to the collection of metadata given to this stage.
     */
    public static interface CollectionMergeStrategy {

        /**
         * Merges the results of each child pipeline in to the collection of metadata given to this stage.
         * 
         * @param target collection in to which all the metadata should be merged, never null
         * @param sources collections of metadata to be merged in to the target, never null not containing any null
         *            elements
         */
        public void mergeCollection(Collection<Metadata<?>> target, Collection<Metadata<?>>... sources);
    }

    /**
     * A {@link CollectionMergeStrategy} that adds the metadata from each source, in order, by means of the
     * {@link Collection#addAll(Collection)} method on the target.
     */
    public static class SimpleCollectionMergeStrategy implements CollectionMergeStrategy {

        /** {@inheritDoc} */
        public void mergeCollection(Collection<Metadata<?>> target, Collection<Metadata<?>>... sources) {
            for (Collection<Metadata<?>> source : sources) {
                target.addAll(source);
            }
        }
    }
}