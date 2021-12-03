/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.pipeline;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An exception to be thrown to cause command-line aggregator execution to terminate.
 */
@ThreadSafe
public class TerminationException extends StageProcessingException {

    /** Serial version UID. */
    private static final long serialVersionUID = 5478964863946263591L;

    /** Constructor. */
    public TerminationException() {
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     */
    public TerminationException(@Nonnull final String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param wrappedException exception to be wrapped by this one
     */
    public TerminationException(@Nonnull final Exception wrappedException) {
        super(wrappedException);
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     * @param wrappedException exception to be wrapped by this one
     */
    public TerminationException(@Nonnull final String message, @Nonnull final Exception wrappedException) {
        super(message, wrappedException);
    }

}
