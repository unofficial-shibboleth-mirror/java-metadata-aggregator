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

package net.shibboleth.metadata.dom.saml;

import javax.xml.namespace.QName;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.SimpleMetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInfo;
import net.shibboleth.metadata.pipeline.Stage;

import org.opensaml.util.StringSupport;
import org.opensaml.util.xml.AttributeSupport;
import org.w3c.dom.Element;


/**
 * A {@link Stage} capable of assembling a collection of EntityDescriptor elements in to a single EntitiesDescriptor
 * element.
 */
@ThreadSafe
public class EntitiesDescriptorAssemblerStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Name of the EntitiesDescriptor's Name attribute. */
    public final static QName NAME_ATTRIB_NAME = new QName("Name");

    /** Name to use for the EntitiesDescriptor. */
    private String descriptorName;

    /** Length of time, in milliseconds, the constructed EntitiesDescriptor is valid. */
    private long validityDuration;

    /** Length of time, in milliseconds, the constructed EntitiesDescriptor should be cached by consumers. */
    private long cacheDuration;

    /**
     * Gets the Name used for the generated descriptor.
     * 
     * @return Name used for the generated descriptor, may be null
     */
    public String getDescriptorName() {
        return descriptorName;
    }

    /**
     * Sets the Name used for the generated descriptor.
     * 
     * @param name Name used for the generated descriptor
     */
    public synchronized void setDescriptorName(final String name) {
        if (isInitialized()) {
            return;
        }
        descriptorName = StringSupport.trimOrNull(name);
    }

    /**
     * Gets the amount of time, in milliseconds, that the produced EntitiesDescriptor is valid.
     * 
     * @return amount of time, in milliseconds, that the produced EntitiesDescriptor is valid
     */
    public long getValidityDuration() {
        return validityDuration;
    }

    /**
     * Sets the amount of time, in milliseconds, that the produced EntitiesDescriptor is valid. A value of 0 or less
     * will result in no validUntil attribute being added to the EntitiesDescriptor.
     * 
     * @param duration amount of time, in milliseconds, that the produced EntitiesDescriptor is valid
     */
    public synchronized void setValidityDuration(final long duration) {
        if (isInitialized()) {
            return;
        }
        validityDuration = duration;
    }

    /**
     * Gets the amount of time, in milliseconds, that the produced EntitiesDescriptor should be cached by the consumer.
     * 
     * @return amount of time, in milliseconds, that the produced EntitiesDescriptor should be cached by the consumer
     */
    public long getCacheDuration() {
        return cacheDuration;
    }

    /**
     * Sets the amount of time, in milliseconds, that the produced EntitiesDescriptor should be cached by the consumer.
     * A value of 0 or less will result in no cacheDuration attribute being added to the EntitiesDescriptor.
     * 
     * @param duration amount of time, in milliseconds, that the produced EntitiesDescriptor should be cached by the
     *            consumer
     */
    public synchronized void setCacheDuration(final long duration) {
        if (isInitialized()) {
            return;
        }
        cacheDuration = duration;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(final MetadataCollection<DomMetadata> metadataCollection) {
        final ComponentInfo compInfo = new ComponentInfo(this);
        
        final Element entitiesDescriptor = MetadataHelper.buildEntitiesDescriptor(metadataCollection);
        if (entitiesDescriptor != null) {
            addDescriptorName(entitiesDescriptor);
        }

        final DomMetadata metadata = new DomMetadata(entitiesDescriptor);
        metadata.getMetadataInfo().put(compInfo);

        final SimpleMetadataCollection<DomMetadata> mec = new SimpleMetadataCollection<DomMetadata>();
        mec.add(new DomMetadata(entitiesDescriptor));

        compInfo.setCompleteInstant();
        return mec;
    }

    /**
     * Adds the Name attribute to the EntitiesDescriptor. This attribute is added if {@link #descriptorName} is not
     * null.
     * 
     * @param entitiesDescriptor
     */
    protected void addDescriptorName(final Element entitiesDescriptor) {
        if (descriptorName != null) {
            AttributeSupport.appendAttribute(entitiesDescriptor, NAME_ATTRIB_NAME, descriptorName);
        }
    }

    /**
     * Adds the validUntil attribute to the EntitiesDescriptor. This attribute is added if {@link #validityDuration} is
     * greater than zero.
     * 
     * @param entitiesDescriptor EntitiesDescriptor element to which the attribute will be added, never null
     */
    protected void addValidUntil(final Element entitiesDescriptor) {
        if (validityDuration > 0) {
            MetadataHelper.addValidUntil(entitiesDescriptor, validityDuration);
        }
    }

    /**
     * Adds the cacheDuration attribute to the EntitiesDescriptor. This attribute is added if {@link #cacheDuration} is
     * greater than zero.
     * 
     * @param entitiesDescriptor EntitiesDescriptor element to which the attribute will be added, never null
     */
    protected void addCacheDuration(final Element entitiesDescriptor) {
        if (cacheDuration > 0) {
            MetadataHelper.addCacheDuration(entitiesDescriptor, cacheDuration);
        }
    }
}