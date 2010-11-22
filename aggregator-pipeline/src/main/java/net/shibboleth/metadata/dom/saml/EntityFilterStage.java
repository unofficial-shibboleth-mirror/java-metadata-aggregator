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
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInfo;
import net.shibboleth.metadata.pipeline.Stage;

import org.opensaml.util.ObjectSupport;
import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazySet;
import org.opensaml.util.xml.DomTypeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.opensaml.util.xml.QNameSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


/** A pipeline stage that will remove SAML EntityDescriptior elements which do meet specified filtering criteria. */
@ThreadSafe
public class EntityFilterStage extends AbstractComponent implements Stage<DomMetadata> {

    /** QName of the RoleDescriptor element. */
    public static final QName ROLE_DESCRIPTOR_NAME = new QName(MetadataHelper.MD_NS, "RoleDescriptor");

    /** QName of the IDPSSODescriptor element. */
    public static final QName IDP_SSO_DESCRIPTOR_NAME = new QName(MetadataHelper.MD_NS, "IDPSSODescriptor");

    /** QName of the SPSSODescriptor element. */
    public static final QName SP_SSO_DESCRIPTOR_NAME = new QName(MetadataHelper.MD_NS, "SPSSODescriptor");

    /** QName of the AuthnAuthorityDescriptor element. */
    public static final QName AUTHN_AUTHORITY_DESCRIPTOR_NAME = new QName(MetadataHelper.MD_NS,
            "AuthnAuthorityDescriptor");

    /** QName of the AttributeAuthorityDescriptor element. */
    public static final QName ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME = new QName(MetadataHelper.MD_NS,
            "AttributeAuthorityDescriptor");

    /** QName of the PDPDescriptor element. */
    public static final QName PDP_DESCRIPTOR_NAME = new QName(MetadataHelper.MD_NS, "PDPDescriptor");

    /** QName of the Organization element. */
    private static final QName ORGANIZTION_NAME = new QName(MetadataHelper.MD_NS, "Organization");

