/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata.dom.saml;

import java.util.List;

import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.Assert;
import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * For each metadata collection element that is a SAML EntitiesDescriptor this stage will scan all descendant
 * EntitiesDescriptors and EntityDescriptors, determine the shortest cache duration, set that on the root
 * EntitiesDescriptor and remove the cache duration from all descendants.
 */
public class PullUpCacheDurationStage extends BaseIteratingStage<DomMetadata> {

    /** The minimum cache duration in milliseconds. Default value: {@value} */
    private long minCacheDuration;

    /** The maximum cache duration in milliseconds. Default value: {@value} */
    private long maxCacheDuration = Long.MAX_VALUE;

    /**
     * Gets the minimum cache duration in milliseconds.
     * 
     * @return minimum cache duration in milliseconds, always 0 or greater
     */
    public long getMinimumCacheDuration() {
        return minCacheDuration;
    }

    /**
     * Sets the minimum cache duration in milliseconds.
     * 
     * @param duration the minimum cache duration in milliseconds
     */
    public synchronized void setMinimumCacheDuration(long duration) {
        if (isInitialized()) {
            return;
        }

        if (duration < 0) {
            minCacheDuration = 0;
        } else {
            minCacheDuration = duration;
        }
    }

    /**
     * Gets the maximum cache duration in milliseconds.
     * 
     * @return maximum cache duration in milliseconds, always greater than 0
     */
    public long getMaximumCacheDuration() {
        return maxCacheDuration;
    }

    /**
     * Sets the maximum cache duration in milliseconds.
     * 
     * @param duration maximum cache duration in milliseconds, must be greater than 0
     */
    public synchronized void setMaximumCacheDuration(long duration) {
        if (isInitialized()) {
            return;
        }
        Assert.isGreaterThan(0, duration, "Maximum cache duration must be greater than 0");
        maxCacheDuration = duration;
    }

    /** {@inheritDoc} */
    protected boolean doExecute(DomMetadata metadata) throws StageProcessingException {
        Element descriptor = metadata.getMetadata();
        long cacheDuration = getShortestCacheDuration(descriptor);
        setCacheDuration(descriptor, cacheDuration);
        return true;
    }

    /**
     * Gets the shorts cache duration for a given entity and entities descriptor an all its descendant descriptors.
     * 
     * @param descriptor descriptor from which to get the shortest cache duration
     * 
     * @return the shortest cache duration from the descriptor and its descendants or 0 if the descriptor does not
     *         contain a cache duration
     */
    protected long getShortestCacheDuration(final Element descriptor) {
        long shortestCacheDuration = 0;
        if (!MetadataHelper.isEntitiesDescriptor(descriptor) && !MetadataHelper.isEntityDescriptor(descriptor)) {
            return shortestCacheDuration;
        }

        long cacheDuration;
        List<Element> entitiesDescriptors = ElementSupport.getChildElements(descriptor,
                MetadataHelper.ENTITIES_DESCRIPTOR_NAME);
        for (Element entitiesDescriptor : entitiesDescriptors) {
            cacheDuration = getShortestCacheDuration(entitiesDescriptor);
            if (shortestCacheDuration > 0 && cacheDuration < shortestCacheDuration) {
                shortestCacheDuration = cacheDuration;
            }
        }

        List<Element> entityDescriptors = ElementSupport.getChildElements(descriptor,
                MetadataHelper.ENTITY_DESCRIPTOR_NAME);
        for (Element entityDescriptor : entityDescriptors) {
            cacheDuration = getShortestCacheDuration(entityDescriptor);
            if (shortestCacheDuration > 0 && cacheDuration < shortestCacheDuration) {
                shortestCacheDuration = cacheDuration;
            }
        }

        Attr cacheDurationAttr = AttributeSupport.getAttribute(descriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME);
        if (cacheDurationAttr != null) {
            cacheDuration = AttributeSupport.getDurationAttributeValueAsLong(cacheDurationAttr);
            if (shortestCacheDuration > 0 && cacheDuration < shortestCacheDuration) {
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
     * @param cacheDuration cache duration to be set
     */
    protected void setCacheDuration(final Element descriptor, final long cacheDuration) {
        if (cacheDuration <= 0) {
            return;
        }

        if (cacheDuration < minCacheDuration) {
            AttributeSupport.appendDurationAttribute(descriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME,
                    minCacheDuration);
        } else if (cacheDuration > maxCacheDuration) {
            AttributeSupport.appendDurationAttribute(descriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME,
                    maxCacheDuration);
        } else {
            AttributeSupport.appendDurationAttribute(descriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME,
                    minCacheDuration);
        }
    }
}