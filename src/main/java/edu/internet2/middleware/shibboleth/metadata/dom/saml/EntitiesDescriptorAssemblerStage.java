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

package edu.internet2.middleware.shibboleth.metadata.dom.saml;

import javax.xml.namespace.QName;

import net.jcip.annotations.ThreadSafe;

import org.opensaml.util.Strings;
import org.opensaml.util.xml.Attributes;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;
import edu.internet2.middleware.shibboleth.metadata.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Stage;

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
     * Constructor.
     * 
     * @param stageId unique stage ID
     */
    public EntitiesDescriptorAssemblerStage(String stageId) {
        super(stageId);
    }

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
    public void setDescriptorName(String name) {
        descriptorName = Strings.trimOrNull(name);
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
    public void setValidityDuration(long duration) {
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
    public void setCacheDuration(long duration) {
        cacheDuration = duration;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> metadataCollection) {
        Element entitiesDescriptor = MetadataHelper.buildEntitiesDescriptor(metadataCollection);

        if (entitiesDescriptor != null) {
            addDescriptorName(entitiesDescriptor);
        }

        SimpleMetadataCollection<DomMetadata> mec = new SimpleMetadataCollection<DomMetadata>();
        mec.add(new DomMetadata(entitiesDescriptor));

        return mec;
    }

    /**
     * Adds the Name attribute to the EntitiesDescriptor. This attribute is added if {@link #descriptorName} is not
     * null.
     * 
     * @param entitiesDescriptor
     */
    protected void addDescriptorName(Element entitiesDescriptor) {
        if (descriptorName != null) {
            Attributes.appendAttribute(entitiesDescriptor, NAME_ATTRIB_NAME, descriptorName);
        }
    }

    /**
     * Adds the validUntil attribute to the EntitiesDescriptor. This attribute is added if {@link #validityDuration} is
     * greater than zero.
     * 
     * @param entitiesDescriptor EntitiesDescriptor element to which the attribute will be added, never null
     */
    protected void addValidUntil(Element entitiesDescriptor) {
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
    protected void addCacheDuration(Element entitiesDescriptor) {
        if (cacheDuration > 0) {
            MetadataHelper.addCacheDuration(entitiesDescriptor, cacheDuration);
        }
    }
}