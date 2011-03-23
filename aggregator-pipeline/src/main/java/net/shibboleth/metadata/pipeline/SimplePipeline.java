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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Metadata;
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

    /** Stages for this pipeline. */
    private List<Stage<MetadataType>> pipelineStages = Collections.emptyList();

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
    public void execute(Collection<MetadataType> metadataCollection) throws PipelineProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        for (Stage<MetadataType> stage : pipelineStages) {
            stage.execute(metadataCollection);
        }

        compInfo.setCompleteInstant();
        MetadataInfoHelper.addToAll(metadataCollection, compInfo);
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        for (Stage<MetadataType> stage : pipelineStages) {
            if (!stage.isInitialized()) {
                stage.initialize();
            }
        }
    }
}