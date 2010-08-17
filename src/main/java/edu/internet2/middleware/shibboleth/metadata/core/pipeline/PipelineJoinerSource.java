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

package edu.internet2.middleware.shibboleth.metadata.core.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.opensaml.util.Assert;

import edu.internet2.middleware.shibboleth.metadata.core.Metadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.util.MetadataInfoHelper;

/**
 * This {@link Source} allows the joining of multiple pipeline outputs into a single {@link MetadataCollection} that can
 * then be used as the input source for another pipeline.
 * 
 * This source works producing a {@link MetadataCollection} by means of the registered {@link MetadataCollectionFactory}
 * . Then each of its registered {@link Pipeline} is invoked in turn (no ordering is guaranteed and pipelines may
 * execute concurrently). Each {@link Metadata} element from the resulting {@link MetadataCollection} of each pipeline
 * is copied ({@link Metadata#copy()}) and added to the sources {@link MetadataCollection}.
 */
public class PipelineJoinerSource extends AbstractComponent implements Source<Metadata<?>> {

    /**
     * The factory used to create the {@link MetadataCollection} returned by this source. Default implementation is
     * {@link SimpleMetadataCollectionFacotry}.
     */
    private MetadataCollectionFactory collectionFactory;

    /** Pipelines whose results become the output of this source. */
    private Collection<Pipeline<Metadata<?>>> pipelines;

    /**
     * Constructor.
     * 
     * @param id ID of this source
     * @param joinedPipelines pipelines whose output make up the output of this source
     */
    public PipelineJoinerSource(String id, Collection<Pipeline<Metadata<?>>> joinedPipelines) {
        super(id);
        collectionFactory = new SimpleMetadataCollectionFacotry();

        Assert.isNotEmpty(joinedPipelines, "Joined pipeline collection may not be null or empty");
        pipelines = Collections.unmodifiableCollection(new ArrayList<Pipeline<Metadata<?>>>(joinedPipelines));
    }

    /**
     * Gets the unmodifiable set of pipelines used by this source
     * 
     * @return unmodifiable set of pipelines used by this source
     */
    public Collection<Pipeline<Metadata<?>>> getPipelines() {
        return pipelines;
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
    public void setCollectionFactory(MetadataCollectionFactory factory) {
        collectionFactory = factory;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        for (Pipeline<Metadata<?>> pipeline : pipelines) {
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }

    /** {@inheritDoc} */
    public MetadataCollection<Metadata<?>> execute() throws SourceProcessingException {
        ComponentInfo compInfo = new ComponentInfo(this);

        MetadataCollection<Metadata<?>> sourceCollection = collectionFactory.newCollection();

        MetadataCollection<Metadata<?>> pipelineResult;
        for (Pipeline<Metadata<?>> pipeline : pipelines) {
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
        return null;
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

    /** Implementation {@link MetadataCollectionFactory} that produces {@link SimpleMetadataCollection} instances. */
    public static class SimpleMetadataCollectionFacotry implements MetadataCollectionFactory {

        /** {@inheritDoc} */
        public MetadataCollection<Metadata<?>> newCollection() {
            return new SimpleMetadataCollection<Metadata<?>>();
        }
    }
}