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

/** Indicates the result of a {@link Pipeline} execution. */
public interface PipelineResult {

    /**
     * Gets whether the pipeline execution was successful.
     * 
     * @return true if the execution was successful, false if not
     */
    public boolean wasSuccessful();

    /**
     * Gets the parameters, if any, passed in at execution time.
     * 
     * @return the per-invocation parameters given at execution time
     */
    public Map<String, Object> getExecutionParameters();

    /**
     * Gets the exception that caused the error.
     * 
     * @return exception that caused the error
     */
    public PipelineProcessingException getProcessingException();
}