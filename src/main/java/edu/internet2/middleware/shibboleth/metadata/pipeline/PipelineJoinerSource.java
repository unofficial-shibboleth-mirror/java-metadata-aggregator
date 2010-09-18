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

package edu.internet2.middleware.shibboleth.metadata.pipeline;

import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;

import edu.internet2.middleware.shibboleth.metadata.Metadata;
import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.util.MetadataInfoHelper;

/**
 * This {@link Source} allows the joining of multiple pipeline outputs into a single {@link MetadataCollection} that can
 * then be used as the input source for another pipeline.
 * 
 * This source works producing a {@link MetadataCollection} by means of the registered
 * {@link edu.internet2.middleware.shibboleth.metadata.pipeline.PipelineJoinerSource.MetadataCollectionFactory} . Then
 * each of its registered {@link Pipeline} is invoked in turn (no ordering is guaranteed and pipelines may execute
 * concurrently). Each {@link Metadata} element from the resulting {@link MetadataCollection} of each pipeline is copied
 * ({@link Metadata#copy()}) and added to the sources {@link MetadataCollection}.
 */
@ThreadSafe
public class PipelineJoinerSource extends AbstractComponent implements Source<Metadata<?>> {

    /**
     * The factory used to create the {@link MetadataCollection} returned by this source. Default implementation is
     * {@link SimpleMetadataCollectionFacotry}.
     */
    private MetadataCollectionFactory collectionFactory = new SimpleMetadataCollectionFacotry();

    /** Pipelines whose results become the output of this source. */
    private List<Pipeline<Metadata<?>>> joinedPipelines = new LazyList<Pipeline<Metadata<?>>>();

    /**
     * Gets the unmodifiable set of pipelines used by this source
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
        collectionFactory = factory;
    }

    /** {@inheritDoc} */
    public MetadataCollection<Metadata<?>> execute() throws SourceProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        final MetadataCollection<Metadata<?>> sourceCollection = collectionFactory.newCollection();

        MetadataCollection<Metadata<?>> pipelineResult;
        for (Pipeline<Metadata<?>> pipeline : joinedPipelines) {
            try {
                pipelineResult = pipeline.execute();
                for (Metadata<?> metadata : pipelineResult) {
                    sourceCollection.add(metadata.copy());
                }
            } catch (PipelineProcessingException e) {
                throw new SourceProcessingException(e);
            }
        }

        compInfo.setCompleteInstant();
        MetadataInfoHelper.addToAll(sourceCollection, compInfo);
        return sourceCollection;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        for (Pipeline<Metadata<?>> pipeline : joinedPipelines) {
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }

    /** Factory used to create the {@link MetadataCollection} that will contain the output of the source. */
    public static interface MetadataCollectionFactory {

        /**
         * Creates the {@link MetadataCollection}.
         * 
         * @return the {@link MetadataCollection}
         */
        public MetadataCollection<Metadata<?>> newCollection();
    }

    /**
     * Implementation
     * {@link edu.internet2.middleware.shibboleth.metadata.pipeline.PipelineJoinerSource.MetadataCollectionFactory} that
     * produces {@link SimpleMetadataCollection} instances.
     */
    public static class SimpleMetadataCollectionFacotry implements MetadataCollectionFactory {

        /** {@inheritDoc} */
        public MetadataCollection<Metadata<?>> newCollection() {
            return new SimpleMetadataCollection<Metadata<?>>();
        }
    }
}