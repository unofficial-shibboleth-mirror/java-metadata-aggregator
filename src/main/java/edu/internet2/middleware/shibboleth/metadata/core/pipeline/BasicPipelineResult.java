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

import java.util.Map;

/** Basic implementation of {@link PipelineResult}. */
public class BasicPipelineResult implements PipelineResult {

    /** Parameters given when the pipeline was executed. */
    private Map<String, Object> parameters;

    /** Exception that caused the failure in the pipeline. */
    private PipelineException exception;

    /** Constructor. */
    public BasicPipelineResult() {
    }

    /**
     * Constructor.
     * 
     * @param executionParameters parameters given when the pipeline was executed
     */
    public BasicPipelineResult(Map<String, Object> executionParameters) {
        parameters = executionParameters;
    }

    /**
     * Constructor.
     * 
     * @param executionParameters parameters given when the pipeline was executed
     * @param pipelineException exception that caused the failure of the pipeline
     */
    public BasicPipelineResult(Map<String, Object> executionParameters, PipelineException pipelineException) {
        parameters = executionParameters;
        exception = pipelineException;
    }

    /** {@inheritDoc} */
    public PipelineException getException() {
        return exception;
    }

    /** {@inheritDoc} */
    public Map<String, Object> getExecutionParameters() {
        return parameters;
    }

    /** {@inheritDoc} */
    public boolean wasSuccessful() {
        return exception == null;
    }
}