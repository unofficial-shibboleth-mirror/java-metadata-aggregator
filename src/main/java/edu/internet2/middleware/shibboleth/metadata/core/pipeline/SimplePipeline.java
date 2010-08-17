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
import java.util.Collections;
import java.util.List;

import org.opensaml.util.Assert;

import edu.internet2.middleware.shibboleth.metadata.core.Metadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.util.MetadataInfoHelper;

/** A very simple implementation of {@link Pipeline}. This implementation takes a static source and list of stages. */
public class SimplePipeline<MetadataType extends Metadata<?>> extends AbstractComponent implements
        Pipeline<MetadataType> {

    /** Source for this pipeline. */
    private Source<MetadataType> source;

    /** Stages for this pipeline. */
    private List<Stage<MetadataType>> stages;

    /**
     * Constructor.
     * 
     * @param id id of this pipeline
     * @param pipelineSource source of this pipeline
     * @param pipelineStages stages of this pipeline
     */
    public SimplePipeline(String id, Source<MetadataType> pipelineSource, List<Stage<MetadataType>> pipelineStages) {
        super(id);
        
        Assert.isNotNull(pipelineSource, "Pipeline source may not be null");
        source = pipelineSource;
        
        if(pipelineStages == null){
            stages = Collections.emptyList();
        }else{
            stages = Collections.unmodifiableList(new ArrayList<Stage<MetadataType>>(pipelineStages));
        }
    }

    /** {@inheritDoc} */
    public Source<MetadataType> getSource() {
        return source;
    }

    /** {@inheritDoc} */
    public List<Stage<MetadataType>> getStages() {
        return stages;
    }

    /** {@inheritDoc} */
    public MetadataCollection<MetadataType> execute() throws PipelineProcessingException {
        ComponentInfo compInfo = new ComponentInfo(this);
        MetadataCollection<MetadataType> metadataCollection = source.execute();

        for (Stage<MetadataType> stage : stages) {
            metadataCollection = stage.execute(metadataCollection);
        }

        compInfo.setCompleteInstant();
        MetadataInfoHelper.addToAll(metadataCollection, compInfo);
        return metadataCollection;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        source.initialize();
        for (Stage<MetadataType> stage : stages) {
            stage.initialize();
        }
    }
}