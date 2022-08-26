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

import java.time.Duration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;

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
public class SetCacheDurationStage extends AbstractIteratingStage<Element> {

    /** Cache duration that will be set on each metadata element. */
    @NonnullAfterInit @GuardedBy("this") private Duration cacheDuration;

    /**
     * Gets the cache duration that will be set on each metadata element.
     * 
     * @return cache duration
     */
    @NonnullAfterInit
    public final synchronized Duration getCacheDuration() {
        return cacheDuration;
    }

    /**
     * Sets the cache duration that will be set on each metadata element.
     * 
     * @param duration cache duration
     */
    public synchronized void setCacheDuration(@Nonnull final Duration duration) {
        checkSetterPreconditions();

        Constraint.isNotNull(duration, "cache duration cannot be null");
        Constraint.isFalse(duration.isZero(), "cache duration cannot be zero");
        Constraint.isFalse(duration.isNegative(), "cache duration cannot be negative");

        cacheDuration = duration;
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element descriptor = item.unwrap();
        if (SAMLMetadataSupport.isEntityOrEntitiesDescriptor(descriptor)) {
            AttributeSupport.removeAttribute(descriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
            AttributeSupport.appendDurationAttribute(descriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME,
                    getCacheDuration());
        }
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (cacheDuration == null) {
            throw new ComponentInitializationException("cache duration must be set");
        }
    }
}
