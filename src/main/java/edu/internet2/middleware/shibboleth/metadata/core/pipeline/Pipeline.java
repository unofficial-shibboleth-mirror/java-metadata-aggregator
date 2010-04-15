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
import java.util.Map;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElement;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.sink.Sink;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.source.Source;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.stage.Stage;

import net.jcip.annotations.ThreadSafe;

/**
 * A pipeline represents a set of configurable {@link Stage}s which takes transform input from either a {@link Source}
 * (if it's the first stage in the pipeline) or other {@link Stage}s. The result of the pipeline is given to the
 * {@link Sink}.
 * 
 * Each pipeline must be initialized, via the {@link #initialize(Map)} method, before use. After a pipeline has been
 * initialized it may never be re-initialized. A pipeline is not considered initialized until all of its {@link Stages},
 * have been initialized.
 * 
 * Pipelines are reusable and threadsafe.
 * 
 * @param <ElementType> type of metadata element which is produced by this source
 * @param <ResultType> type of result returned by the pipeline
 */
@ThreadSafe
public interface Pipeline<ElementType extends MetadataElement<?>, ResultType extends PipelineResult> extends Component {

    /**
     * Gets the list of Stages within the pipeline.
     * 
     * @return unmodifiable list of stages within the pipeline
     */
    public List<Stage<ElementType>> getStages();

    /**
     * Executes the pipeline by pulling information from the {@link Source}, executing all the registered {@link Stages}
     * , and giving the final result to the {@link Sink}.
     * 
     * @param source source of data to be processed by the pipeline
     * @param sink object to which the processed data is given
     * 
     * @return the result of the execution
     */
    public ResultType execute(Source<ElementType> source, Sink<ElementType> sink);

    /**
     * Executes the pipeline by pulling information from the {@link Source}, executing the registered {@link Stages},
     * and giving the final result to the {@link Sink}.
     * 
     * This method allows additional configuration parameters to be passed in. These parameters <strong>may</strong>
     * override the initialization parameters provided to the {@link Source}, {@link Stage}s, or {@link Sink}. Not all
     * initialization parameters may be overridden.
     * 
     * @param parameters configuration parameters which may override the initialization configuration parameters
     * @param source source of data to be processed by the pipeline
     * @param sink object to which the processed data is given
     * 
     * @return the result of the execution
     */
    public ResultType execute(Map<String, Object> parameters, Source<ElementType> source, Sink<ElementType> sink);
}