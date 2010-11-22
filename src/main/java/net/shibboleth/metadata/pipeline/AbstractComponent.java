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

import net.jcip.annotations.ThreadSafe;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.util.StringSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for pipeline components.
 * 
 * All classes which extend this base class must ensure that the state of the object does <strong>not</strong> change
 * after {@link #initialize()} is called. In particular, any setter method should check to see if the component has been
 * initialized and, if so, immediately return without altering the component state.
 */
@ThreadSafe
public abstract class AbstractComponent implements Component {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractComponent.class);

    /** Unique ID for the component. */
    private String id;

    /** Instant when the component was initialized. */
    private DateTime initInstant;

    /** {@inheritDoc} */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of this component.
     * 
     * @param componentId ID of the component, may not be null or empty
     */
    public synchronized void setId(final String componentId) {
        if (isInitialized()) {
            return;
        }
        id = StringSupport.trimOrNull(componentId);
    }

    /** {@inheritDoc} */
    public final DateTime getInitializationInstant() {
        return initInstant;
    }

    /** {@inheritDoc} */
    public synchronized final void initialize() throws ComponentInitializationException {
        if (isInitialized()) {
            throw new IllegalStateException("Pipeline component already initialized");
        }

        if (id == null) {
            throw new ComponentInitializationException("Pipeline component may not have a null or empty ID");
        }

        log.debug("Initializing pipeline component {}", getId());
        doInitialize();

        initInstant = new DateTime(ISOChronology.getInstanceUTC());
        log.debug("Pipeline component {} initialized", getId());
    }

    /** {@inheritDoc} */
    public final boolean isInitialized() {
        return initInstant != null;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return id.hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof AbstractComponent) {
            AbstractComponent otherComponent = (AbstractComponent) obj;
            return id.equals(otherComponent.getId());
        }

        return false;
    }

    /**
     * Do the initialization of the component. Default implementation of this method is a no-op.
     * 
     * Extending classes should override this method to perform any initialization logic necessary.
     * 
     * @throws ComponentInitializationException throw if there is a problem initializing the component
     */
    protected void doInitialize() throws ComponentInitializationException {

    }
}