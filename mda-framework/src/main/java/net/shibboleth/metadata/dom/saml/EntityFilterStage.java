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
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractFilteringStage;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.primitive.LoggerFactory;
import net.shibboleth.shared.xml.ElementSupport;

/** A pipeline stage that will remove SAML EntityDescriptior elements which do meet specified filtering criteria. */
@ThreadSafe
public class EntityFilterStage extends AbstractFilteringStage<Element> {

    /** Class logger. */
    private static final @Nonnull Logger LOG = LoggerFactory.getLogger(EntityFilterStage.class);

    /** Entities which are white/black listed depending on the value of {@link #whitelistingEntities}. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Set<String> designatedEntities = CollectionSupport.emptySet();

    /** Whether {@link #designatedEntities} should be considered a whitelist or a blacklist. Default value: false */
    @GuardedBy("this") private boolean whitelistingEntities;

    /** Whether EntitiesDescriptor that do not contain EntityDescriptors should be removed. Default value: true */
    @GuardedBy("this") private boolean removingEntitylessEntitiesDescriptor = true;

    /**
     * Gets the list of designated entity IDs.
     * 
     * @return list of designated entity IDs, never null
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Collection<String> getDesignatedEntities() {
        return designatedEntities;
    }

    /**
     * Sets the list of designated entity IDs.
     * 
     * @param ids list of designated entity IDs
     */
    public synchronized void setDesignatedEntities(
            @Nonnull @NonnullElements @Unmodifiable final Collection<String> ids) {
        checkSetterPreconditions();
        designatedEntities = Set.copyOf(ids);
    }

    /**
     * Whether the list of designated entities should be considered a whitelist.
     * 
     * @return true if the designated entities should be considered a whitelist, false otherwise
     */
    public final synchronized boolean isWhitelistingEntities() {
        return whitelistingEntities;
    }

    /**
     * Sets whether the list of designated entities should be considered a whitelist.
     * 
     * @param whitelisting true if the designated entities should be considered a whitelist, false otherwise
     */
    public synchronized void setWhitelistingEntities(final boolean whitelisting) {
        checkSetterPreconditions();
        whitelistingEntities = whitelisting;
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
     * Filters the given entity descriptor.
     * 
     * @param entityDescriptor entity descriptor to be filtered
     * 
     * @return true if the given entity descriptor itself should be filtered out, false otherwise
     */
    protected boolean processEntityDescriptor(@Nonnull final Element entityDescriptor) {
        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");

        // if we're whitelisting entities and this entity isn't in the list, kick it out
        if (isWhitelistingEntities() && !getDesignatedEntities().contains(entityId)) {
            LOG.debug("{} pipeline stage removing entity {} because it wasn't on the whitelist", getId(), entityId);
            return true;
        }

        // if we're backlisting entities and this entity is in the list, kick it out
        if (!isWhitelistingEntities() && getDesignatedEntities().contains(entityId)) {
            LOG.debug("{} pipeline stage removing entity {} because it was on the blacklist", getId(), entityId);
            return true;
        }

        // entity has been filtered and made it through, don't kick it out
        return false;
    }

}
