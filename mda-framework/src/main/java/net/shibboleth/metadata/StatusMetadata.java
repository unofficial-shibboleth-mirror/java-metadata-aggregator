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

package net.shibboleth.metadata;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import net.shibboleth.shared.annotation.constraint.NotEmpty;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.StringSupport;

/** A {@link ItemMetadata} implementation that carries status information about an {@link Item}. */
@Immutable
public abstract sealed class StatusMetadata implements ItemMetadata
    permits ErrorStatus, WarningStatus, InfoStatus {

    /** The component that generated this status information. */
    @Nonnull @NotEmpty private final String component;

    /** The message associated with this status. */
    @Nonnull @NotEmpty private final String message;

    /**
     * Constructor.
     *
     * @param componentId ID of the component creating the status message, never null or empty
     * @param statusMessage the status message, never null or empty
     */
    public StatusMetadata(final @Nonnull String componentId, final String statusMessage) {
        component = Constraint.isNotNull(StringSupport.trimOrNull(componentId),
                "Component ID can not be null or empty");
        message = Constraint.isNotNull(StringSupport.trimOrNull(statusMessage),
                "Status message can not be null or empty");
    }

    /**
     * Gets the ID of the component that generated the status message.
     * 
     * @return ID of the component that generated the status message, never null or empty
     */
    @Nonnull @NotEmpty public String getComponentId() {
        return component;
    }

    /**
     * Gets the status message.
     * 
     * @return the status message, never null or empty
     */
    @Nonnull @NotEmpty public String getStatusMessage() {
        return message;
    }
}
