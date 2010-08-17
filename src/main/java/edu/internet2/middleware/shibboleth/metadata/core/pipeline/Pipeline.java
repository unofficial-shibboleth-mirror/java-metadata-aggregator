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

import java.util.List;

import net.jcip.annotations.ThreadSafe;
import edu.internet2.middleware.shibboleth.metadata.core.Metadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;

/**
 * A pipeline represents an ordered list of {@link Stage}s which takes transform input from either a {@link Source}.
 * 
 * Each pipeline must be initialized, via the {@link #initialize()} method, before use. After a pipeline has been
 * initialized it may never be re-initialized. A pipeline is not considered initialized until all of its {@link Stages},
 * have been initialized.
 * 
 * Pipelines are reusable and threadsafe.
 * 
 * @param <MetadataType> type of metadata element which is produced by this source
 */
@ThreadSafe
public interface Pipeline<MetadataType extends Metadata<?>> extends Component {

    /**
     * Gets the source of data operated upon by this pipeline.
     * 
     * @return source of data operated upon by this pipeline
     */
    public Source<MetadataType> getSource();

    /**
     * Gets the list of Stages within the pipeline.
     * 
     * @return unmodifiable list of stages within the pipeline
     */
    public List<Stage<MetadataType>> getStages();

    /**
     * Executes the pipeline by pulling information from the {@link Source}, executing all the registered {@link Stages}
     * , and giving the final result to the {@link Sink}.
     * 
     * @param source source of data to be processed by the pipeline
     * @param sink object to which the processed data is given
     * 
     * @return the result of the execution
     */
    public MetadataCollection<MetadataType> execute() throws PipelineProcessingException;
}