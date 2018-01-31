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

import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.utilities.java.support.component.IdentifiedComponent;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/** Some basic information related to a component's processing of an {@link net.shibboleth.metadata.Item}. */
public class ComponentInfo implements ItemMetadata {

    /** Serial version UID. */
    private static final long serialVersionUID = -2181091708008092869L;

    /** ID of the component that operated on the element. */
    private String componentId;

    /** Gets the type of the component that operated on the element. */
    private Class<?> componentType;

    /** Instant when the component operation started. */
    private Instant startInstant;

    /** Instant when the component operation completed. */
    private Instant completeInstant;

    /** Constructor. */
    public ComponentInfo() {

    }

    /**
     * Constructor. Sets the ID and component type from the given component. Sets the start instant to now.
     * 
     * @param component component which this info describes
     */
    public ComponentInfo(@Nonnull final IdentifiedComponent component) {
        Constraint.isNotNull(component, "Component can not be null");
        componentId = component.getId();
        componentType = component.getClass();
        startInstant = Instant.now();
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
     * Sets the ID of the component that operated on the element.
     * 
     * @param id ID of the component that operated on the element
     */
    public void setComponentId(@Nullable final String id) {
        componentId = StringSupport.trimOrNull(id);
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
     * Sets the type of the component that operated on the element.
     * 
     * @param type type of the component that operated on the element
     */
    public void setComponentType(@Nullable final Class<?> type) {
        componentType = type;
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
     * Sets the instant when the component operation started.
     * 
     * @param instant instant when the component operation started
     */
    public void setStartInstant(@Nullable final Instant instant) {
        startInstant = instant;
    }

    /**
     * Gets the instant when the component operation completed.
     * 
     * @return instant when the component operation completed
     */
    @Nullable public Instant getCompleteInstant() {
        return completeInstant;
    }

    /** Sets the complete instant of the component to now. */
    public void setCompleteInstant() {
        completeInstant = Instant.now();
    }

    /**
     * Sets the instant when the component operation completed.
     * 
     * @param instant when the component operation completed
     */
    public void setCompleteInstant(@Nullable final Instant instant) {
        completeInstant = instant;
    }
}
