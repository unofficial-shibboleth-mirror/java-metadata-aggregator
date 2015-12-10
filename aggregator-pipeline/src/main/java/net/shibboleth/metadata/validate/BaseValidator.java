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

package net.shibboleth.metadata.validate;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;

/**
 * Base class for validator implementations.
 * 
 * Encapsulates the notion of an identifier for each validator class, and helper
 * methods for constructing status metadata.
 */
public abstract class BaseValidator extends AbstractIdentifiableInitializableComponent {

    /**
     * Construct a modified component identifier from the stage identifier and the
     * validator identifier.
     * 
     * @param stageId identifier for the calling stage
     * 
     * @return composite component identifier
     */
    private String makeComponentId(@Nonnull final String stageId) {
        final String id = getId();
        if (id == null) {
            return stageId;
        } else {
            return stageId + "/" + getId();
        }
    }

    /**
     * Add an {@link ErrorStatus} to the given {@link Item}.
     * 
     * @param message message to include in the status metadata
     * @param item {@link Item} to add the status metadata to
     * @param stageId component identifier for the calling stage
     */
    protected void addError(@Nonnull final String message, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        item.getItemMetadata().put(new ErrorStatus(makeComponentId(stageId), message));
    }
    
    /**
     * Add a {@link WarningStatus} to the given {@link Item}.
     * 
     * @param message message to include in the status metadata
     * @param item {@link Item} to add the status metadata to
     * @param stageId component identifier for the calling stage
     */
    protected void addWarning(@Nonnull final String message, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        item.getItemMetadata().put(new WarningStatus(makeComponentId(stageId), message));
    }
    
    /**
     * Add a {@link WarningStatus} or {@link ErrorStatus} to the given {@link Item}.
     * 
     * @param error <code>true</code> if an {@link ErrorStatus} should be added
     * @param message message to include in the status metadata
     * @param item {@link Item} to add the status metadata to
     * @param stageId component identifier for the calling stage
     */
    protected void addStatus(final boolean error, @Nonnull final String message, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        if (error) {
            addError(message, item, stageId);
        } else {
            addWarning(message, item, stageId);
        }
    }
    
}
