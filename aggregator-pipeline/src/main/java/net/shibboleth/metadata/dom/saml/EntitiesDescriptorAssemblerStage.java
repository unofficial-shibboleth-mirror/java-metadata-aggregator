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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.BaseStage;

import org.opensaml.util.Assert;
import org.opensaml.util.StringSupport;
import org.opensaml.util.xml.AttributeSupport;
import org.w3c.dom.Element;

/**
 * A {@link net.shibboleth.metadata.pipeline.Stage} capable of assembling a collection of EntityDescriptor elements in
 * to a single EntitiesDescriptor element.
 */
@ThreadSafe
public class EntitiesDescriptorAssemblerStage extends BaseStage<DomMetadata> {

    /** Name of the EntitiesDescriptor's Name attribute. */
    public static final QName NAME_ATTRIB_NAME = new QName("Name");

    /** Strategy used to order a collection of metadata. The default strategy performs no ordering. */
    private MetadataOrderingStrategy orderingStrategy;

    /** Name to use for the EntitiesDescriptor. */
    private String descriptorName;

    /** Constructor. */
    public EntitiesDescriptorAssemblerStage() {
        super();
        orderingStrategy = new NoOpMetadataOrderingStrategy();
    }

    /**
     * Gets the strategy used to order a collection of metadata.
     * 
     * @return strategy used to order a collection of metadata
     */
    public MetadataOrderingStrategy getMetadataOrderingStrategy() {
        return orderingStrategy;
    }

    /**
     * Sets the strategy used to order a collection of metadata.
     * 
     * @param strategy strategy used to order a collection of metadata, never null
     */
    public synchronized void setMetadataOrderingStrategy(MetadataOrderingStrategy strategy) {
        if (isInitialized()) {
            return;
        }
        Assert.isNotNull(strategy, "Metadata ordering strategy may not be null");
        orderingStrategy = strategy;
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
    public synchronized void setDescriptorName(final String name) {
        if (isInitialized()) {
            return;
        }
        descriptorName = StringSupport.trimOrNull(name);
    }

    /** {@inheritDoc} */
    protected void doExecute(final Collection<DomMetadata> metadataCollection) {
        final Element entitiesDescriptor = MetadataHelper.buildEntitiesDescriptor(orderingStrategy
                .order(metadataCollection));

        if (entitiesDescriptor != null) {
            addDescriptorName(entitiesDescriptor);
        }

        final DomMetadata metadata = new DomMetadata(entitiesDescriptor);
        metadataCollection.clear();
        metadataCollection.add(metadata);
    }

    /**
     * Adds the Name attribute to the EntitiesDescriptor. This attribute is added if {@link #descriptorName} is not
     * null.
     * 
     * @param entitiesDescriptor the entity descriptor to which the
     */
    protected void addDescriptorName(final Element entitiesDescriptor) {
        if (descriptorName != null) {
            AttributeSupport.appendAttribute(entitiesDescriptor, NAME_ATTRIB_NAME, descriptorName);
        }
    }

    /**
     * A strategy that defines how to order a {@link MetadataCollection}.
     * 
     * @param <MetadataType> type of metadata elements in the collection
     */
    public static interface MetadataOrderingStrategy {

        /**
         * Orders a given metadata collection.
         * 
         * @param metadata collection of metadata, never null
         * 
         * @return sorted collection of metadata, never null
         */
        public List<DomMetadata> order(Collection<DomMetadata> metadata);
    }

    /** An ordering strategy that simply returns the collection in whatever order it was already in. */
    private class NoOpMetadataOrderingStrategy implements MetadataOrderingStrategy {

        /** {@inheritDoc} */
        public List<DomMetadata> order(Collection<DomMetadata> metadata) {
            return new ArrayList<DomMetadata>(metadata);
        }
    }
}