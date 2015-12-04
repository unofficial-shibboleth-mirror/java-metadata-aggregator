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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.metadata.dom.saml.SAMLSupport;
import net.shibboleth.metadata.dom.saml.mdrpi.RegistrationAuthority;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Predicate;

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
 */
public class EntityAttributeFilteringStage extends BaseStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntityAttributeFilteringStage.class);

    /**
     * List of matching rules to apply to each attribute value. The list is applied in
     * order, with the first rule returning <code>true</code> terminating the evaluation.
     * This amounts to an implicit ORing of the individual rules, with early
     * termination.
     */
    private List<Predicate<EntityAttributeContext>> rules = Collections.emptyList();

    /** Mode of operation: whitelisting or blacklisting. Default: whitelisting. */
    private boolean whitelisting = true;
    
    /**
     * Sets the {@link List} of rules to be used to match attribute values.
     * 
     * @param newRules new {@link List} of rules
     */
    public void setRules(@Nonnull final List<Predicate<EntityAttributeContext>> newRules) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        rules = Constraint.isNotNull(newRules, "rules property may not be null");
    }
    
    /**
     * Returns the {@link List} of rules being used to match entity attributes.
     * 
     * @return the {@link List} of rules
     */
    @Nonnull
    public List<Predicate<EntityAttributeContext>> getRules() {
        return Collections.unmodifiableList(rules);
    }
    
    /**
     * Sets the mode of operation.
     * 
     * @param newValue <code>true</code> to whitelist (default),
     *                 <code>false</code> to blacklist
     */
    public void setWhitelisting(final boolean newValue) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        whitelisting = newValue;
    }
    
    /**
     * Indicates whether the stage is set to whitelisting or blacklisting mode.
     * 
     * @return <code>true</code> if whitelisting (default),
     *         <code>false</code> if blacklisting
     */
    public boolean isWhitelisting() {
        return whitelisting;
    }
    
    /**
     * Apply the rules to a context.
     * 
     * @param ctx the context to apply the rules to
     * @return <code>true</code> if one of the rules returns <code>true</code>;
     *  otherwise <code>false</code>
     */
    private boolean applyRules(final EntityAttributeContext ctx) {
        for (final Predicate<EntityAttributeContext> rule : rules) {
            if (rule.apply(ctx)) {
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
        } else {
            return regAuthList.get(0).getRegistrationAuthority();
        }
    }
    
    /**
     * Filter an <code>Attribute</code> element.
     * 
     * @param attribute an <code>Attribute</code> element to filter
     * @param registrationAuthority the registration authority associated with the entity
     */
    private void filterAttribute(@Nonnull final Element attribute, @Nullable final String registrationAuthority) {
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
                    new SimpleEntityAttributeContext(attributeValue, attributeName,
                            attributeNameFormat, registrationAuthority);            
            final boolean matched = applyRules(ctx);
            if (matched ^ whitelisting) {
                log.debug("removing {}", ctx);
                attribute.removeChild(value);
            }
        }
    }
    
    /**
     * Filter an <code>EntityAttributes</code> extension element.
     * 
     * @param entityAttributes the <code>EntityAttributes</code> extension element
     * @param registrationAuthority the registration authority associated with the entity
     */
    private void filterEntityAttributes(@Nonnull final Element entityAttributes,
            @Nullable final String registrationAuthority) {
        // Locate the Attribute elements to filter
        final List<Element> attributes =
                ElementSupport.getChildElements(entityAttributes, SAMLSupport.ATTRIBUTE_NAME);
        
        // Filter each Attribute in turn
        for (final Element attribute : attributes) {
            filterAttribute(attribute, registrationAuthority);
            
            // remove the Attribute container if it is now empty
            if (ElementSupport.getFirstChildElement(attribute) == null) {
                log.debug("removing empty Attribute");
                entityAttributes.removeChild(attribute);
            }
        }
    }
    
    @Override
    protected void doExecute(final Collection<Item<Element>> itemCollection) throws StageProcessingException {
        for (final Item<Element> item : itemCollection) {
            final Element entity = item.unwrap();
            
            // Establish the item's registrationAuthority, if any
            final String registrationAuthority = extractRegistrationAuthority(item);

            // Locate mdattr:EntityAttributes element
            final Element entityAttributes = SAMLMetadataSupport.getDescriptorExtensions(entity,
                    MDAttrSupport.ENTITY_ATTRIBUTES_NAME);
            if (entityAttributes != null) {
                filterEntityAttributes(entityAttributes, registrationAuthority);
                
                // remove the EntityAttributes container if it is now empty
                if (ElementSupport.getFirstChildElement(entityAttributes) == null) {
                    log.debug("removing empty EntityAttributes");
                    final Node extensions = entityAttributes.getParentNode();
                    extensions.removeChild(entityAttributes);
                }
            }
        }
    }

    @Override
    protected void doDestroy() {
        rules = null;

        super.doDestroy();
    }

}
