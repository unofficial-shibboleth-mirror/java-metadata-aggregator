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

package net.shibboleth.metadata.pipeline;

import net.shibboleth.metadata.MetadataInfo;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.util.StringSupport;


/** Some basic information related to component's processing metadata elements. */
public class ComponentInfo implements MetadataInfo {

    /** Serial version UID. */
    private static final long serialVersionUID = -2181091708008092869L;

    /** ID of the component that operated on the element. */
    private String componentId;

    /** Gets the type of the component that operated on the element. */
    private Class<?> componentType;

    /** Instant when the component operation started. */
    private DateTime startInstant;

    /** Instant when the component operation completed. */
    private DateTime completeInstant;

    /** Constructor. */
    public ComponentInfo() {

    }

    /**
     * Constructor. Sets the ID and component type from the given component. Sets the start instant to now.
     * 
     * @param component component which this info describes
     */
    public ComponentInfo(final Component component) {
        componentId = component.getId();
        componentType = component.getClass();
        startInstant = new DateTime(ISOChronology.getInstanceUTC());
    }

    /**
     * Gets the ID of the component that operated on the element.
     * 
     * @return ID of the component that operated on the element
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Sets the ID of the component that operated on the element.
     * 
     * @param id ID of the component that operated on the element
     */
    public void setComponentId(final String id) {
        componentId = StringSupport.trimOrNull(id);
    }

    /**
     * Gets the type of the component that operated on the element.
     * 
     * @return type of the component that operated on the element
     */
    public Class<?> getComponentType() {
        return componentType;
    }

    /**
     * Sets the type of the component that operated on the element.
     * 
     * @param type type of the component that operated on the element
     */
    public void setComponentType(final Class<?> type) {
        componentType = type;
    }

    /**
     * Gets the instant when the component operation started.
     * 
     * @return instant when the component operation started
     */
    public DateTime getStartInstant() {
        return startInstant;
    }

    /**
     * Sets the instant when the component operation started.
     * 
     * @param instant instant when the component operation started
     */
    public void setStartInstant(final DateTime instant) {
        startInstant = instant;
    }

    /**
     * Gets the instant when the component operation completed.
     * 
     * @return instant when the component operation completed
     */
    public DateTime getCompleteInstant() {
        return completeInstant;
    }

    /** Sets the complete instant of the component to now. */
    public void setCompleteInstant() {
        completeInstant = new DateTime(ISOChronology.getInstanceUTC());
    }

    /**
     * Sets the instant when the component operation completed.
     * 
     * @param instant when the component operation completed
     */
    public void setCompleteInstant(DateTime instant) {
        completeInstant = instant;
    }
}