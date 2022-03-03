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
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.metadata.dom.saml.SAMLSupport;
import net.shibboleth.metadata.dom.saml.mdrpi.RegistrationAuthority;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/**
 * A stage which filters entity attributes from entity definitions according to a supplied
 * set of rules.
 * 
 * For each attribute value under consideration, a {@link EntityAttributeContext} is built
 * from the components of the attribute and the entity's <code>registrationAuthority</code>,
 * if any.
 * 
 * Note that the <code>registrationAuthority</code> to be used is assumed to have been
 * extracted out into a {@link RegistrationAuthority} object in the entity's item metadata.
 * 
 * The stage can be operated in a whitelisting mode (the default) or in a blacklisting mode
 * by setting the <code>whitelisting</code> property to <code>false</code>.
 *
 * @since 0.9.0
 */
@ThreadSafe
public class EntityAttributeFilteringStage extends AbstractIteratingStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntityAttributeFilteringStage.class);

    /**
     * An entity attribute context against which matches can take place. It consists
     * of the attribute's value, <code>Name</code> and <code>NameFormat</code> attributes,
     * and the entity's <code>registrationAuthority</code>, if any.
     * 
     * A matcher is a {@link Predicate} over such a context.
     */
    public interface EntityAttributeContext {

        /**
         * Returns the registration authority component, or <code>null</code>.
         * 
         * @return the registration authority, or <code>null</code>
         */
        @Nullable
        String getRegistrationAuthority();
        
        /**
         * Returns the attribute's <code>NameFormat</code>.
         * 
         * @return the attribute's <code>NameFormat</code>.
         */
        @Nonnull
        String getNameFormat();
        
        /**
         * Returns the attribute's <code>Name</code>.
         * 
         * @return the attribute's <code>Name</code>
         */
        @Nonnull
        String getName();
        
        /**
         * Returns the attribute's value.
         * 
         * @return the attribute's value
         */
        @Nonnull
        String getValue();
        
    }

    /**
     * A simple immutable implementation of {@link EntityAttributeContext}.
     */
    @Immutable
    static class ContextImpl implements EntityAttributeContext {

        /** The attribute's value. */
        @Nonnull
        private final String value;
        
        /** The attribute's <code>Name</code>. */
        @Nonnull
        private final String name;
        
        /** The attribute's <code>NameFormat</code>. */
        @Nonnull
        private final String nameFormat;
        
        /** The entity's registration authority, or <code>null</code>. */
        @Nullable
        private final String registrationAuthority;
        
        /**
         * Constructor.
         * 
         * @param attributeValue attribute value
         * @param attributeName attribute <code>Name</code>
         * @param attributeNameFormat attribute <code>NameFormat</code>
         * @param registrar entity's registration authority, or <code>null</code>
         */
        public ContextImpl(@Nonnull final String attributeValue,
                @Nonnull final String attributeName,
                @Nonnull final String attributeNameFormat,
                @Nullable final String registrar) {
            value = Constraint.isNotNull(attributeValue, "value may not be null");
            name = Constraint.isNotNull(attributeName, "name may not be null");
            nameFormat = Constraint.isNotNull(attributeNameFormat, "name format may not be null");
            registrationAuthority = registrar;
        }
        
        /**
         * Shorthand three-argument constructor.
         * 
         * @param attributeValue attribute value
         * @param attributeName attribute <code>Name</code>
         * @param attributeNameFormat attribute <code>NameFormat</code>
         */
        public ContextImpl(@Nonnull final String attributeValue,
                @Nonnull final String attributeName,
                @Nonnull final String attributeNameFormat) {
            this(attributeValue, attributeName, attributeNameFormat, null);
        }    

        @Override
        public String getRegistrationAuthority() {
            return registrationAuthority;
        }

        @Override
        public String getNameFormat() {
            return nameFormat;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            final StringBuilder b = new StringBuilder();
            b.append("{v=").append(getValue());
            b.append(", n=").append(getName());
            b.append(", f=").append(getNameFormat());
            b.append(", r=");
            if (getRegistrationAuthority() == null) {
                b.append("(none)");
            } else {
                b.append(getRegistrationAuthority());
            }
            b.append('}');
            return b.toString();
        }
    }

    /**
     * List of matching rules to apply to each attribute value. The list is applied in
     * order, with the first rule returning <code>true</code> terminating the evaluation.
     * This amounts to an implicit ORing of the individual rules, with early
     * termination.
     */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Predicate<EntityAttributeContext>> rules = List.of();

    /** Mode of operation: whitelisting or blacklisting. Default: whitelisting. */
    @GuardedBy("this") private boolean whitelisting = true;

    /** Whether we add status metadata to the item when entity attributes are removed. Default: no. */
    @GuardedBy("this") private boolean recordingRemovals;

    /**
     * Sets the {@link List} of rules to be used to match attribute values.
     * 
     * @param newRules new {@link List} of rules
     */
    public synchronized void setRules(
            @Nonnull @NonnullElements @Unmodifiable final List<Predicate<EntityAttributeContext>> newRules) {
        throwSetterPreconditionExceptions();
        rules = List.copyOf(Constraint.isNotNull(newRules, "rules property may not be null"));
    }
    
    /**
     * Returns the {@link List} of rules being used to match entity attributes.
     * 
     * @return the {@link List} of rules
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized List<Predicate<EntityAttributeContext>> getRules() {
        return rules;
    }
    
    /**
     * Sets the mode of operation.
     * 
     * @param newValue <code>true</code> to whitelist (default),
     *                 <code>false</code> to blacklist
     */
    public synchronized void setWhitelisting(final boolean newValue) {
        throwSetterPreconditionExceptions();
        whitelisting = newValue;
    }
    
    /**
     * Indicates whether the stage is set to whitelisting or blacklisting mode.
     * 
     * @return <code>true</code> if whitelisting (default),
     *         <code>false</code> if blacklisting
     */
    public final synchronized boolean isWhitelisting() {
        return whitelisting;
    }

    /**
     * Set whether to record removed entity attributes.
     *
     * @param newValue whether to remove recorded entity attributes
     */
    public synchronized void setRecordingRemovals(final boolean newValue) {
        throwSetterPreconditionExceptions();
        recordingRemovals = newValue;
    }

    /**
     * Indicates whether the stage will record removed entity attributes.
     *
     * @return <code>true</code> if recording,
     *         <code>false</code> if not (default)
     */
    public synchronized boolean isRecordingRemovals() {
        return recordingRemovals;
    }

    /**
     * Apply the rules to a context.
     * 
     * @param ctx the context to apply the rules to
     * @return <code>true</code> if one of the rules returns <code>true</code>;
     *  otherwise <code>false</code>
     */
    private boolean applyRules(final EntityAttributeContext ctx) {
        for (final Predicate<EntityAttributeContext> rule : getRules()) {
            if (rule.test(ctx)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extract the registration authority for an entity from its entity metadata.
     * 
     * @param item the {@link Item} representing the entity
     * @return the registration authority URI, or <code>null</code> if not present
     */
    private String extractRegistrationAuthority(@Nonnull final Item<Element> item) {
        final List<RegistrationAuthority> regAuthList = item.getItemMetadata().get(RegistrationAuthority.class);
        if (regAuthList.isEmpty()) {
            return null;
        }
        return regAuthList.get(0).getRegistrationAuthority();
    }
    
    /**
     * Filter an <code>Attribute</code> element.
     * 
     * @param attribute an <code>Attribute</code> element to filter
     * @param registrationAuthority the registration authority associated with the entity
     * @param item the {@link Item} representing the entity
     */
    private void filterAttribute(@Nonnull final Element attribute, @Nullable final String registrationAuthority,
            @Nonnull final Item<Element> item) {
        // Determine the attribute's name; this will default to the empty string if not present
        final String attributeName = attribute.getAttribute("Name");
        
        // Determine the attribute's NameFormat
        final String attributeNameFormat = SAMLSupport.extractAttributeNameFormat(attribute);
        
        // Locate the AttributeValue elements to filter
        final List<Element> attributeValues =
                ElementSupport.getChildElements(attribute, SAMLSupport.ATTRIBUTE_VALUE_NAME);
        
        // Filter each AttributeValue in turn
        for (final Element value : attributeValues) {
            final String attributeValue = value.getTextContent();

            // Construct an entity attribute context to be matched against
            final EntityAttributeContext ctx =
                    new ContextImpl(attributeValue, attributeName,
                            attributeNameFormat, registrationAuthority);            
            final boolean matched = applyRules(ctx);
            if (matched ^ isWhitelisting()) {
                log.debug("removing {}", ctx);
                if (isRecordingRemovals()) {
                    item.getItemMetadata().put(new WarningStatus(getId(),
                            "removing '" + ctx.getName() + "' = '" + ctx.getValue() + "'"));
                }
                attribute.removeChild(value);
            }
        }
    }
    
    /**
     * Filter an <code>EntityAttributes</code> extension element.
     * 
     * @param entityAttributes the <code>EntityAttributes</code> extension element
     * @param registrationAuthority the registration authority associated with the entity
     * @param item the {@link Item} representing the entity
     */
    private void filterEntityAttributes(@Nonnull final Element entityAttributes,
            @Nullable final String registrationAuthority,
            @Nonnull final Item<Element> item) {
        // Locate the Attribute elements to filter
        final List<Element> attributes =
                ElementSupport.getChildElements(entityAttributes, SAMLSupport.ATTRIBUTE_NAME);
        
        // Filter each Attribute in turn
        for (final Element attribute : attributes) {
            filterAttribute(attribute, registrationAuthority, item);
            
            // remove the Attribute container if it is now empty
            if (ElementSupport.getFirstChildElement(attribute) == null) {
                log.debug("removing empty Attribute");
                entityAttributes.removeChild(attribute);
            }
        }
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) {
        final Element entity = item.unwrap();

        // Establish the item's registrationAuthority, if any
        final String registrationAuthority = extractRegistrationAuthority(item);

        /*
         * Process each EntityAttributes container independently. There MUST be only one
         * such container according to the specification, but we can't count on that being
         * picked up elsewhere as it isn't a schema constraint.
         */
        for (final Element entityAttributes : SAMLMetadataSupport.getDescriptorExtensionList(entity,
                MDAttrSupport.ENTITY_ATTRIBUTES_NAME)) {
            filterEntityAttributes(entityAttributes, registrationAuthority, item);

            // remove the EntityAttributes container if it is now empty
            if (ElementSupport.getFirstChildElement(entityAttributes) == null) {
                log.debug("removing empty EntityAttributes");
                final Node extensions = entityAttributes.getParentNode();
                extensions.removeChild(entityAttributes);
            }
        }
    }

}
