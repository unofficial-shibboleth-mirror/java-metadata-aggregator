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

import java.util.List;

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.Assert;
import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * For each Item collection element that is a SAML EntitiesDescriptor this stage will scan all descendant
 * EntitiesDescriptors and EntityDescriptors, determine the earliest valid until date, set that on the root
 * EntitiesDescriptor and remove the valid until dates from all descendants.
 */
public class PullUpValidUntilStage extends BaseIteratingStage<DomElementItem> {

    /** The minimum amount of time, in milliseconds, a descriptor may be valid . Default value: 0 */
    private long minValidityDuration;

    /**
     * The maximum amount of time, in milliseconds, a descriptor may be valid. Default value:
     * {@value java.lang.Long#MAX_VALUE}
     */
    private long maxValidityDuration = Long.MAX_VALUE;

    /**
     * Gets the minimum amount of time, in milliseconds, a descriptor may be valid.
     * 
     * @return minimum amount of time, in milliseconds, a descriptor may be valid, always 0 or greater
     */
    public long getMinimumValidityDuration() {
        return minValidityDuration;
    }

    /**
     * Sets the minimum amount of time, in milliseconds, a descriptor may be valid.
     * 
     * @param duration minimum amount of time, in milliseconds, a descriptor may be valid
     */
    public synchronized void setMinimumValidityDuration(long duration) {
        if (isInitialized()) {
            return;
        }

        if (duration < 0) {
            minValidityDuration = 0;
        } else {
            minValidityDuration = duration;
        }
    }

    /**
     * Gets the maximum amount of time, in milliseconds, a descriptor may be valid.
     * 
     * @return maximum maximum amount of time, in milliseconds, a descriptor may be valid, always greater than 0
     */
    public long getMaximumValidityDuration() {
        return maxValidityDuration;
    }

    /**
     * Sets the maximum amount of time, in milliseconds, a descriptor may be valid.
     * 
     * @param duration maximum amount of time, in milliseconds, a descriptor may be valid, must be greater than 0
     */
    public synchronized void setMaximumValidityDuration(long duration) {
        if (isInitialized()) {
            return;
        }

        Assert.isGreaterThan(0, duration, "Maximum validity duration must be greater than 0");
        maxValidityDuration = duration;
    }

    /** {@inheritDoc} */
    protected boolean doExecute(DomElementItem item) throws StageProcessingException {
        Element descriptor = item.unwrap();
        Long nearestValidUntil = getNearestValidUntil(descriptor);
        setValidUntil(descriptor, nearestValidUntil);
        return true;
    }

    /**
     * Gets the shorts cache duration for a given entity and entities descriptor an all its descendant descriptors.
     * 
     * @param descriptor descriptor from which to get the shortest cache duration
     * 
     * @return the shortest cache duration from the descriptor and its descendants or null if the descriptor does not
     *         contain a cache duration
     */
    protected Long getNearestValidUntil(final Element descriptor) {
        Long nearestValidUntil = null;
        if (!SamlMetadataSupport.isEntitiesDescriptor(descriptor)
                && !SamlMetadataSupport.isEntityDescriptor(descriptor)) {
            return nearestValidUntil;
        }

        Long validUntil;
        List<Element> entitiesDescriptors =
                ElementSupport.getChildElements(descriptor, SamlMetadataSupport.ENTITIES_DESCRIPTOR_NAME);
        for (Element entitiesDescriptor : entitiesDescriptors) {
            validUntil = getNearestValidUntil(entitiesDescriptor);
            if (validUntil != null && (nearestValidUntil == null || (validUntil < nearestValidUntil))) {
                nearestValidUntil = validUntil;
            }
        }

        List<Element> entityDescriptors =
                ElementSupport.getChildElements(descriptor, SamlMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        for (Element entityDescriptor : entityDescriptors) {
            validUntil = getNearestValidUntil(entityDescriptor);
            if (validUntil != null && (nearestValidUntil == null || (validUntil < nearestValidUntil))) {
                nearestValidUntil = validUntil;
            }
        }

        Attr validUntilAttr =
                descriptor.getAttributeNodeNS(null, SamlMetadataSupport.VALID_UNTIL_ATTIB_NAME.getLocalPart());
        if (validUntilAttr != null) {
            validUntil = AttributeSupport.getDateTimeAttributeAsLong(validUntilAttr);
            if (validUntil != null && (nearestValidUntil == null || (validUntil < nearestValidUntil))) {
                nearestValidUntil = validUntil;
            }

            descriptor.removeAttributeNode(validUntilAttr);
        }

        return nearestValidUntil;
    }

    /**
     * Sets the valid until instant on the given descriptor. If the given validUntil is null no instant is set. If the
     * given validUntil is less than now + {@link #minValidityDuration} then the instant of now + the minimum duration
     * is set. If the given validUntil is greater than now + {@link #maxValidityDuration} then the instant of now + the
     * maximum duration is set. Otherwise the given instant is set.
     * 
     * @param descriptor entity or entities descriptor to receive the validUntil, never null
     * @param validUntil validUntil time to be set on the given descriptor
     */
    protected void setValidUntil(final Element descriptor, final Long validUntil) {
        if (validUntil == null) {
            return;
        }

        long now = System.currentTimeMillis();
        long minValidUntil = now + minValidityDuration;
        long maxValidUntil = now + maxValidityDuration;
        if (maxValidUntil < 0) {
            maxValidUntil = Long.MAX_VALUE;
        }

        long boundedValidUntil;
        if (validUntil < minValidUntil) {
            boundedValidUntil = minValidUntil;
        } else if (validUntil > maxValidUntil) {
            boundedValidUntil = maxValidUntil;
        } else {
            boundedValidUntil = validUntil;
        }

        AttributeSupport.appendDateTimeAttribute(descriptor, SamlMetadataSupport.VALID_UNTIL_ATTIB_NAME,
                boundedValidUntil);
    }
}