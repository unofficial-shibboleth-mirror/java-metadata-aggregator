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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractFilteringStage;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/** A pipeline stage that will remove SAML EntityDescriptior elements which do meet specified filtering criteria. */
@ThreadSafe
public class EntityFilterStage extends AbstractFilteringStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntityFilterStage.class);

    /** Entities which are white/black listed depending on the value of {@link #whitelistingEntities}. */
    private Collection<String> designatedEntities = Collections.emptyList();

    /** Whether {@link #designatedEntities} should be considered a whitelist or a blacklist. Default value: false */
    private boolean whitelistingEntities;

    /** Whether EntitiesDescriptor that do not contain EntityDescriptors should be removed. Default value: true */
    private boolean removingEntitylessEntitiesDescriptor = true;

    /**
     * Gets the list of designated entity IDs.
     * 
     * @return list of designated entity IDs, never null
     */
    @Nonnull @NonnullElements @Unmodifiable public Collection<String> getDesignatedEntities() {
        return designatedEntities;
    }

    /**
     * Sets the list of designated entity IDs.
     * 
     * @param ids list of designated entity IDs
     */
    public synchronized void setDesignatedEntities(@Nullable @NullableElements final Collection<String> ids) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (ids == null || ids.isEmpty()) {
            designatedEntities = Collections.emptyList();
        } else {
            designatedEntities = ImmutableList.copyOf(Iterables.filter(ids, Predicates.notNull()));
        }
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        whitelistingEntities = whitelisting;
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        removingEntitylessEntitiesDescriptor = remove;
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        designatedEntities = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected boolean doExecute(@Nonnull final Item<Element> item) {
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
     * Filters the given entity descriptor.
     * 
     * @param entityDescriptor entity descriptor to be filtered
     * 
     * @return true if the given entity descriptor itself should be filtered out, false otherwise
     */
    protected boolean processEntityDescriptor(@Nonnull final Element entityDescriptor) {
        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");

        if (designatedEntities.isEmpty()) {
            return false;
        }

        // if we're whitelisting entities and this entity isn't in the list, kick it out
        if (isWhitelistingEntities() && !designatedEntities.contains(entityId)) {
            log.debug("{} pipeline stage removing entity {} because it wasn't on the whitelist", getId(), entityId);
            return true;
        }

        // if we're backlisting entities and this entity is in the list, kick it out
        if (!isWhitelistingEntities() && designatedEntities.contains(entityId)) {
            log.debug("{} pipeline stage removing entity {} because it was on the blacklist", getId(), entityId);
            return true;
        }

        // entity has been filtered and made it through, don't kick it out
        return false;
    }
}