    /** QName of the ContactPerson element. */
    private static final QName CONTACT_PERSON_NAME = new QName(MetadataHelper.MD_NS, "ContactPerson");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntityFilterStage.class);

    // TODO white/blacklist bindings, extensions

    /** Entities which are white/black listed depending on the value of {@link #whitelistingEntities}. */
    private Collection<String> designatedEntities = new LazySet<String>();

    /** Whether {@link #designatedEntities} should be considered a whitelist or a blacklist. Default value: false */
    private boolean whitelistingEntities;

    /** Role element or type names which are white/black listed depending on the value of {@link #whitelistingRoles}. */
    private Collection<QName> designatedRoles = new LazySet<QName>();

    /** Whether {@link #designatedRoles} should be considered a whitelist or a blacklist. Default value: false */
    private boolean whitelistingRoles;

    /**
     * Whether EntityDescriptor metadata elements that do not contain roles, after filtering, should be removed. Default
     * value: true
     */
    private boolean removeRolelessEntities = true;

    /** Indicates that Organization elements should be removed from EntityDescriptors. Default value: true */
    private boolean removeOrganization = true;

    /** Indicates that ContactPerson elements should be removed from EntityDescriptors. Default value: true */
    private boolean removeContactPerson = true;

    /**
     * Gets the list of designated entity IDs.
     * 
     * @return list of designated entity IDs, never null
     */
    public Collection<String> getDesignatedEntities() {
        return designatedEntities;
    }

    /**
     * Sets the list of designated entity IDs.
     * 
     * @param ids list of designated entity IDs
     */
    public synchronized void setDesignatedEntities(final Collection<String> ids) {
        if (isInitialized()) {
            return;
        }
        designatedEntities = CollectionSupport.addNonNull(ids, new LazySet<String>());
    }

    /**
     * Whether the list of designated entities should be considered a whitelist.
     * 
     * @return true if the designated entities should be considered a whitelist, false otherwise
     */
    public boolean isWhitelistingEntities() {
        return whitelistingEntities;
    }

    /**
     * Sets whether the list of designated entities should be considered a whitelist.
     * 
     * @param whitelisting true if the designated entities should be considered a whitelist, false otherwise
     */
    public synchronized void setWhitelistingEntities(final boolean whitelisting) {
        if (isInitialized()) {
            return;
        }
        whitelistingEntities = whitelisting;
    }

    /**
     * Gets the list of designated entity roles. The list may contain either role element names or schema types.
     * 
     * @return list of designated entity roles, never null
     */
    public Collection<QName> getDesignatedRoles() {
        return designatedRoles;
    }

    /**
     * Sets the list of designated entity roles. The list may contain either role element names or schema types.
     * 
     * @param roles list of designated entity roles
     */
    public synchronized void setDesignatedRoles(final Collection<QName> roles) {
        if (isInitialized()) {
            return;
        }
        designatedRoles = CollectionSupport.addNonNull(roles, new LazySet<QName>());
    }

    /**
     * Gets whether the list of designated roles should be considered a whitelist.
     * 
     * @return true if the designated roles should be considered a whitelist, false otherwise
     */
    public boolean isWhitelistingRoles() {
        return whitelistingRoles;
    }

    /**
     * Sets whether the list of designated roles should be considered a whitelist.
     * 
     * @param whitelisting true if the designated entities should be considered a whitelist, false otherwise
     */
    public synchronized void setWhitelistingRoles(final boolean whitelisting) {
        if (isInitialized()) {
            return;
        }
        whitelistingRoles = whitelisting;
    }

    /**
     * Gets whether EntityDescriptor elements without roles (after filtering) should be removed altogether.
     * 
     * @return true if EntityDescriptors without roles (after filtering) should be removed, false otherwise
     */
    public boolean getRemoveRolelessEntities() {
        return removeRolelessEntities;
    }

    /**
     * Sets whether EntityDescriptor elements without roles (after filtering) should be removed altogether.
     * 
     * @param remove whether EntityDescriptor elements without roles (after filtering) should be removed altogether
     */
    public synchronized void setRemoveRolelessEntities(final boolean remove) {
        if (isInitialized()) {
            return;
        }
        removeRolelessEntities = remove;
    }

    /**
     * Gets whether Organization elements should be removed from EntityDescriptor elements.
     * 
     * @return true if the elements should be removed, false otherwise
     */
    public boolean getRemoveOrganizations() {
        return removeOrganization;
    }

    /**
     * Sets whether Organization elements should be removed from EntityDescriptor elements.
     * 
     * @param remove whether Organization elements should be removed from EntityDescriptor elements
     */
    public synchronized void setRemoveOrganization(final boolean remove) {
        if (isInitialized()) {
            return;
        }
        removeOrganization = remove;
    }

    /**
     * Gets whether ContactPerson elements should be removed from EntityDescriptor elements.
     * 
     * @return true if the elements should be removed, false otherwise
     */
    public boolean getRemoveContactPerson() {
        return removeContactPerson;
    }

    /**
     * Sets whether ContactPerson elements should be removed from EntityDescriptor elements.
     * 
     * @param remove whether ContactPerson elements should be removed from EntityDescriptor elements
     */
    public synchronized void setRemoveContactPerson(final boolean remove) {
        if (isInitialized()) {
            return;
        }
        removeContactPerson = remove;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(final MetadataCollection<DomMetadata> metadataCollection) {
        final ComponentInfo compInfo = new ComponentInfo(this);

        final ArrayList<DomMetadata> markedForRemoval = new ArrayList<DomMetadata>();
        Element descriptor;
        for (DomMetadata metadata : metadataCollection) {
            descriptor = metadata.getMetadata();
            if (filterEntityDescriptor(descriptor)) {
                markedForRemoval.add(metadata);
            }
        }
        metadataCollection.removeAll(markedForRemoval);

        for (DomMetadata element : metadataCollection) {
            element.getMetadataInfo().put(compInfo);
        }

        compInfo.setCompleteInstant();
        return metadataCollection;
    }

    /**
     * Filters the given entity descriptor.
     * 
     * @param entityDescriptor entity descriptor to be filtered
     * 
     * @return true if the given entity descriptor itself should be filtered out, false otherwise
     */
    protected boolean filterEntityDescriptor(final Element entityDescriptor) {
        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");

        if (!designatedEntities.isEmpty()) {
            // if we're whitelisting entities and this entity isn't in the list, kick it out
            if (isWhitelistingEntities() && !designatedEntities.contains(entityId)) {
                log.debug("{} pipeline stage removing entity {} because it wasn't on the whitelist", getId(), entityId);
                return true;
            } else {
                // we're backlisting entities, if the entity is in the list, kick it out
                if (designatedEntities.contains(entityId)) {
                    log.debug("{} pipeline stage removing entity {} because it was on the blacklist", getId(), entityId);
                    return true;
                }
            }
        }

        // filter the internal elements of the entity
        final List<Element> children = ElementSupport.getChildElements(entityDescriptor);
        for (Element child : children) {
            filterRoleDescriptor(entityId, entityDescriptor, child);
            filterOrganization(entityId, entityDescriptor, child);
            filterContactPerson(entityId, entityDescriptor, child);
        }

        // if requested, kick out entities that don't have any roles at this point
        if (removeRolelessEntities) {
            for (Element child : children) {
                if (ObjectSupport.equalsAny(QNameSupport.getNodeQName(child), ROLE_DESCRIPTOR_NAME,
                        IDP_SSO_DESCRIPTOR_NAME, SP_SSO_DESCRIPTOR_NAME, AUTHN_AUTHORITY_DESCRIPTOR_NAME,
                        ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME, PDP_DESCRIPTOR_NAME)) {
                    log.debug("{} pipeline stage removing entity {} because it no longer contains roles", getId(),
                            entityId);
                    return true;
                }
            }
        }

        // entity has been filtered and made it through, don't kick it out
        return false;
    }

    /**
     * Removes roles from the EntityDescriptor that are not whitelisted, if there are whitelisted roles.
     * 
     * @param entityId ID of the entity
     * @param entityDescriptor the entity descriptor from which the role elements are to be removed
     * @param childElement a child element of the entity descriptor
     */
    protected void filterRoleDescriptor(final String entityId, final Element entityDescriptor,
            final Element childElement) {
        if (designatedRoles.isEmpty()) {
            return;
        }

        log.debug("{} pipeline stage filtering roles from EntityDescriptor {}", getId(), entityId);

        boolean isDesignatedRole = false;
        QName roleIdentifier = null;
        // check if the element is a <RoleDescriptor> and if its schema type is a designated role
        if (ElementSupport.isElementNamed(childElement, ROLE_DESCRIPTOR_NAME)) {
            roleIdentifier = DomTypeSupport.getXSIType(childElement);
            if (roleIdentifier != null && !designatedRoles.contains(roleIdentifier)) {
                isDesignatedRole = true;
            }
        }

        QName elementName = QNameSupport.getNodeQName(childElement);
        // check if the element is a SAML specified role and it is a designated role
        if (ObjectSupport.equalsAny(elementName, IDP_SSO_DESCRIPTOR_NAME, SP_SSO_DESCRIPTOR_NAME,
                AUTHN_AUTHORITY_DESCRIPTOR_NAME, ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME, PDP_DESCRIPTOR_NAME)) {
            roleIdentifier = elementName;
            if (!designatedRoles.contains(roleIdentifier)) {
                isDesignatedRole = true;
            }
        }

        if (roleIdentifier != null) {
            if ((isWhitelistingRoles() && !isDesignatedRole) || (!isWhitelistingRoles() && isDesignatedRole)) {
                log.debug("{} pipeline stage removing role {} from EntityDescriptor {}", new Object[] { getId(),
                        roleIdentifier, entityId });
                entityDescriptor.removeChild(childElement);
            } else {
                log.debug("{} pipeline did not remove role {} from EntityDescriptor {}", new Object[] { getId(),
                        roleIdentifier, entityId });
            }
        }
    }

    /**
     * Removes the Organization element, if {@link #removeOrganization} is true, from an EntityDescriptor element.
     * 
     * @param entityId ID of the entity
     * @param entityDescriptor the entity descriptor from which the organization element is to be removed
     * @param childElement a child element of the entity descriptor
     */
    protected void filterOrganization(final String entityId, final Element entityDescriptor, final Element childElement) {
        if (!removeOrganization) {
            return;
        }

        if (ElementSupport.isElementNamed(childElement, ORGANIZTION_NAME)) {
            log.debug("{} pipeline stage removing Organization from EntityDescriptor {}", getId(), entityId);
            entityDescriptor.removeChild(childElement);
        }
    }

    /**
     * Removes the ContactPerson element, if {@link #removeContactPerson} is true, from an EntityDescriptor element.
     * 
     * @param entityId ID of the entity
     * @param entityDescriptor the entity descriptor from which the contact person element is to be removed
     * @param childElement a child element of the entity descriptor
     */
    protected void filterContactPerson(final String entityId, final Element entityDescriptor, final Element childElement) {
        if (!removeContactPerson) {
            return;
        }

        if (ElementSupport.isElementNamed(childElement, CONTACT_PERSON_NAME)) {
            log.debug("{} pipeline stage removing ContactPerson from EntityDescriptor {}", getId(), entityId);
            entityDescriptor.removeChild(childElement);
        }
    }
}