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

import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.EntityIdInfo;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.SimpleMetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInfo;
import net.shibboleth.metadata.pipeline.Stage;

import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.opensaml.util.xml.QNameSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A pipeline stage that replaces any SAML EntitiesDescriptor found in the metadata collection with the EntityDescriptor
 * elements contained therein.
 * 
 * This stage will always add a {@link EntityIdInfo}, containing the SAML entity ID given in the EntityDescriptor, to
 * each metadata element. In addition it may optionally attached a validUntil and cacheDuration to each
 * EntityDescriptor. If {@link #pushCacheDuration} is true then the cacheDuration attribute of the EntityDescriptor is
 * set to the smallest cacheDuration of the EntityDescriptor and all enclosing EntitiesDescriptors. If
 * {@link #pushValidUntil} is true then the validUntil attribute of the EntityDescriptor is set to the nearest
 * validUntil time of the EntityDescriptor and all enclosing EntitiesDescriptors.
 */
@ThreadSafe
public class EntitiesDescriptorDisassemblerStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntitiesDescriptorDisassemblerStage.class);

    /** Whether validUntil attributes on EntitiesDescriptors are pushed down to descendant EntityDescriptors. */
    private boolean pushValidUntil = true;

    /** Whether cacheDuration attributes on EntitiesDescriptors are pushed down to descendant EntityDescriptors. */
    private boolean pushCacheDuration;

    /**
     * Get whether the validUntil attribute on an EntitiesDescriptor will be pushed down on to descendant
     * EntityDescriptor if they do not have a validUntil attribute.
     * 
     * @return true if validUntil attribute information is pushed down to EntityDescriptor elements
     */
    public boolean isPushValidUntil() {
        return pushValidUntil;
    }

    /**
     * Sets whether the validUntil attribute on an EntitiesDescriptor will be pushed down on to descendant
     * EntityDescriptor if they do not have a validUntil attribute.
     * 
     * @param pushDown true if validUntil attribute information is pushed down to EntityDescriptor elements
     */
    public synchronized void setPushValidUntil(final boolean pushDown) {
        if (isInitialized()) {
            return;
        }
        pushValidUntil = pushDown;
    }

    /**
     * Get whether the cacheDuration attribute on an EntitiesDescriptor will be pushed down on to descendant
     * EntityDescriptor if they do not have a cacheDuration attribute.
     * 
     * @return true if cacheDuration attribute information is pushed down to EntityDescriptor elements
     */
    public boolean isPushCacheDuration() {
        return pushCacheDuration;
    }

    /**
     * Sets whether the cacheDuration attribute on an EntitiesDescriptor will be pushed down on to descendant
     * EntityDescriptor if they do not have a cacheDuration attribute.
     * 
     * @param pushDown true if cacheDuration attribute information is pushed down to EntityDescriptor elements
     */
    public synchronized void setPushCacheDuration(final boolean pushDown) {
        if (isInitialized()) {
            return;
        }
        pushCacheDuration = pushDown;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(final MetadataCollection<DomMetadata> sourceCollection) {
        final ComponentInfo compInfo = new ComponentInfo(this);

        final SimpleMetadataCollection<DomMetadata> destinationCollection = new SimpleMetadataCollection<DomMetadata>();

        Element metadataElement;
        for (DomMetadata sourceMetadata : sourceCollection) {
            metadataElement = sourceMetadata.getMetadata();
            if (MetadataHelper.isEntitiesDescriptor(metadataElement)) {
                processEntitiesDescriptor(destinationCollection, metadataElement, null, null);
            } else if (MetadataHelper.isEntityDescriptor(metadataElement)) {
                destinationCollection.add(new DomMetadata(metadataElement));
            } else {
                log.debug("{} pipeline stage: metadata element {} not supported, ignoring it", getId(),
                        QNameSupport.getNodeQName(metadataElement));
            }
        }

        for (DomMetadata element : destinationCollection) {
            element.getMetadataInfo().put(compInfo);
        }

        compInfo.setCompleteInstant();
        return destinationCollection;
    }

    /**
     * Processes an EntitiesDescriptor element. All child EntityDescriptor elements are processed and
     * EntitiesDescriptors are run back through this method.
     * 
     * @param metadataCollection collection to which EntityDescriptor metadata elements are added
     * @param entitiesDescriptor the EntitiesDescriptor to break down
     * @param validUntil the validUntil attribute value from the ancestral EntitiesDescriptor, may be null
     * @param cacheDuration the cacheDuration attribute value from the ancestral EntitiesDescriptor, may be null
     */
    protected void processEntitiesDescriptor(final MetadataCollection<DomMetadata> metadataCollection,
            final Element entitiesDescriptor, final String validUntil, final String cacheDuration) {
        String desciptorValidUntil = null;
        if (pushValidUntil) {
            desciptorValidUntil = AttributeSupport.getAttributeValue(entitiesDescriptor,
                    MetadataHelper.VALID_UNTIL_ATTIB_NAME);
            if (desciptorValidUntil == null) {
                desciptorValidUntil = validUntil;
            }
        }

        String descriptorCacheDuration = null;
        if (pushCacheDuration) {
            descriptorCacheDuration = AttributeSupport.getAttributeValue(entitiesDescriptor,
                    MetadataHelper.CACHE_DURATION_ATTRIB_NAME);
            if (descriptorCacheDuration == null) {
                descriptorCacheDuration = cacheDuration;
            }
        }

        final List<Element> children = ElementSupport.getChildElements(entitiesDescriptor);
        for (Element child : children) {
            if (MetadataHelper.isEntitiesDescriptor(child)) {
                processEntitiesDescriptor(metadataCollection, child, desciptorValidUntil, descriptorCacheDuration);
            }
            if (MetadataHelper.isEntityDescriptor(child)) {
                metadataCollection.add(buildMetadataElement(child, desciptorValidUntil, descriptorCacheDuration));
            }
        }
    }

    /**
     * Builds a {@link DomMetadata} from the given EntityDescriptor element. If the EntityDescriptor does not already
     * have a validUntil or cacheDuration attribute these attributes are created and populated with the given values.
     * 
     * @param entityDescriptor EntityDescriptor element to be made in to a {@link DomMetadata}
     * @param validUntil validUntil attribute value to be added to the EntityDescriptor if the attribute does not
     *            already exist
     * @param cacheDuration cacheDuration attribute value to be added to the EntityDescriptor if the attribute does not
     *            already exist
     * 
     * @return the constructed metadata element
     */
    protected DomMetadata buildMetadataElement(final Element entityDescriptor, final String validUntil,
            final String cacheDuration) {
        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");

        if (validUntil != null
                && !AttributeSupport.hasAttribute(entityDescriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME)) {
            log.debug("{} pipeline stage adding valid until of {} to entity descriptor {}", new Object[] { getId(),
                    validUntil, entityId, });
            AttributeSupport.appendAttribute(entityDescriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME, validUntil);
        }

        if (cacheDuration != null
                && !AttributeSupport.hasAttribute(entityDescriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME)) {
            log.debug("{} pipeline stage adding cacheDuration of {} to EntityDescriptor {}", new Object[] { getId(),
                    cacheDuration, entityId, });
            AttributeSupport
                    .appendAttribute(entityDescriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME, cacheDuration);
        }

        final DomMetadata element = new DomMetadata(entityDescriptor);
        element.getMetadataInfo().put(new EntityIdInfo(entityId));
        return new DomMetadata(entityDescriptor);
    }
}