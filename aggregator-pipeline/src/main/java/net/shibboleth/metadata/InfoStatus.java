/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata;

/**
 * A type of {@link StatusMetadata} that carries informational messages. These messages should never be used to carry
 * status messages that would indicate an error or failing of the Item in some way.
 */
public class InfoStatus extends StatusMetadata {

    /** Serial version UID. */
    private static final long serialVersionUID = 4418261627233729537L;

    /**
     * Constructor.
     * 
     * @param componentId ID of the component creating the status message, never null or empty
     * @param statusMessage the status message, never null or empty
     */
    public InfoStatus(String componentId, String statusMessage) {
        super(componentId, statusMessage);
    }
}