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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

/**
 * Filtering stage that removes ContactPerson elements from EntityDescriptors.
 * 
 * <p>
 * Note, only the values {@link #TECHNICAL}, {@link #SUPPORT}, {@link #ADMINISTRATIVE}, {@link #BILLING}, and
 * {@link #OTHER} are valid contact person types. Attempting to designate a type other than these will result in that
 * type being ignored. <code>ContactPerson</code> elements which do not contain the required <code>contactType</code>
 * attribute are always removed.
 * </p>
 * <p>
 * To remove all contact persons enable type whitelisting and provide an empty designated type set.
 * </p>
 */
@ThreadSafe
public class ContactPersonFilterStage extends AbstractIteratingStage<Element> {

    /** 'technical' person type constant. */
    public static final String TECHNICAL = "technical";

    /** 'support' person type constant. */
    public static final String SUPPORT = "support";

    /** 'administrative' person type constant. */
    public static final String ADMINISTRATIVE = "administrative";

    /** 'billing' person type constant. */
    public static final String BILLING = "billing";

    /** 'other' person type constant. */
    public static final String OTHER = "other";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ContactPersonFilterStage.class);

    /** Allowed contact person types. */
    @Nonnull @NonnullElements @Unmodifiable
    private final Set<String> allowedTypes = Set.of(TECHNICAL, SUPPORT, ADMINISTRATIVE, BILLING, OTHER);

    /** Person types which are white/black listed depending on the value of {@link #whitelistingTypes}. */
    @Nonnull @NonnullElements @Unmodifiable
    private Set<String> designatedTypes = Set.copyOf(allowedTypes);

    /** Whether {@link #designatedTypes} should be considered a whitelist. Default value: true */
    private boolean whitelistingTypes = true;

    /**
     * Gets the list of designated person types.
     * 
     * @return list of designated person types
     */
    @Nonnull @NonnullElements @Unmodifiable
    public Collection<String> getDesignateTypes() {
        return designatedTypes;
    }

    /**
     * Sets the designated entity roles. The collection may contain either role element names or schema types.
     * 
     * @param types collection of designated entity roles
     */
    public synchronized void setDesignatedTypes(
            @Nonnull @NonnullElements @Unmodifiable final Collection<String> types) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        final Set<String> checkedTypes = new HashSet<>();
        for (final String type : types) {
            if (allowedTypes.contains(type)) {
                checkedTypes.add(type);
            } else {
                log.debug("Stage {}: {} is not an allowed contact person type and so has been ignored", getId(),
                        type);
            }
        }

        designatedTypes = Set.copyOf(checkedTypes);
    }

    /**
     * Gets whether the list of designated roles should be considered a whitelist.
     * 
     * @return true if the designated roles should be considered a whitelist, false otherwise
     */
    public boolean isWhitelistingTypes() {
        return whitelistingTypes;
    }

    /**
     * Sets whether the list of designated roles should be considered a whitelist.
     * 
     * @param whitelisting true if the designated entities should be considered a whitelist, false otherwise
     */
    public synchronized void setWhitelistingTypes(final boolean whitelisting) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        whitelistingTypes = whitelisting;
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element descriptor = item.unwrap();
        if (SAMLMetadataSupport.isEntitiesDescriptor(descriptor)) {
            processEntitiesDescriptor(descriptor);
        } else if (SAMLMetadataSupport.isEntityDescriptor(descriptor)) {
            processEntityDescriptor(descriptor);
        }
    }

    /**
     * Iterates over all child EntitiesDescriptor, passing each to {@link #processEntitiesDescriptor(Element)}, and
     * EntityDescriptor, passing each to {@link #processEntityDescriptor(Element)}.
     * 
     * @param entitiesDescriptor EntitiesDescriptor being processed
     */
    protected void processEntitiesDescriptor(@Nonnull final Element entitiesDescriptor) {
        final List<Element> children = ElementSupport.getChildElements(entitiesDescriptor);
        for (final Element child : children) {
            if (SAMLMetadataSupport.isEntitiesDescriptor(child)) {
                processEntitiesDescriptor(child);
            } else if (SAMLMetadataSupport.isEntityDescriptor(child)) {
                processEntityDescriptor(child);
            }
        }
    }

    /**
     * Processes an EntityDescriptor by removing any ContactPerson that is within it.
     * 
     * @param entityDescriptor entity descriptor being processed
     */
    protected void processEntityDescriptor(@Nonnull final Element entityDescriptor) {
        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");

        final List<Element> contactPersons =
                ElementSupport
                        .getChildElementsByTagNameNS(entityDescriptor, SAMLMetadataSupport.MD_NS, "ContactPerson");
        if (!contactPersons.isEmpty()) {
            log.debug("{} pipeline stage filtering ContactPerson from EntityDescriptor {}", getId(), entityId);
            for (final Element contactPerson : contactPersons) {
                if (!isRetainedContactPersonType(contactPerson)) {
                    entityDescriptor.removeChild(contactPerson);
                }
            }
        }
    }

    /**
     * Check whether the given contact person is designated as a type that is to be retained or not.
     * 
     * @param contactPerson the contact person
     * 
     * @return true if the contact person should be retained, false otherwise
     */
    protected boolean isRetainedContactPersonType(@Nonnull final Element contactPerson) {
        Constraint.isNotNull(contactPerson, "Contact person element can not be null");

        final String type =
                StringSupport.trimOrNull(AttributeSupport.getAttributeValue(contactPerson, null, "contactType"));

        if (type == null) {
            log.debug(
                    "The following ContactPerson does not contain the required contactType attribute, " +
                            "it will be removed:\n{}",
                    SerializeSupport.prettyPrintXML(contactPerson));
            return false;
        }

        if (!allowedTypes.contains(type)) {
            log.debug("The following ContactPerson contained an invalid contactType, it will be removed:\n{}",
                    SerializeSupport.prettyPrintXML(contactPerson));
            return false;
        }

        if (isWhitelistingTypes() && designatedTypes.contains(type)) {
            // if we're whitelisting types and the person's type appears in the designated type list, keep them
            return true;
        }

        if (!isWhitelistingTypes() && !designatedTypes.contains(type)) {
            // if we're blacklisting types and the person's type does not appear in the designated type list, keep them
            return true;
        }

        // otherwise boot them
        return false;
    }

    @Override
    protected void doDestroy() {
        designatedTypes = null;
        super.doDestroy();
    }
}
