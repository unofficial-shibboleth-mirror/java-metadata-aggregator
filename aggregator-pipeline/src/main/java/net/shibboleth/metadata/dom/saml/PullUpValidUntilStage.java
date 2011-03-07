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

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.convert.ConverterManager;
import org.opensaml.util.Assert;
import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInfo;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.Stage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.util.MetadataInfoHelper;

/**
 * For each metadata collection element that is a SAML EntitiesDescriptor this stage will scan all descendant
 * EntitiesDescriptors and EntityDescriptors, determine the earliest valid until date, set that on the root
 * EntitiesDescriptor and remove the valid until dates from all descendants.
 */
public class PullUpValidUntilStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Manager used to convert to read/write XML data/times. */
    ConverterManager timeConverter;

    /** The minimum amount of time, in milliseconds, a descriptor may be valid . Default value: {@value} */
    private long minValidityDuration;

    /** The maximum amount of time, in milliseconds, a descriptor may be valid. Default value: {@value} */
    private long maxValidityDuration = Long.MAX_VALUE;

    /**
     * Gets the minimum amount of time ,in milliseconds, a descriptor may be valid.
     * 
     * @return minimum amount of time, in milliseconds, a descriptor may be valid, always 0 or greater
     */
    public long getMinimumValidityDuration() {
        return minValidityDuration;
    }

    /**
     * Sets the minimum amount of time,i n milliseconds, a descriptor may be valid.
     * 
     * @param duration minimum amount of time, in milliseconds, a descriptor may be valid
     */
    public synchronized void setMinimumCacheDuration(long duration) {
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
    public long getMaximumCacheDuration() {
        return maxValidityDuration;
    }

    /**
     * Sets the maximum amount of time, in milliseconds, a descriptor may be valid.
     * 
     * @param duration maximum amount of time, in milliseconds, a descriptor may be valid, must be greater than 0
     */
    public synchronized void setMaximumCacheDuration(long duration) {
        Assert.isGreaterThan(0, duration, "Maximum cache duration must be greater than 0");
        maxValidityDuration = duration;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> metadataCollection)
            throws StageProcessingException {

        final ComponentInfo compInfo = new ComponentInfo(this);

        Element descriptor;
        long nearestValidUntil;
        for (DomMetadata metadata : metadataCollection) {
            descriptor = metadata.getMetadata();
            nearestValidUntil = getNearestValidUntil(descriptor);
            setValidUntil(descriptor, nearestValidUntil);
        }

        compInfo.setCompleteInstant();
        MetadataInfoHelper.addToAll(metadataCollection, compInfo);
        return metadataCollection;
    }

    /**
     * Gets the shorts cache duration for a given entity and entities descriptor an all its descendant descriptors.
     * 
     * @param descriptor descriptor from which to get the shortest cache duration
     * 
     * @return the shortest cache duration from the descriptor and its descendants or 0 if the descriptor does not
     *         contain a cache duration
     */
    protected long getNearestValidUntil(final Element descriptor) {
        long nearestValidUntil = 0;
        if (!MetadataHelper.isEntitiesDescriptor(descriptor) && !MetadataHelper.isEntityDescriptor(descriptor)) {
            return nearestValidUntil;
        }

        long validUntil;
        List<Element> entitiesDescriptors = ElementSupport.getChildElements(descriptor,
                MetadataHelper.ENTITIES_DESCRIPTOR_NAME);
        for (Element entitiesDescriptor : entitiesDescriptors) {
            validUntil = getNearestValidUntil(entitiesDescriptor);
            if (nearestValidUntil > 0 && validUntil < nearestValidUntil) {
                nearestValidUntil = validUntil;
            }
        }

        List<Element> entityDescriptors = ElementSupport.getChildElements(descriptor,
                MetadataHelper.ENTITY_DESCRIPTOR_NAME);
        for (Element entityDescriptor : entityDescriptors) {
            validUntil = getNearestValidUntil(entityDescriptor);
            if (nearestValidUntil > 0 && validUntil < nearestValidUntil) {
                nearestValidUntil = validUntil;
            }
        }

        Attr validUntilAttr = AttributeSupport.getAttribute(descriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME);
        if (validUntilAttr != null) {
            validUntil = AttributeSupport.getDurationAttributeValueAsLong(validUntilAttr);
            if (nearestValidUntil > 0 && validUntil < nearestValidUntil) {
                nearestValidUntil = validUntil;
            }
            
            descriptor.removeAttributeNode(validUntilAttr);
        }

        return nearestValidUntil;
    }

    /**
     * Chooses the earliest of the two given dates.
     * 
     * @param instant1 first instant, may be null
     * @param instant2 second instant, may be null
     * 
     * @return the earliest of the instants or null if the given instants were null
     */
    protected XMLGregorianCalendar chooseEarliest(XMLGregorianCalendar instant1, XMLGregorianCalendar instant2) {
        if (instant1 == null && instant2 == null) {
            return null;
        }

        if (instant2 == null) {
            return instant1;
        }

        if (instant1.compare(instant2) == DatatypeConstants.LESSER) {
            return instant1;
        }

        return instant2;
    }

    /**
     * Sets the valid until instant on the given descriptor. If the given duration is less than, or equal to, 0 no
     * instant is set. If the given duration is less than {@link #minValidityDuration} then the instant of now + the
     * minimum duration is set. If the given duration is greater than {@link #maxValidityDuration} then the instant of
     * now + the maximum duration is set. Otherwise the instant of now + the given duration is set.
     * 
     * @param descriptor entity or entities descriptor to receive the cache duration, never null
     * @param validityDuration valid duration used to computer the validity instant
     */
    protected void setValidUntil(final Element descriptor, final long validityDuration) {
        if (validityDuration <= 0) {
            return;
        }

        long validUntil;
        if (validityDuration < minValidityDuration) {
            validUntil = System.currentTimeMillis() + maxValidityDuration;
        } else if (validityDuration > maxValidityDuration) {
            validUntil = System.currentTimeMillis() + maxValidityDuration;
        } else {
            validUntil = System.currentTimeMillis() + validityDuration;
        }

        AttributeSupport.appendDateTimeAttribute(descriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME, validUntil);
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        timeConverter = ConverterManager.getInstance();
    }
}