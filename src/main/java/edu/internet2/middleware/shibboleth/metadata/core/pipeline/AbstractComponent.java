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

import java.util.Collections;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

/** Base implementation for pipeline components. */
public abstract class AbstractComponent implements Component {

    /** Unique ID for the component. */
    private String id;

    /** Instant when the component was initialized. */
    private DateTime initInstant;

    /** Parameters used to initialize the component. */
    private Map<String, Object> initParams;

    /**
     * Constructor.
     * 
     * @param componentId the ID of the component
     */
    public AbstractComponent(String componentId) {
        id = componentId;
    }

    /** {@inheritDoc} */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of this component.
     * 
     * @param componentId ID of the component, may not be null or empty
     */
    protected void setId(String componentId) {
        id = componentId;
    }

    /** {@inheritDoc} */
    public DateTime getInitializationInstant() {
        return initInstant;
    }

    /** {@inheritDoc} */
    public Map<String, Object> getInitializationParameters() {
        return initParams;
    }

    /** {@inheritDoc} */
    public void initialize(Map<String, Object> parameters) {
        initParams = Collections.unmodifiableMap(parameters);
        initInstant = new DateTime(ISOChronology.getInstanceUTC());
    }

    /** {@inheritDoc} */
    public boolean isInitialized() {
        return initInstant != null;
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        return id.hashCode();
    }
    
    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        
        if(obj instanceof AbstractComponent){
            AbstractComponent otherComponent = (AbstractComponent) obj;
            return id.equals(otherComponent.getId());
        }
        
        return false;
    }
}