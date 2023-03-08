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
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractFilteringStage;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.xml.DOMTypeSupport;
import net.shibboleth.shared.xml.ElementSupport;
import net.shibboleth.shared.xml.QNameSupport;

/**
 * A pipeline stage that will filter SAML role descriptors from EntityDescriptors.
 * 
 * This filter will work on {@link Element} items that are entity or entities descriptors. In the case of
 * EntitiesDescriptors the role filter will effect all descendant EntityDescriptors.
 */
@ThreadSafe
public class EntityRoleFilterStage extends AbstractFilteringStage<Element> {

    /**
     * Set containing the SAML-defined, named role descriptors: {@link SAMLMetadataSupport#IDP_SSO_DESCRIPTOR_NAME},
     * {@link SAMLMetadataSupport#SP_SSO_DESCRIPTOR_NAME}, {@link SAMLMetadataSupport#AUTHN_AUTHORITY_DESCRIPTOR_NAME},
     * {@link SAMLMetadataSupport#ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME}, {@link SAMLMetadataSupport#PDP_DESCRIPTOR_NAME}.
     */
    @Nonnull  @NonnullElements @Unmodifiable
    private static final Set<QName> NAMED_ROLES = Set.of(SAMLMetadataSupport.IDP_SSO_DESCRIPTOR_NAME,
            SAMLMetadataSupport.SP_SSO_DESCRIPTOR_NAME,
            SAMLMetadataSupport.AUTHN_AUTHORITY_DESCRIPTOR_NAME,
            SAMLMetadataSupport.ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME,
            SAMLMetadataSupport.PDP_DESCRIPTOR_NAME);

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntityRoleFilterStage.class);

    /** Role element or type names which are white/black listed depending on the value of {@link #whitelistingRoles}. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Set<QName> designatedRoles = CollectionSupport.emptySet();

    /** Whether {@link #designatedRoles} should be considered a whitelist or a blacklist. Default value: false */
    @GuardedBy("this") private boolean whitelistingRoles;

    /**
     * Whether EntityDescriptor elements that do not contain roles, after filtering, should be removed. Default value:
     * true
     */
    @GuardedBy("this") private boolean removingRolelessEntities = true;

    /** Whether EntitiesDescriptor that do not contain EntityDescriptors should be removed. Default value: true */
    @GuardedBy("this") private boolean removingEntitylessEntitiesDescriptor = true;

    /**
     * Gets the list of designated entity roles. The list may contain either role element names or schema types.
     * 
     * @return list of designated entity roles, never null
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Collection<QName> getDesignatedRoles() {
        return designatedRoles;
    }

    /**
     * Sets the list of designated entity roles. The list may contain either role element names or schema types.
     * 
     * @param roles list of designated entity roles
     */
    public synchronized void setDesignatedRoles(@Nonnull @NonnullElements @Unmodifiable final Collection<QName> roles) {
        checkSetterPreconditions();
        designatedRoles = Set.copyOf(roles);
    }

    /**
     * Gets whether the list of designated roles should be considered a whitelist.
     * 
     * @return true if the designated roles should be considered a whitelist, false otherwise
     */
    public final synchronized boolean isWhitelistingRoles() {
        return whitelistingRoles;
    }

    /**
     * Sets whether the list of designated roles should be considered a whitelist.
     * 
     * @param whitelisting true if the designated entities should be considered a whitelist, false otherwise
     */
    public synchronized void setWhitelistingRoles(final boolean whitelisting) {
        checkSetterPreconditions();
        whitelistingRoles = whitelisting;
    }

    /**
     * Gets whether EntityDescriptor elements without roles (after filtering) should be removed altogether.
     * 
     * @return true if EntityDescriptors without roles (after filtering) should be removed, false otherwise
     */
    public final synchronized boolean isRemovingRolelessEntities() {
        return removingRolelessEntities;
    }

    /**
     * Sets whether EntityDescriptor elements without roles (after filtering) should be removed altogether.
     * 
     * @param remove whether EntityDescriptor elements without roles (after filtering) should be removed altogether
     */
    public synchronized void setRemoveRolelessEntities(final boolean remove) {
        checkSetterPreconditions();
        removingRolelessEntities = remove;
    }

    /**
     * Gets whether EntitiesDescriptor that do not contain EntityDescriptors should be removed.
     * 
     * @return whether EntitiesDescriptor that do not contain EntityDescriptors should be removed
     */
    public final synchronized boolean isRemovingEntitylessEntitiesDescriptor() {
        return removingEntitylessEntitiesDescriptor;
    }

    /**
     * Sets whether EntitiesDescriptor that do not contain EntityDescriptors should be removed.
     * 
     * @param remove whether EntitiesDescriptor that do not contain EntityDescriptors should be removed
     */
    public synchronized void setRemovingEntitylessEntitiesDescriptor(final boolean remove) {
        checkSetterPreconditions();
        removingEntitylessEntitiesDescriptor = remove;
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
        boolean remove = true;

        final List<Element> childEntitiesDescriptors =
                ElementSupport.getChildElements(entitiesDescriptor, SAMLMetadataSupport.ENTITIES_DESCRIPTOR_NAME);
        for (final var descriptor : childEntitiesDescriptors) {
            if (processEntitiesDescriptor(descriptor)) {
                entitiesDescriptor.removeChild(descriptor);
            } else {
                remove = false;
            }
        }

        final List<Element> childEntityDescriptors =
                ElementSupport.getChildElements(entitiesDescriptor, SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        for (final var descriptor : childEntityDescriptors) {
            if (processEntityDescriptor(descriptor)) {
                entitiesDescriptor.removeChild(descriptor);
            } else {
                remove = false;
            }
        }

        if (remove && isRemovingEntitylessEntitiesDescriptor()) {
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
        if (getDesignatedRoles().isEmpty()) {
            return false;
        }

        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");

        log.debug("{} pipeline stage filtering roles from EntityDescriptor {}", getId(), entityId);

        final boolean hasRoles = hasFilteredRoles(entityId, entityDescriptor);
        if (!hasRoles && isRemovingRolelessEntities()) {
            return true;
        }

        return false;
    }

    /**
     * Iterates over the roles of an EntityDescriptor and filters out the appropriate ones.
     * 
     * @param entityId ID of the entity whose roles are being processed
     * @param entityDescriptor descriptor of entity whose roles are being processed
     * 
     * @return <code>true</code> if the EntityDescriptor has any roles remaining after processing
     */
    private boolean hasFilteredRoles(@Nonnull final String entityId, @Nonnull final Element entityDescriptor) {
        boolean remains = false;
        final List<Element> childElements = ElementSupport.getChildElements(entityDescriptor);
        for (final var child : childElements) {
            final QName childQName = QNameSupport.getNodeQName(child);

            final QName roleIdentifier;
            if (Objects.equals(childQName, SAMLMetadataSupport.ROLE_DESCRIPTOR_NAME)) {
                roleIdentifier = DOMTypeSupport.getXSIType(child);
            } else if (NAMED_ROLES.contains(childQName)) {
                roleIdentifier = childQName;
            } else {
                continue;
            }

            if (roleIdentifier != null) {
                final boolean isDesignatedRole = getDesignatedRoles().contains(roleIdentifier);
                if ((isWhitelistingRoles() && !isDesignatedRole) || (!isWhitelistingRoles() && isDesignatedRole)) {
                    log.debug("{} pipeline stage removing role {} from EntityDescriptor {}", new Object[] {getId(),
                            roleIdentifier, entityId,});
                    entityDescriptor.removeChild(child);
                } else {
                    log.debug("{} pipeline did not remove role {} from EntityDescriptor {}", new Object[] {getId(),
                            roleIdentifier, entityId,});
                    remains = true;
                }
            }
        }

        return remains;
    }

}
