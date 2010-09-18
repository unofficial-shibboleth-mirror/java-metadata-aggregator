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

package edu.internet2.middleware.shibboleth.metadata.pipeline;

/** Exception indicating that the {@link Source} of the pipeline encountered an error. */
public class SourceProcessingException extends PipelineProcessingException {

    /** Serial version UID. */
    private static final long serialVersionUID = -5704283270420905308L;

    /** Constructor. */
    public SourceProcessingException() {

    }

    /**
     * Constructor.
     * 
     * @param message exception message
     */
    public SourceProcessingException(final String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param wrappedException exception to be wrapped by this one
     */
    public SourceProcessingException(final Exception wrappedException) {
        super(wrappedException);
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     * @param wrappedException exception to be wrapped by this one
     */
    public SourceProcessingException(final String message, final Exception wrappedException) {
        super(message, wrappedException);
    }
}