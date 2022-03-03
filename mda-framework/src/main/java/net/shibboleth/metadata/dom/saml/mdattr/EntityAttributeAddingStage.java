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

package net.shibboleth.metadata.dom.saml.mdattr;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.Container;
import net.shibboleth.metadata.dom.saml.AttributeElementMaker;
import net.shibboleth.metadata.dom.saml.AttributeElementMatcher;
import net.shibboleth.metadata.dom.saml.AttributeValueElementMaker;
import net.shibboleth.metadata.dom.saml.AttributeValueElementMatcher;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.metadata.dom.saml.SAMLSupport;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A stage which adds entity attribute values to entity definitions.
 *
 * @since 0.10.0
 */
@ThreadSafe
public class EntityAttributeAddingStage extends AbstractStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntityAttributeAddingStage.class);

    /**
     * The <code>Name</code> of the attribute to be added.
     * 
     * The default value is for an entity attribute specifying an entity category.
     */
    @Nonnull @GuardedBy("this")
    private String attributeName = EntityCategorySupport.EC_CATEGORY_ATTR_NAME;

    /**
     * The <code>NameFormat</code> of the attribute to be added.
     * 
     * The default value is suitable for most entity attributes.
     */
    @Nonnull @GuardedBy("this")
    private String attributeNameFormat = EntityCategorySupport.EC_ATTR_NAME_FORMAT;

    /** The value of the attribute to be added. */
    @NonnullAfterInit @GuardedBy("this")
    private String attributeValue;

    /**
     * Whether we add <code>mdattr:EntityAttributes</code> as the first child
     * of <code>md:Extensions</code> if not already present.
     * 
     * Default: <code>false</code> (add as last child).
     */
    @GuardedBy("this") private boolean addingFirstChild;

    /**
     * Returns the attribute name.
     * 
     * @return the attributeName
     */
    @Nonnull
    public final synchronized String getAttributeName() {
        return attributeName;
    }

    /**
     * Sets the attribute name.
     * 
     * @param name the attributeName to set
     */
    public synchronized void setAttributeName(@Nonnull final String name) {
        throwSetterPreconditionExceptions();
        attributeName = Constraint.isNotNull(name, "attributeName must not be null");
    }

    /**
     * Gets the attribute name format.
     * 
     * @return the attributeNameFormat
     */
    @Nonnull
    public final synchronized String getAttributeNameFormat() {
        return attributeNameFormat;
    }

    /**
     * Sets the attribute name format.
     * 
     * @param nameFormat the attributeNameFormat to set
     */
    public synchronized void setAttributeNameFormat(@Nonnull final String nameFormat) {
        throwSetterPreconditionExceptions();
        attributeNameFormat = Constraint.isNotNull(nameFormat, "attributeNameFormat must not be null");
    }

    /**
     * Gets the attribute value.
     * 
     * @return the attributeValue
     */
    @Nonnull
    public final synchronized String getAttributeValue() {
        return attributeValue;
    }

    /**
     * Sets the attribute value.
     * 
     * @param value the attributeValue to set
     */
    public synchronized void setAttributeValue(@Nonnull final String value) {
        throwSetterPreconditionExceptions();
        attributeValue = Constraint.isNotNull(value, "attributeValue must not be null");
    }

    /**
     * Get whether we are adding <code>mdattr:EntityAttributes</code> as the first child
     * of <code>md:Extensions</code> if not already present.
     * 
     * @return <code>true</code> if adding as the first child, <code>false</code> if the last
     */
    public final synchronized boolean isAddingFirstChild() {
        return addingFirstChild;
    }

    /**
     * Sets whether to add <code>mdattr:EntityAttributes</code> as the first child
     * of <code>md:Extensions</code> if not already present.
     * 
     * @param addFirst <code>true</code> to add as the first child, <code>false</code> as the last
     */
    public synchronized void setAddingFirstChild(final boolean addFirst) {
        throwSetterPreconditionExceptions();
        addingFirstChild = addFirst;
    }

    /**
     * Looks for the attribute value we want to add within the contents of a list of
     * Attribute container elements.
     * 
     * @param attributes {@link List} of Attribute {@link Container}s
     * @param matcher {@link Predicate} that matches the {@link Element} we are looking for
     * @return true iff the value appears somewhere in the list of containers
     */
    private boolean attributeValuePresent(@Nonnull final List<Container> attributes,
            @Nonnull final Predicate<Element> matcher) {
        for (final Container attribute : attributes) {
            if (attribute.findChild(matcher) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<Element>> items) {
        final var name = getAttributeName();
        final var format = getAttributeNameFormat();
        final var attributeMatcher = new AttributeElementMatcher(name, format);
        final var attributeMaker = new AttributeElementMaker(name, format);

        final var value = getAttributeValue();
        final var attributeValueMatcher = new AttributeValueElementMatcher(value);
        final var attributeValueMaker = new AttributeValueElementMaker(value);

        for (final var item : items) {
            final Element entity = item.unwrap();
    
            if (SAMLMetadataSupport.isEntityDescriptor(entity)) {
                // Start from the entity
                final Container entityContainer = new Container(entity);
    
                // Dig down to <Extensions>
                final Container extensionsContainer =
                        entityContainer.locateChild(SAMLSupport.EXTENSIONS_MATCHER,
                                SAMLSupport.EXTENSIONS_MAKER, Container.FIRST_CHILD);
    
                // Dig down to <EntityAttributes>
                final Container attributesContainer =
                        extensionsContainer.locateChild(MDAttrSupport.ENTITY_ATTRIBUTES_MATCHER,
                                MDAttrSupport.ENTITY_ATTRIBUTES_MAKER,
                                isAddingFirstChild() ? Container.FIRST_CHILD : Container.LAST_CHILD);
    
                // Collect all matching <Attribute> containers
                final List<Container> attributes =
                        attributesContainer.findChildren(attributeMatcher);
    
                // If any of the existing attribute values match our value, we're done
                if (attributeValuePresent(attributes, attributeValueMatcher)) {
                    log.debug("attribute value '{}' already present", attributeValue);
                    return;
                }
    
                // If not already present, re-locate an <Attribute> and add it in there.
                final Container attribute =
                        attributesContainer.locateChild(attributeMatcher, attributeMaker, Container.LAST_CHILD);
                attribute.addChild(attributeValueMaker, Container.LAST_CHILD);
            }
        }
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (attributeValue == null) {
            throw new ComponentInitializationException("attributeValue property must be supplied");
        }

    }

}
