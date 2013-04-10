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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import net.shibboleth.utilities.java.support.xml.NamespaceSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link net.shibboleth.metadata.pipeline.Stage} capable of assembling a collection of EntityDescriptor elements in
 * to a single EntitiesDescriptor element.
 */
@ThreadSafe
public class EntitiesDescriptorAssemblerStage extends BaseStage<DomElementItem> {

    /** Name of the EntitiesDescriptor's Name attribute. */
    public static final QName NAME_ATTRIB_NAME = new QName("Name");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntitiesDescriptorAssemblerStage.class);

    /**
     * Whether attempting to turn an empty item collection, which would result in a schema-invalid childless
     * EntitiesDescriptor, should be treated as processing error. Default value: <code>false</code>
     */
    private boolean noChildrenAProcessingError;

    /** Strategy used to order a collection of Items. The default strategy performs no ordering. */
    private ItemOrderingStrategy orderingStrategy = new NoOpItemOrderingStrategy();

    /** Name to use for the EntitiesDescriptor. */
    private String descriptorName;

    /**
     * Gets whether attempting to turn an empty item collection, which would result in a schema-invalid childless
     * EntitiesDescriptor, should be treated as processing error.
     * 
     * @return whether attempting to turn an empty item collection should be treated as processing error
     */
    public boolean isNoChildrenAProcessingError() {
        return noChildrenAProcessingError;
    }

    /**
     * Sets whether attempting to turn an empty item collection, which would result in a schema-invalid childless
     * EntitiesDescriptor, should be treated as processing error.
     * 
     * @param isError whether attempting to turn an empty item collection should be treated as processing error
     */
    public synchronized void setNoChildrenAProcessingError(boolean isError) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        noChildrenAProcessingError = isError;
    }

    /**
     * Gets the strategy used to order a collection of Items.
     * 
     * @return strategy used to order a collection of Items
     */
    @Nonnull public ItemOrderingStrategy getItemOrderingStrategy() {
        return orderingStrategy;
    }

    /**
     * Sets the strategy used to order a collection of Items.
     * 
     * @param strategy strategy used to order a collection of Items
     */
    public synchronized void setItemOrderingStrategy(@Nonnull final ItemOrderingStrategy strategy) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        orderingStrategy = Constraint.isNotNull(strategy, "Item ordering strategy can not be null");
    }

    /**
     * Gets the Name used for the generated descriptor.
     * 
     * @return Name used for the generated descriptor, may be null
     */
    @Nullable public String getDescriptorName() {
        return descriptorName;
    }

    /**
     * Sets the Name used for the generated descriptor.
     * 
     * @param name Name used for the generated descriptor
     */
    public synchronized void setDescriptorName(@Nullable final String name) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        descriptorName = StringSupport.trimOrNull(name);
    }

    /** {@inheritDoc} */
    protected void doExecute(@Nonnull @NonnullElements final Collection<DomElementItem> itemCollection)
            throws StageProcessingException {
        if (itemCollection.isEmpty()) {
            if (noChildrenAProcessingError) {
                throw new StageProcessingException("Unable to assemble EntitiesDescriptor from an empty collection");
            } else {
                log.debug("Unable to assemble EntitiesDescriptor from an empty collection");
                return;
            }
        }

        final DOMImplementation domImpl =
                itemCollection.iterator().next().unwrap().getOwnerDocument().getImplementation();
        final Document entitiesDescriptorDocument = domImpl.createDocument(null, null, null);

        final Element entitiesDescriptor =
                ElementSupport.constructElement(entitiesDescriptorDocument,
                        SamlMetadataSupport.ENTITIES_DESCRIPTOR_NAME);
        NamespaceSupport.appendNamespaceDeclaration(entitiesDescriptor,
                entitiesDescriptor.getNamespaceURI(), entitiesDescriptor.getPrefix());
        entitiesDescriptorDocument.appendChild(entitiesDescriptor);
        addDescriptorName(entitiesDescriptor);

        // Put a newline between the start and end tags
        ElementSupport.appendTextContent(entitiesDescriptor, "\n");

        List<DomElementItem> orderedItems = orderingStrategy.order(itemCollection);
        Element descriptor;
        for (DomElementItem item : orderedItems) {
            descriptor = item.unwrap();
            if (SamlMetadataSupport.isEntityOrEntitiesDescriptor(descriptor)) {
                descriptor = (Element) entitiesDescriptorDocument.importNode(descriptor, true);
                entitiesDescriptor.appendChild(descriptor);

                // Put a newline after every child descriptor element
                ElementSupport.appendTextContent(entitiesDescriptor, "\n");
            }
        }

        final DomElementItem item = new DomElementItem(entitiesDescriptorDocument);
        itemCollection.clear();
        itemCollection.add(item);
    }

    /**
     * Adds the Name attribute to the EntitiesDescriptor. This attribute is added if {@link #descriptorName} is not
     * null.
     * 
     * @param entitiesDescriptor the entity descriptor to which the
     */
    protected void addDescriptorName(@Nonnull final Element entitiesDescriptor) {
        if (descriptorName != null) {
            AttributeSupport.appendAttribute(entitiesDescriptor, NAME_ATTRIB_NAME, descriptorName);
        }
    }

    /** {@inheritDoc} */
    protected void doDestroy() {
        orderingStrategy = null;
        descriptorName = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (orderingStrategy == null) {
            orderingStrategy = new NoOpItemOrderingStrategy();
        }
    }

    /** A strategy that defines how to order a {@link net.shibboleth.metadata.Item} collection. */
    public static interface ItemOrderingStrategy {

        /**
         * Orders a given Item collection.
         * 
         * @param items collection of Item, never null
         * 
         * @return sorted collection of Item, never null
         */
        public List<DomElementItem> order(@Nonnull @NonnullElements final Collection<DomElementItem> items);
    }

    /** An ordering strategy that simply returns the collection in whatever order it was already in. */
    private class NoOpItemOrderingStrategy implements ItemOrderingStrategy {

        /** {@inheritDoc} */
        public List<DomElementItem> order(@Nonnull @NonnullElements final Collection<DomElementItem> items) {
            return new ArrayList<DomElementItem>(items);
        }
    }
}