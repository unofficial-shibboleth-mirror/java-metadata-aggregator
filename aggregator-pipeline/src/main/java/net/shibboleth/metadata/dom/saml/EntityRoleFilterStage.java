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
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;

import org.opensaml.util.ObjectSupport;
import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazySet;
import org.opensaml.util.xml.DomTypeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.opensaml.util.xml.QNameSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A pipeline stage that will filter SAML role descriptors from EntityDescriptors.
 * 
 * This filter will work on {@link DomElementItem} elements that are entity or entities descriptors. In the case of
 * EntitiesDescriptors the role filter will effect all descendant EntityDescriptors.
 */
@ThreadSafe
public class EntityRoleFilterStage extends BaseIteratingStage<DomElementItem> {

    /** QName of the RoleDescriptor element. */
    public static final QName ROLE_DESCRIPTOR_NAME = new QName(SamlMetadataSupport.MD_NS, "RoleDescriptor");

    /** QName of the IDPSSODescriptor element. */
    public static final QName IDP_SSO_DESCRIPTOR_NAME = new QName(SamlMetadataSupport.MD_NS, "IDPSSODescriptor");

    /** QName of the SPSSODescriptor element. */
    public static final QName SP_SSO_DESCRIPTOR_NAME = new QName(SamlMetadataSupport.MD_NS, "SPSSODescriptor");

    /** QName of the AuthnAuthorityDescriptor element. */
    public static final QName AUTHN_AUTHORITY_DESCRIPTOR_NAME = new QName(SamlMetadataSupport.MD_NS,
            "AuthnAuthorityDescriptor");

    /** QName of the AttributeAuthorityDescriptor element. */
    public static final QName ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME = new QName(SamlMetadataSupport.MD_NS,
            "AttributeAuthorityDescriptor");

