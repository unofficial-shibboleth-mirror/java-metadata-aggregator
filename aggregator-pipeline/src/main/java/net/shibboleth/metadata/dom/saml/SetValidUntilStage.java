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
 * Sets a validUntil attribute for every EntityDescriptor and EntitiesDescriptor element in the collection.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>validityDuration</code></li>
 * </ul>
 */
@ThreadSafe
public class SetValidUntilStage extends BaseIteratingStage<Element> {

    /** Amount of time the descriptors will be valid, expressed in milliseconds. */
    @Duration
    private long validityDuration;

    /**
     * Gets the amount of time the descriptors will be valid, expressed in milliseconds.
     * 
     * @return amount of time the descriptors will be valid, expressed in milliseconds
     */
    public long getValidityDuration() {
        return validityDuration;
    }

    /**
     * Sets the amount of time the descriptors will be valid, expressed in milliseconds.
     * 
     * @param duration amount of time the descriptors will be valid, expressed in milliseconds
     */
    public synchronized void setValidityDuration(@Duration @Positive final long duration) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        validityDuration = Constraint.isGreaterThan(0, duration, "validity duration must be greater than 0");
    }

    /** {@inheritDoc} */
    @Override protected boolean doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element descriptor = item.unwrap();
        if (SAMLMetadataSupport.isEntityOrEntitiesDescriptor(descriptor)) {
            AttributeSupport.removeAttribute(descriptor, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME);
            AttributeSupport.appendDateTimeAttribute(descriptor, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME,
                    System.currentTimeMillis() + validityDuration);
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (validityDuration <= 0) {
            throw new ComponentInitializationException("validity duration must be greater than 0");
        }
    }
}