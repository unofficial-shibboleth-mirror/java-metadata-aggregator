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
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.xml.AttributeSupport;
import net.shibboleth.shared.xml.ElementSupport;

/**
 * For each Item collection element that is a SAML EntitiesDescriptor this stage will scan all descendant
 * EntitiesDescriptors and EntityDescriptors, determine the shortest cache duration, set that on the root
 * EntitiesDescriptor and remove the cache duration from all descendants.
 */
@ThreadSafe
public class PullUpCacheDurationStage extends AbstractIteratingStage<Element> {

    /** The minimum cache duration. Default value: <code>0</code> */
    @Nonnull @GuardedBy("this")
    private Duration minCacheDuration = Duration.ZERO;

    /** The maximum cache duration. Default value: {@value java.lang.Long#MAX_VALUE} */
    @Nonnull @GuardedBy("this")
    private Duration maxCacheDuration = Duration.ofMillis(Long.MAX_VALUE);

    /**
     * Gets the minimum cache duration.
     * 
     * @return minimum cache duration, always 0 or greater
     */
    @Nonnull
    public final synchronized Duration getMinimumCacheDuration() {
        return minCacheDuration;
    }

    /**
     * Sets the minimum cache duration.
     * 
     * @param duration the minimum cache duration
     */
    public synchronized void setMinimumCacheDuration(final Duration duration) {
        checkSetterPreconditions();
        if (duration.isNegative()) {
            minCacheDuration = Duration.ZERO;
        } else {
            minCacheDuration = duration;
        }
    }

    /**
     * Gets the maximum cache duration.
     * 
     * @return maximum cache duration, always greater than 0
     */
    @Nonnull
    public final synchronized Duration getMaximumCacheDuration() {
        return maxCacheDuration;
    }

    /**
     * Sets the maximum cache duration.
     * 
     * @param duration maximum cache duration, must be greater than 0
     */
    public synchronized void setMaximumCacheDuration(final Duration duration) {
        checkSetterPreconditions();
        Constraint.isGreaterThan(0, duration.toMillis(), "Maximum cache duration must be greater than 0");
        maxCacheDuration = duration;
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element descriptor = item.unwrap();
        final Duration cacheDuration = getShortestCacheDuration(descriptor);
        setCacheDuration(descriptor, cacheDuration);
    }

    /**
     * Gets the shortest cache duration for a given entity or entities descriptor and all its descendant descriptors.
     * 
     * @param descriptor descriptor from which to get the shortest cache duration
     * 
     * @return the shortest cache duration from the descriptor and its descendants or null if the descriptor does not
     *         contain a cache duration
     */
    @Nullable
    protected Duration getShortestCacheDuration(@Nonnull final Element descriptor) {
        Duration shortestCacheDuration = null;
        if (!SAMLMetadataSupport.isEntityOrEntitiesDescriptor(descriptor)) {
            return shortestCacheDuration;
        }

        Duration cacheDuration = null;
        final List<Element> entitiesDescriptors =
                ElementSupport.getChildElements(descriptor, SAMLMetadataSupport.ENTITIES_DESCRIPTOR_NAME);
        for (final Element entitiesDescriptor : entitiesDescriptors) {
            cacheDuration = getShortestCacheDuration(entitiesDescriptor);
            if (cacheDuration != null &&
                    (shortestCacheDuration == null || (cacheDuration.compareTo(shortestCacheDuration) < 0))) {
                shortestCacheDuration = cacheDuration;
            }
        }

        final List<Element> entityDescriptors =
                ElementSupport.getChildElements(descriptor, SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        for (final Element entityDescriptor : entityDescriptors) {
            cacheDuration = getShortestCacheDuration(entityDescriptor);
            if (cacheDuration != null &&
                    (shortestCacheDuration == null || (cacheDuration.compareTo(shortestCacheDuration) < 0))) {
                shortestCacheDuration = cacheDuration;
            }
        }

        final Attr cacheDurationAttr =
                AttributeSupport.getAttribute(descriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
        if (cacheDurationAttr != null) {
            cacheDuration = AttributeSupport.getDurationAttributeValue(cacheDurationAttr);
            if (cacheDuration != null &&
                    (shortestCacheDuration == null || (cacheDuration.compareTo(shortestCacheDuration) < 0))) {
                shortestCacheDuration = cacheDuration;
            }

            descriptor.removeAttributeNode(cacheDurationAttr);
        }

        return shortestCacheDuration;
    }

    /**
     * Sets the cache duration on the given descriptor. If the given cache duration is less than, or equal to, 0 no
     * duration is set. If the given cache duration is less than {@link #minCacheDuration} then the minimum cache
     * duration is set. If the given cache duration is greater than {@link #maxCacheDuration} then the maximum cache
     * duration is set. Otherwise the given cache duration is set.
     * 
     * @param descriptor entity or entities descriptor to receive the cache duration, never null
     * @param cacheDuration cache duration to be set, may be null
     */
    protected void setCacheDuration(@Nonnull final Element descriptor, @Nullable final Duration cacheDuration) {
        if (cacheDuration == null || cacheDuration.isNegative() || cacheDuration.isZero()) {
            return;
        }

        if (cacheDuration.compareTo(getMinimumCacheDuration()) < 0) {
            AttributeSupport.appendDurationAttribute(descriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME,
                    getMinimumCacheDuration());
        } else if (cacheDuration.compareTo(getMaximumCacheDuration()) > 0) {
            AttributeSupport.appendDurationAttribute(descriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME,
                    getMaximumCacheDuration());
        } else {
            AttributeSupport.appendDurationAttribute(descriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME,
                    cacheDuration);
        }
    }
}
