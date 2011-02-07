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

import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.util.MetadataInfoHelper;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;


/** 
 * A very simple implementation of {@link Pipeline}. This implementation takes a static source and list of stages. 
 *
 * @param <MetadataType> the type of metadata which is produced by the source and operated upon by the stages
 */
@ThreadSafe
public class SimplePipeline<MetadataType extends Metadata<?>> extends AbstractComponent implements
        Pipeline<MetadataType> {

    /** Source for this pipeline. */
    private Source<MetadataType> pipelineSource;

    /** Stages for this pipeline. */
    private List<Stage<MetadataType>> pipelineStages = Collections.emptyList();

    /** {@inheritDoc} */
    public Source<MetadataType> getSource() {
        return pipelineSource;
    }

    /**
     * Sets the source that produces the initial set of metadata upon which this pipeline operates.
     * 
     * @param source source that produces the initial set of metadata upon which this pipeline operates
     */
    public synchronized void setSource(final Source<MetadataType> source) {
        if (isInitialized()) {
            return;
        }
        pipelineSource = source;
    }

    /** {@inheritDoc} */
    public List<Stage<MetadataType>> getStages() {
        return pipelineStages;
    }

    /**
     * Sets the stages that make up this pipeline.
     * 
     * @param stages stages that make up this pipeline
     */
    public synchronized void setStages(final List<Stage<MetadataType>> stages) {
        if (isInitialized()) {
            return;
        }
        pipelineStages = Collections.unmodifiableList(CollectionSupport.addNonNull(stages,
                new LazyList<Stage<MetadataType>>()));
    }

    /** {@inheritDoc} */
    public MetadataCollection<MetadataType> execute() throws PipelineProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        MetadataCollection<MetadataType> metadataCollection = pipelineSource.execute();
        for (Stage<MetadataType> stage : pipelineStages) {
            metadataCollection = stage.execute(metadataCollection);
        }

        compInfo.setCompleteInstant();
        MetadataInfoHelper.addToAll(metadataCollection, compInfo);
        return metadataCollection;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        if (pipelineSource == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId() + ", Source may not be null");
        }

        if (!pipelineSource.isInitialized()) {
            pipelineSource.initialize();
        }

        for (Stage<MetadataType> stage : pipelineStages) {
            if (!stage.isInitialized()) {
                stage.initialize();
            }
        }
    }
}