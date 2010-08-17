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

/** Base exception for errors that occur within the pipeline execution. */
public class PipelineProcessingException extends ComponentException {

    /** Serial version UID. */
    private static final long serialVersionUID = -8313183972495091212L;

    /** Constructor. */
    public PipelineProcessingException() {

    }

    /**
     * Constructor.
     * 
     * @param message exception message
     */
    public PipelineProcessingException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param wrappedException exception to be wrapped by this one
     */
    public PipelineProcessingException(Exception wrappedException) {
        super(wrappedException);
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     * @param wrappedException exception to be wrapped by this one
     */
    public PipelineProcessingException(String message, Exception wrappedException) {
        super(message, wrappedException);
    }
}