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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.jcip.annotations.ThreadSafe;

import org.opensaml.util.Objects;
import org.opensaml.util.xml.Elements;
import org.opensaml.util.xml.QNames;
import org.opensaml.util.xml.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.PipelineInitializationException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.stage.Stage;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadataElement;

/**
 * A pipeline stage that will remove SAML EntityDescriptior elements which do meet specified filtering criteria.
 */
@ThreadSafe
public class SAMLEntityFilterStage extends AbstractComponent implements Stage<DomMetadataElement> {

    /** QName of the RoleDescriptor element. */
    public static final QName ROLE_DESCRIPTOR_NAME = new QName(SAMLConstants.MD_NS, "RoleDescriptor");

    /** QName of the IDPSSODescriptor element. */
    public static final QName IDP_SSO_DESCRIPTOR_NAME = new QName(SAMLConstants.MD_NS, "IDPSSODescriptor");

    /** QName of the SPSSODescriptor element. */
    public static final QName SP_SSO_DESCRIPTOR_NAME = new QName(SAMLConstants.MD_NS, "SPSSODescriptor");

    /** QName of the AuthnAuthorityDescriptor element. */
    public static final QName AUTHN_AUTHORITY_DESCRIPTOR_NAME = new QName(SAMLConstants.MD_NS,
            "AuthnAuthorityDescriptor");

    /** QName of the AttributeAuthorityDescriptor element. */
    public static final QName ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME = new QName(SAMLConstants.MD_NS,
            "AttributeAuthorityDescriptor");

    /** QName of the PDPDescriptor element. */
    public static final QName PDP_DESCRIPTOR_NAME = new QName(SAMLConstants.MD_NS, "PDPDescriptor");

    /** QName of the Organization element. */
    private static final QName ORGANIZTION_NAME = new QName(SAMLConstants.MD_NS, "Organization");

    /** QName of the ContactPerson element. */
    private static final QName CONTACT_PERSON_NAME = new QName(SAMLConstants.MD_NS, "ContactPerson");
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(SAMLEntityFilterStage.class);

    // TODO remove binding, services, white/blacklist extensions

    /** Whether EntityDescriptor metadata elements that do not contain roles, after filtering, should be removed. */
    private boolean removeRolelessEntities;

    /** Roles which should be retained within an entity descriptor. */
    private Collection<QName> whitelistedRoles;

    /** Indicates that Organization elements should be removed from EntityDescriptors. */
    private boolean removeOrganization;

    /** Indicates that ContactPerson elements should be removed from EntityDescriptors. */
    private boolean removeContactPerson;

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     */
    public SAMLEntityFilterStage(String stageId) {
        super(stageId);

        removeRolelessEntities = true;
        whitelistedRoles = new ArrayList<QName>();
        removeOrganization = true;
        removeContactPerson = true;
    }

    /**
     * Gets whether EntityDescriptor elements without roles (after filtering) should be removed altogether.
     * 
     * @return true if roleless EntityDescriptors should be removed, false otherwise
     */
    public boolean getRemoveRolelessEntities() {
        return removeRolelessEntities;
    }

    /**
     * Sets whether EntityDescriptor elements without roles (after filtering) should be removed altogether.
     * 
     * @param remove whether EntityDescriptor elements without roles (after filtering) should be removed altogether
     */
    public void setRemoveRolelessEntities(boolean remove) {
        removeRolelessEntities = remove;
    }

    /**
     * Gets the list of whitelisted roles. This collection should include either the qualified tag names for IDP and SP
     * SSO descriptors, Authn and Attribute authority descriptors, or a PDP descriptor or the schema type for any
     * RoleDescriptor elements.
     * 
     * @return whitelisted roles
     */
    public Collection<QName> getWhitelistedRoles() {
        return whitelistedRoles;
    }

