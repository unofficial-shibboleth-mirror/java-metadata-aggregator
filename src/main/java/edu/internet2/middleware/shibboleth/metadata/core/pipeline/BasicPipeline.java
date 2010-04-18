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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.ThreadSafe;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElement;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.sink.Sink;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.source.Source;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.stage.Stage;

/**
 * A simple, synchronous, implementation of {@link Pipeline}.
 * 
 * @param <ElementType> type of metadata element upon which the pipeline operates
 */
@ThreadSafe
public class BasicPipeline<ElementType extends MetadataElement<?>> extends AbstractComponent implements
        Pipeline<ElementType, PipelineResult> {
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(BasicPipeline.class);

    /** The stages for the pipeline. */
    private ArrayList<Stage<ElementType>> stages;

    /**
     * Constructor.
     * 
     * @param pipelineId the ID of the pipeline
     * @param pipelineStages the list of stages for the pipeline in execution order
     */
    public BasicPipeline(String pipelineId, List<Stage<ElementType>> pipelineStages) {
        super(pipelineId);
        stages = new ArrayList<Stage<ElementType>>(pipelineStages);
    }

    /** {@inheritDoc} */
    public List<Stage<ElementType>> getStages() {
        return Collections.unmodifiableList(stages);
    }

    /** {@inheritDoc} */
    public PipelineResult execute(Source<ElementType> source, Sink<ElementType> sink) {
        Map<String, Object> emptyParams = Collections.emptyMap();
        return execute(emptyParams, source, sink);
    }

    /** {@inheritDoc} */
    public PipelineResult execute(Map<String, Object> parameters, Source<ElementType> source, Sink<ElementType> sink) {
        try {
            if(!source.isInitialized()){
                log.debug("{} pipeline initializing source {}", getId(), source.getId());
                source.isInitialized();
            }
            log.debug("{} pipeline getting metadata data from source {}", getId(), source.getId());
            MetadataElementCollection<ElementType> metadataCollection = source.execute(parameters);
            
            for (Stage<ElementType> stage : stages) {
                log.debug("{} pipeline executing {}", getId(), stage.getId());
                metadataCollection = stage.execute(parameters, metadataCollection);
            }

            if(!sink.isInitialized()){
                log.debug("{} pipeline initializing sink {}", getId(), sink.getId());
                sink.initialize();
            }
            log.debug("{} pipeline sending metadata to sink {}", getId(), sink.getId());
            sink.execute(parameters, metadataCollection);

            return new BasicPipelineResult(parameters);
        } catch (PipelineException e) {
            log.error("Error occured while executing pipeline " + getId(), e);
            return new BasicPipelineResult(parameters, e);
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws PipelineInitializationException {
        for (Stage<ElementType> stage : stages) {
            if (!stage.isInitialized()) {
                stage.initialize();
            }
        }
    }
}