    /** QName of the PDPDescriptor element. */
    public static final QName PDP_DESCRIPTOR_NAME = new QName(SamlMetadataSupport.MD_NS, "PDPDescriptor");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntityRoleFilterStage.class);

    /** Role element or type names which are white/black listed depending on the value of {@link #whitelistingRoles}. */
    private Collection<QName> designatedRoles = new LazySet<QName>();

    /** Whether {@link #designatedRoles} should be considered a whitelist or a blacklist. Default value: false */
    private boolean whitelistingRoles;

    /**
     * Whether EntityDescriptor elements that do not contain roles, after filtering, should be removed. Default
     * value: true
     */
    private boolean removingRolelessEntities = true;

    /** Whether EntitiesDescriptor that do not contain EntityDescriptors should be removed. Default value: true */
    private boolean removingEntitylessEntitiesDescriptor = true;

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
    public boolean isRemovingRolelessEntities() {
        return removingRolelessEntities;
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
        removingRolelessEntities = remove;
    }

    /**
     * Gets whether EntitiesDescriptor that do not contain EntityDescriptors should be removed.
     * 
     * @return whether EntitiesDescriptor that do not contain EntityDescriptors should be removed
     */
    public boolean isRemovingEntitylessEntitiesDescriptor() {
        return removingEntitylessEntitiesDescriptor;
    }

    /**
     * Sets whether EntitiesDescriptor that do not contain EntityDescriptors should be removed.
     * 
     * @param remove whether EntitiesDescriptor that do not contain EntityDescriptors should be removed
     */
    public synchronized void setRemovingEntitylessEntitiesDescriptor(final boolean remove) {
        if (isInitialized()) {
            return;
        }
        removingEntitylessEntitiesDescriptor = remove;
    }

    /** {@inheritDoc} */
    protected boolean doExecute(DomElementItem item) {
        Element descriptor = item.unwrap();
        if (SamlMetadataSupport.isEntitiesDescriptor(descriptor)) {
            if (processEntitiesDescriptor(descriptor)) {
                return false;
            }
        } else if (SamlMetadataSupport.isEntityDescriptor(descriptor)) {
            if (processEntityDescriptor(descriptor)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Iterates over all child EntitiesDescriptor, passing each to {@link #processEntitiesDescriptor(Element)}, and
     * EntityDescriptor, passing each to {@link #processEntityDescriptor(Element)}. If
     * {@link #isRemovingEntitylessEntitiesDescriptor()} is true and the EntitiesDescriptor contains no child
     * EntitiesDescriptors or EntityDescriptors it is removed.
     * 
     * @param entitiesDescriptor EntitiesDescriptor being processed
     * 
     * @return true if the descriptor should be removed, false otherwise
     */
    protected boolean processEntitiesDescriptor(final Element entitiesDescriptor) {
        Iterator<Element> descriptorItr;
        Element descriptor;

        final List<Element> childEntitiesDescriptors = ElementSupport.getChildElements(entitiesDescriptor,
                SamlMetadataSupport.ENTITIES_DESCRIPTOR_NAME);
        descriptorItr = childEntitiesDescriptors.iterator();
        while (descriptorItr.hasNext()) {
            descriptor = descriptorItr.next();
            if (processEntitiesDescriptor(descriptor)) {
                entitiesDescriptor.removeChild(descriptor);
                descriptorItr.remove();
            }
        }

        final List<Element> childEntityDescriptors = ElementSupport.getChildElements(entitiesDescriptor,
                SamlMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        descriptorItr = childEntityDescriptors.iterator();
        while (descriptorItr.hasNext()) {
            descriptor = descriptorItr.next();
            if (processEntityDescriptor(descriptor)) {
                entitiesDescriptor.removeChild(descriptor);
                descriptorItr.remove();
            }
        }

        if (removingEntitylessEntitiesDescriptor && childEntitiesDescriptors.isEmpty()
                && childEntityDescriptors.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * Processes an EntityDescriptor. First, all filtered out roles are removed. Then, if no roles are left and
     * {@link #isRemovingRolelessEntities()} is true the EntityDescriptor is marked to be removed.
     * 
     * @param entityDescriptor entity descriptor being processed
     * 
     * @return true if the entity descriptor should be removed, false otherwise
     */
    protected boolean processEntityDescriptor(final Element entityDescriptor) {
        if (designatedRoles.isEmpty()) {
            return false;
        }

        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");

        log.debug("{} pipeline stage filtering roles from EntityDescriptor {}", getId(), entityId);

        final List<Element> roles = getFilteredRoles(entityId, entityDescriptor);
        if (removingRolelessEntities && roles.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * Iterates over the roles of a EntitiesDescriptor, filters out the appropriate ones and returns the rest.
     * 
     * @param entityId ID of the entity whose roles are being processed
     * @param entityDescriptor descriptor of entity whose roles are being processed
     * 
     * @return the list of roles remaining after processing
     */
    protected List<Element> getFilteredRoles(final String entityId, final Element entityDescriptor) {
        final List<Element> childElements = ElementSupport.getChildElements(entityDescriptor);

        final Iterator<Element> childItr = childElements.iterator();

        Element child;
        QName childQName;
        QName roleIdentifier;
        while (childItr.hasNext()) {
            child = childItr.next();
            childQName = QNameSupport.getNodeQName(child);
            roleIdentifier = null;

            if (ObjectSupport.equals(childQName, ROLE_DESCRIPTOR_NAME)) {
                roleIdentifier = DomTypeSupport.getXSIType(child);
            } else if (ObjectSupport.equalsAny(childQName, IDP_SSO_DESCRIPTOR_NAME, SP_SSO_DESCRIPTOR_NAME,
                    AUTHN_AUTHORITY_DESCRIPTOR_NAME, ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME, PDP_DESCRIPTOR_NAME)) {
                roleIdentifier = childQName;
            } else {
                childItr.remove();
                continue;
            }

            boolean isDesignatedRole = designatedRoles.contains(roleIdentifier);
            if (roleIdentifier != null) {
                if ((isWhitelistingRoles() && !isDesignatedRole) || (!isWhitelistingRoles() && isDesignatedRole)) {
                    log.debug("{} pipeline stage removing role {} from EntityDescriptor {}", new Object[] { getId(),
                            roleIdentifier, entityId, });
                    entityDescriptor.removeChild(child);
                    childItr.remove();
                } else {
                    log.debug("{} pipeline did not remove role {} from EntityDescriptor {}", new Object[] { getId(),
                            roleIdentifier, entityId, });
                }
            }
        }

        return childElements;
    }
}