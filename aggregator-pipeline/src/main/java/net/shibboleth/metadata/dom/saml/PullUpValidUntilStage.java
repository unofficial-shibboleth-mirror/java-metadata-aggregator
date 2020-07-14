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
import java.time.Instant;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/**
 * For each Item collection element that is a SAML EntitiesDescriptor this stage will scan all descendant
 * EntitiesDescriptors and EntityDescriptors, determine the earliest valid until date, set that on the root
 * EntitiesDescriptor and remove the valid until dates from all descendants.
 */
@ThreadSafe
public class PullUpValidUntilStage extends AbstractIteratingStage<Element> {

    /** The minimum amount of time a descriptor may be valid. Default value: 0 */
    @Nonnull @GuardedBy("this")
    private Duration minValidityDuration = Duration.ZERO;

    /**
     * The maximum amount of time a descriptor may be valid. Default value:
     * {@value java.lang.Long#MAX_VALUE}
     */
    @Nonnull @GuardedBy("this")
    private Duration maxValidityDuration = Duration.ofMillis(Long.MAX_VALUE);

    /**
     * Gets the minimum amount of time a descriptor may be valid.
     * 
     * @return minimum amount of time a descriptor may be valid, always 0 or greater
     */
    @Nonnull
    public final synchronized Duration getMinimumValidityDuration() {
        return minValidityDuration;
    }

    /**
     * Sets the minimum amount of time a descriptor may be valid.
     * 
     * @param duration minimum amount of time a descriptor may be valid
     */
    public synchronized void setMinimumValidityDuration(@Nonnull final Duration duration) {
        throwSetterPreconditionExceptions();
        if (duration.isNegative()) {
            minValidityDuration = Duration.ZERO;
        } else {
            minValidityDuration = duration;
        }
    }

    /**
     * Gets the maximum amount of time a descriptor may be valid.
     * 
     * @return maximum maximum amount of time a descriptor may be valid, always greater than 0
     */
    @Nonnull
    public final synchronized Duration getMaximumValidityDuration() {
        return maxValidityDuration;
    }

    /**
     * Sets the maximum amount of time a descriptor may be valid.
     * 
     * @param duration maximum amount of time a descriptor may be valid, must be greater than 0
     */
    public synchronized void setMaximumValidityDuration(@Nonnull final Duration duration) {
        throwSetterPreconditionExceptions();
        Constraint.isGreaterThan(0, duration.toMillis(), "Maximum validity duration must be greater than 0");
        maxValidityDuration = duration;
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element descriptor = item.unwrap();
        final Instant nearestValidUntil = getNearestValidUntil(descriptor);
        setValidUntil(descriptor, nearestValidUntil);
    }

    /**
     * Gets the shortest cache duration for a given entity and entities descriptor and all its descendant descriptors.
     * 
     * @param descriptor descriptor from which to get the shortest cache duration
     * 
     * @return the shortest cache duration from the descriptor and its descendants or null if the descriptor does not
     *         contain a cache duration
     */
    @Nullable
    protected Instant getNearestValidUntil(@Nonnull final Element descriptor) {
        Instant nearestValidUntil = null;
        if (!SAMLMetadataSupport.isEntityOrEntitiesDescriptor(descriptor)) {
            return nearestValidUntil;
        }

        Instant validUntil;
        final List<Element> entitiesDescriptors =
                ElementSupport.getChildElements(descriptor, SAMLMetadataSupport.ENTITIES_DESCRIPTOR_NAME);
        for (final Element entitiesDescriptor : entitiesDescriptors) {
            validUntil = getNearestValidUntil(entitiesDescriptor);
            if (validUntil != null && (nearestValidUntil == null || (validUntil.isBefore(nearestValidUntil)))) {
                nearestValidUntil = validUntil;
            }
        }

        final List<Element> entityDescriptors =
                ElementSupport.getChildElements(descriptor, SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        for (final Element entityDescriptor : entityDescriptors) {
            validUntil = getNearestValidUntil(entityDescriptor);
            if (validUntil != null && (nearestValidUntil == null || (validUntil.isBefore(nearestValidUntil)))) {
                nearestValidUntil = validUntil;
            }
        }

        final Attr validUntilAttr =
                descriptor.getAttributeNodeNS(null, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME.getLocalPart());
        if (validUntilAttr != null) {
            validUntil = AttributeSupport.getDateTimeAttribute(validUntilAttr);
            if (validUntil != null && (nearestValidUntil == null || (validUntil.isBefore(nearestValidUntil)))) {
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
    protected void setValidUntil(@Nonnull final Element descriptor, @Nullable final Instant validUntil) {
        if (validUntil == null) {
            return;
        }

        final Instant now = Instant.now();
        final Instant minValidUntil = now.plus(getMinimumValidityDuration());
        final Instant maxValidUntil = now.plus(getMaximumValidityDuration());

        final Instant boundedValidUntil;
        if (validUntil.isBefore(minValidUntil)) {
            boundedValidUntil = minValidUntil;
        } else if (validUntil.isAfter(maxValidUntil)) {
            boundedValidUntil = maxValidUntil;
        } else {
            boundedValidUntil = validUntil;
        }

        AttributeSupport.appendDateTimeAttribute(descriptor, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME,
                boundedValidUntil);
    }
}
