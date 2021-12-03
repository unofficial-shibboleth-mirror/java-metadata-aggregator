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

import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import net.shibboleth.metadata.ItemMetadata;

/** Some basic information related to a component's processing of an {@link net.shibboleth.metadata.Item}. */
@Immutable
public class ComponentInfo implements ItemMetadata {

    /** ID of the component that operated on the element. */
    private final String componentId;

    /** Gets the type of the component that operated on the element. */
    private final Class<?> componentType;

    /** Instant when the component operation started. */
    private final Instant startInstant;

    /** Instant when the component operation completed. */
    private final Instant completeInstant;

    /**
     * Constructor.
     *
     * @param cId ID of the component performing the processing
     * @param cType type of the component performing the processing
     * @param start time at which the component started processing
     * @param complete time at which the component completed processing
     *
     * @since 0.10.0
     */
    public ComponentInfo(@Nonnull final String cId, @Nonnull final Class<?> cType,
            @Nonnull final Instant start, @Nonnull final Instant complete) {
        componentId = cId;
        componentType = cType;
        startInstant = start;
        completeInstant = complete;
    }

    /**
     * Gets the ID of the component that operated on the element.
     * 
     * @return ID of the component that operated on the element
     */
    @Nullable public String getComponentId() {
        return componentId;
    }

    /**
     * Gets the type of the component that operated on the element.
     * 
     * @return type of the component that operated on the element
     */
    @Nullable public Class<?> getComponentType() {
        return componentType;
    }

    /**
     * Gets the instant when the component operation started.
     * 
     * @return instant when the component operation started
     */
    @Nullable public Instant getStartInstant() {
        return startInstant;
    }

    /**
     * Gets the instant when the component operation completed.
     * 
     * @return instant when the component operation completed
     */
    @Nullable public Instant getCompleteInstant() {
        return completeInstant;
    }

}
