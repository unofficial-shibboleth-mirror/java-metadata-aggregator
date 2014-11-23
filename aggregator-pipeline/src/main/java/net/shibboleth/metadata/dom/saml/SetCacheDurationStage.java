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

package net.shibboleth.metadata.dom.saml;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.Duration;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;

import org.w3c.dom.Element;

/**
 * Sets a cacheDuration attribute for every EntityDescriptor and EntitiesDescriptor element in the collection.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>cacheDuration</code></li>
 * </ul>
 */
@ThreadSafe
public class SetCacheDurationStage extends BaseIteratingStage<Element> {

    /** Cache duration, in milliseconds, that will be set on each metadata element. */
    @Duration
    private long cacheDuration;

    /**
     * Gets the cache duration, in milliseconds, that will be set on each metadata element.
     * 
     * @return cache duration, in milliseconds
     */
    public long getCacheDuration() {
        return cacheDuration;
    }

    /**
     * Sets the cache duration, in milliseconds, that will be set on each metadata element.
     * 
     * @param duration cache duration, in milliseconds
     */
    public synchronized void setCacheDuration(@Duration @Positive final long duration) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        cacheDuration = Constraint.isGreaterThan(0, duration, "cache duration must be greater than 0");
    }

    /** {@inheritDoc} */
    @Override protected boolean doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element descriptor = item.unwrap();
        if (SAMLMetadataSupport.isEntityOrEntitiesDescriptor(descriptor)) {
            AttributeSupport.removeAttribute(descriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
            AttributeSupport.appendDurationAttribute(descriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME,
                    cacheDuration);
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (cacheDuration <= 0) {
            throw new ComponentInitializationException("cache duration must be greater than 0");
        }
    }
}