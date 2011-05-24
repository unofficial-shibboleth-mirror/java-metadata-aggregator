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

import org.opensaml.util.Assert;
import org.opensaml.util.StringSupport;

/** A {@link ItemMetadata} implementation that carries status information about an {@link Item}. */
public class StatusMetadata implements ItemMetadata {

    /** Serial version UID. */
    private static final long serialVersionUID = 9058387763020864155L;

    /** The component that generated this status information. */
    private String component;

    /** The message associated with this status. */
    private String message;

    /**
     * Constructor.
     * 
     * @param componentId ID of the component creating the status message, never null or empty
     * @param statusMessage the status message, never null or empty
     */
    public StatusMetadata(String componentId, String statusMessage) {
        String trimmedId = StringSupport.trimOrNull(componentId);
        Assert.isNotNull(trimmedId, "Component ID can not be null or empty");
        component = trimmedId;

        String trimmedMessage = StringSupport.trimOrNull(statusMessage);
        Assert.isNotNull(trimmedMessage, "Status message can not be null or empty");
        message = trimmedMessage;
    }

    /**
     * Gets the ID of the component that generated the status message.
     * 
     * @return ID of the component that generated the status message, never null
     */
    public String getComponentId() {
        return component;
    }

    /**
     * Gets the status message.
     * 
     * @return the status message, never null
     */
    public String getStatusMessage() {
        return message;
    }
}