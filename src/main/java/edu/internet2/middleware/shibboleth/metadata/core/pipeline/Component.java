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

import org.joda.time.DateTime;

/**
 * Base interface for all the components of a {@link Pipeline}, including the pipeline itself.
 */
public interface Component {

    /**
     * Gets a unique identifier for the component.
     * 
     * @return unique identifier for the component
     */
    public String getId();

    /**
     * Checks whether the component has been initialized.
     * 
     * @return whether the component has been initialized
     */
    public boolean isInitialized();

    /**
     * Initializes the component.
     * 
     * Once a component has been initialized this method will throw an {@link IllegalStateException}.
     * 
     * @throws ComponentInitializationException thrown if there is a problem initializing the component for use
     */
    public void initialize() throws ComponentInitializationException;

    /**
     * Gets the instant the component was initialized. All instants are given in UTC time.
     * 
     * @return instant the component was initialized
     */
    public DateTime getInitializationInstant();
}