    /**
     * Sets the list of whitelisted roles. This collection should include either the qualified tag names for IDP and SP
     * SSO descriptors, Authn and Attribute authority descriptors, or a PDP descriptor or the schema type for any
     * RoleDescriptor elements.
     * 
     * @param roles whitelisted roles
     */
    public void setWhitelistedRoles(Collection<QName> roles) {
        whitelistedRoles = new ArrayList<QName>(roles);
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
    public void setRemoveOrganization(boolean remove) {
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
    public void setRemoveContactPerson(boolean remove) {
        removeContactPerson = remove;
    }

    /** {@inheritDoc} */
    public MetadataElementCollection<DomMetadataElement> execute(Map<String, Object> parameters,
            MetadataElementCollection<DomMetadataElement> metadataCollection) {
        ArrayList<DomMetadataElement> markedForRemoval = new ArrayList<DomMetadataElement>();

        Element descriptor;
        for (DomMetadataElement metadata : metadataCollection) {
            descriptor = metadata.getEntityMetadata();

            List<Element> children = Elements.getChildElements(descriptor);
            for (Element child : children) {
                filterRoleDescriptor(descriptor, child);
                filterOrganization(descriptor, child);
                filterContactPerson(descriptor, child);
            }

            if (shouldRemoveEntityDescriptor(descriptor)) {
                log.debug("{} pipeline stage removing roleless EntityDescriptor {}", getId(), descriptor
                        .getAttributeNS(null, "entityID"));
                markedForRemoval.add(metadata);
            }
        }

        metadataCollection.removeAll(markedForRemoval);

        return metadataCollection;
    }

    /**
     * Removes roles from the EntityDescriptor that are not whitelisted, if there are whitelisted roles.
     * 
     * @param entityDescriptor the entity descriptor from which the role elements are to be removed
     * @param childElement a child element of the entity descriptor
     */
    protected void filterRoleDescriptor(Element entityDescriptor, Element childElement) {
        if (whitelistedRoles.isEmpty()) {
            return;
        }

        log.debug("{} pipeline stage filtering roles from EntityDescriptor {}", getId(), entityDescriptor
                .getAttributeNS(null, "entityID"));

        QName elementName = QNames.getNodeQName(childElement);
        boolean removeRole = false;

        if (Elements.isElementNamed(childElement, ROLE_DESCRIPTOR_NAME)) {
            QName type = Types.getXSIType(childElement);
            if (type != null && !whitelistedRoles.contains(type)) {
                log.debug("{} pipeline stage marked RoleDescriptor of type {} for removal", getId(), type);
                removeRole = true;
            }
        }

        if (Objects.equalsAny(elementName, IDP_SSO_DESCRIPTOR_NAME, SP_SSO_DESCRIPTOR_NAME,
                AUTHN_AUTHORITY_DESCRIPTOR_NAME, ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME, PDP_DESCRIPTOR_NAME)) {
            if (!whitelistedRoles.contains(elementName)) {
                log.debug("{} pipeline stage marked {} role for removal", getId(), elementName);
                removeRole = true;
            }
        }

        if (removeRole) {
            log.debug("{} pipeline stage removing marked role from EntityDescriptor {}", getId(), entityDescriptor
                    .getAttributeNS(null, "entityID"));
            entityDescriptor.removeChild(childElement);
        } else {
            log.debug("{} pipeline did not remove any role from EntityDescriptor {}", getId(), entityDescriptor
                    .getAttributeNS(null, "entityID"));
        }
    }

    /**
     * Removes the Organization element, if {@link #removeOrganization} is true, from an EntityDescriptor element.
     * 
     * @param entityDescriptor the entity descriptor from which the organization element is to be removed
     * @param childElement a child element of the entity descriptor
     */
    protected void filterOrganization(Element entityDescriptor, Element childElement) {
        if (!removeOrganization) {
            return;
        }

        if (Elements.isElementNamed(childElement, ORGANIZTION_NAME)) {
            log.debug("{} pipeline stage removing Organization from EntityDescriptor {}", getId(), entityDescriptor
                    .getAttributeNS(null, "entityID"));
            entityDescriptor.removeChild(childElement);
        }
    }

    /**
     * Removes the ContactPerson element, if {@link #removeContactPerson} is true, from an EntityDescriptor element.
     * 
     * @param entityDescriptor the entity descriptor from which the contact person element is to be removed
     * @param childElement a child element of the entity descriptor
     */
    protected void filterContactPerson(Element entityDescriptor, Element childElement) {
        if (!removeContactPerson) {
            return;
        }

        if (Elements.isElementNamed(childElement, CONTACT_PERSON_NAME)) {
            log.debug("{} pipeline stage removing ContactPerson from EntityDescriptor {}", getId(), entityDescriptor
                    .getAttributeNS(null, "entityID"));
            entityDescriptor.removeChild(childElement);
        }
    }

    /**
     * Checks whether the given EntityDescriptor should be removed. A descriptor should be removed if
     * {@link #removeRolelessEntities} is true and the descriptor contains no roles.
     * 
     * @param entityDescriptor entity descriptor to be checked
     * 
     * @return true if the entityDescriptor should be removed, false otherwise
     */
    protected boolean shouldRemoveEntityDescriptor(Element entityDescriptor) {
        if (!removeRolelessEntities) {
            return false;
        }

        List<Element> children = Elements.getChildElements(entityDescriptor);
        for (Element child : children) {
            if (Objects.equalsAny(QNames.getNodeQName(child), ROLE_DESCRIPTOR_NAME, IDP_SSO_DESCRIPTOR_NAME,
                    SP_SSO_DESCRIPTOR_NAME, AUTHN_AUTHORITY_DESCRIPTOR_NAME, ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME,
                    PDP_DESCRIPTOR_NAME)) {
                return false;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws PipelineInitializationException {
        // nothing to do here
    }
}