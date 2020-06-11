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
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractFilteringStage;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.xml.DOMTypeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import net.shibboleth.utilities.java.support.xml.QNameSupport;

/**
 * A pipeline stage that will filter SAML role descriptors from EntityDescriptors.
 * 
 * This filter will work on {@link Element} items that are entity or entities descriptors. In the case of
 * EntitiesDescriptors the role filter will effect all descendant EntityDescriptors.
 */
@ThreadSafe
public class EntityRoleFilterStage extends AbstractFilteringStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntityRoleFilterStage.class);

    /**
     * Set containing the SAML-defined, named role descriptors: {@link SAMLMetadataSupport#IDP_SSO_DESCRIPTOR_NAME},
     * {@link SAMLMetadataSupport#SP_SSO_DESCRIPTOR_NAME}, {@link SAMLMetadataSupport#AUTHN_AUTHORITY_DESCRIPTOR_NAME},
     * {@link SAMLMetadataSupport#ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME}, {@link SAMLMetadataSupport#PDP_DESCRIPTOR_NAME}.
     */
    @Nonnull @NonnullElements @Unmodifiable
    private final Set<QName> namedRoles = Set.of(SAMLMetadataSupport.IDP_SSO_DESCRIPTOR_NAME,
            SAMLMetadataSupport.SP_SSO_DESCRIPTOR_NAME,
            SAMLMetadataSupport.AUTHN_AUTHORITY_DESCRIPTOR_NAME,
            SAMLMetadataSupport.ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME,
            SAMLMetadataSupport.PDP_DESCRIPTOR_NAME);

    /** Role element or type names which are white/black listed depending on the value of {@link #whitelistingRoles}. */
    @Nonnull @NonnullElements @Unmodifiable
    private Set<QName> designatedRoles = Set.of();

    /** Whether {@link #designatedRoles} should be considered a whitelist or a blacklist. Default value: false */
    private boolean whitelistingRoles;

    /**
     * Whether EntityDescriptor elements that do not contain roles, after filtering, should be removed. Default value:
     * true
     */
    private boolean removingRolelessEntities = true;

    /** Whether EntitiesDescriptor that do not contain EntityDescriptors should be removed. Default value: true */
    private boolean removingEntitylessEntitiesDescriptor = true;

    /**
     * Gets the list of designated entity roles. The list may contain either role element names or schema types.
     * 
     * @return list of designated entity roles, never null
     */
    @Nonnull @NonnullElements @Unmodifiable
    public Collection<QName> getDesignatedRoles() {
        return designatedRoles;
    }

    /**
     * Sets the list of designated entity roles. The list may contain either role element names or schema types.
     * 
     * @param roles list of designated entity roles
     */
    public synchronized void setDesignatedRoles(@Nonnull @NonnullElements @Unmodifiable final Collection<QName> roles) {
        throwSetterPreconditionExceptions();
        designatedRoles = Set.copyOf(roles);
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
        throwSetterPreconditionExceptions();
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
        throwSetterPreconditionExceptions();
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
        throwSetterPreconditionExceptions();
        removingEntitylessEntitiesDescriptor = remove;
    }

    @Override
    protected void doDestroy() {
        designatedRoles = null;

        super.doDestroy();
    }

    @Override
    protected boolean doExecute(@Nonnull final Item<Element> item) {
        final Element descriptor = item.unwrap();
        if (SAMLMetadataSupport.isEntitiesDescriptor(descriptor)) {
            if (processEntitiesDescriptor(descriptor)) {
                return false;
            }
        } else if (SAMLMetadataSupport.isEntityDescriptor(descriptor)) {
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
    protected boolean processEntitiesDescriptor(@Nonnull final Element entitiesDescriptor) {
        Iterator<Element> descriptorItr;
        Element descriptor;

        final List<Element> childEntitiesDescriptors =
                ElementSupport.getChildElements(entitiesDescriptor, SAMLMetadataSupport.ENTITIES_DESCRIPTOR_NAME);
        descriptorItr = childEntitiesDescriptors.iterator();
        while (descriptorItr.hasNext()) {
            descriptor = descriptorItr.next();
            if (processEntitiesDescriptor(descriptor)) {
                entitiesDescriptor.removeChild(descriptor);
                descriptorItr.remove();
            }
        }

        final List<Element> childEntityDescriptors =
                ElementSupport.getChildElements(entitiesDescriptor, SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME);
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
    protected boolean processEntityDescriptor(@Nonnull final Element entityDescriptor) {
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
    protected List<Element> getFilteredRoles(@Nonnull final String entityId, @Nonnull final Element entityDescriptor) {
        final List<Element> childElements = ElementSupport.getChildElements(entityDescriptor);

        final Iterator<Element> childItr = childElements.iterator();

        Element child;
        QName childQName;
        QName roleIdentifier;
        while (childItr.hasNext()) {
            child = childItr.next();
            childQName = QNameSupport.getNodeQName(child);
            roleIdentifier = null;

            if (Objects.equals(childQName, SAMLMetadataSupport.ROLE_DESCRIPTOR_NAME)) {
                roleIdentifier = DOMTypeSupport.getXSIType(child);
            } else if (namedRoles.contains(childQName)) {
                roleIdentifier = childQName;
            } else {
                childItr.remove();
                continue;
            }

            final boolean isDesignatedRole = designatedRoles.contains(roleIdentifier);
            if (roleIdentifier != null) {
                if ((isWhitelistingRoles() && !isDesignatedRole) || (!isWhitelistingRoles() && isDesignatedRole)) {
                    log.debug("{} pipeline stage removing role {} from EntityDescriptor {}", new Object[] {getId(),
                            roleIdentifier, entityId,});
                    entityDescriptor.removeChild(child);
                    childItr.remove();
                } else {
                    log.debug("{} pipeline did not remove role {} from EntityDescriptor {}", new Object[] {getId(),
                            roleIdentifier, entityId,});
                }
            }
        }

        return childElements;
    }
}
