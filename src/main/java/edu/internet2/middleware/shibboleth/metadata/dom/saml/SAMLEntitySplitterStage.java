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

import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.opensaml.util.xml.Attributes;
import org.opensaml.util.xml.Elements;
import org.opensaml.util.xml.QNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.core.EntityIdInfo;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.ComponentInfo;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.ComponentInitializationException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.Stage;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;

/**
 * A pipeline stage that replaces any SAML EntitiesDescriptors found in the metadata collection and replaces them with
 * the EntityDescriptor elements contained therein.
 * 
 * This stage will always add a {@link EntityIdInfo}, containing the SAML entity ID given in the EntityDescriptor, to
 * each metadata element. In addition it may optionally attached a validUntil and cacheDuration to each
 * EntityDescriptor. If {@link #pushCacheDuration} is true then the cacheDuration attribute of the EntityDescriptor is
 * set to the smallest cacheDuration of the EntityDescriptor and all enclosing EntitiesDescriptors. If
 * {@link #pushValidUntil} is true then the validUntil attribute of the EntityDescriptor is set to the nearest
 * validUntil time of the EntityDescriptor and all enclosing EntitiesDescriptors.
 */
@ThreadSafe
public class SAMLEntitySplitterStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(SAMLEntitySplitterStage.class);

    /** Whether validUntil attributes on EntitiesDescriptors are pushed down to descendant EntityDescriptors. */
    private boolean pushValidUntil;

    /** Whether cacheDuration attributes on EntitiesDescriptors are pushed down to descendant EntityDescriptors. */
    private boolean pushCacheDuration;

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     */
    public SAMLEntitySplitterStage(String stageId) {
        super(stageId);
        pushValidUntil = true;
        pushCacheDuration = false;
    }

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     * @param pushDownValidUntil whether validUntil attributes on EntitiesDescriptors are pushed down to descendant
     *            EntityDescriptors
     * @param pushDownCacheDuration whether cacheDuration attributes on EntitiesDescriptors are pushed down to
     *            descendant EntityDescriptors
     */
    public SAMLEntitySplitterStage(String stageId, boolean pushDownValidUntil, boolean pushDownCacheDuration) {
        super(stageId);
        pushValidUntil = pushDownValidUntil;
        pushCacheDuration = pushDownCacheDuration;
    }

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
    public void setPushValidUntil(boolean pushDown) {
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
    public void setPushCacheDuration(boolean pushDown) {
        pushCacheDuration = pushDown;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> sourceCollection) {

        ComponentInfo compInfo = new ComponentInfo(this);

        SimpleMetadataCollection<DomMetadata> destinationCollection = new SimpleMetadataCollection<DomMetadata>();

        Element metadataElement;
        for (DomMetadata sourceMetadata : sourceCollection) {
            metadataElement = sourceMetadata.getMetadata();
            if (Elements.isElementNamed(metadataElement, SAMLConstants.ENTITIES_DESCRIPTOR_NAME)) {
                processEntitiesDescriptor(destinationCollection, metadataElement, null, null);
            } else if (Elements.isElementNamed(metadataElement, SAMLConstants.ENTITY_DESCRIPTOR_NAME)) {
                destinationCollection.add(new DomMetadata(metadataElement));
            } else {
                log.debug("{} pipeline stage: metadata element {} not supported, ignoring it", getId(), QNames
                        .getNodeQName(metadataElement));
            }
        }

        compInfo.setCompleteInstant();
        for (DomMetadata element : destinationCollection) {
            element.getMetadataInfo().put(compInfo);
        }

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
    protected void processEntitiesDescriptor(MetadataCollection<DomMetadata> metadataCollection,
            Element entitiesDescriptor, String validUntil, String cacheDuration) {
        String desciptorValidUntil = null;
        if (pushValidUntil) {
            desciptorValidUntil = Attributes
                    .getAttributeValue(entitiesDescriptor, SAMLConstants.VALID_UNTIL_ATTIB_NAME);
            if (desciptorValidUntil == null) {
                desciptorValidUntil = validUntil;
            }
        }

        String descriptorCacheDuration = null;
        if (pushCacheDuration) {
            descriptorCacheDuration = Attributes.getAttributeValue(entitiesDescriptor,
                    SAMLConstants.CACHE_DURATION_ATTRIB_NAME);
            if (descriptorCacheDuration == null) {
                descriptorCacheDuration = cacheDuration;
            }
        }

        List<Element> children = Elements.getChildElements(entitiesDescriptor);
        for (Element child : children) {
            if (Elements.isElementNamed(child, SAMLConstants.ENTITIES_DESCRIPTOR_NAME)) {
                processEntitiesDescriptor(metadataCollection, child, desciptorValidUntil, descriptorCacheDuration);
            }
            if (Elements.isElementNamed(child, SAMLConstants.ENTITY_DESCRIPTOR_NAME)) {
                metadataCollection.add(buildMetadataElement(child, desciptorValidUntil, descriptorCacheDuration));
            }
        }
    }

    /**
     * Builds a {@link DomMetadata} from the given EntityDescriptor element. If the EntityDescriptor does not
     * already have a validUntil or cacheDuration attribute these attributes are created and populated with the given
     * values.
     * 
     * @param entityDescriptor EntityDescriptor element to be made in to a {@link DomMetadata}
     * @param validUntil validUntil attribute value to be added to the EntityDescriptor if the attribute does not
     *            already exist
     * @param cacheDuration cacheDuration attribute value to be added to the EntityDescriptor if the attribute does not
     *            already exist
     * 
     * @return the constructed metadata element
     */
    protected DomMetadata buildMetadataElement(Element entityDescriptor, String validUntil, String cacheDuration) {
        String entityId = entityDescriptor.getAttributeNS(null, "entityID");
        if (validUntil != null && !Attributes.hasAttribute(entityDescriptor, SAMLConstants.VALID_UNTIL_ATTIB_NAME)) {
            log.debug("{} pipeline stage adding valid until of {} to entity descriptor {}", new Object[] { getId(),
                    validUntil, entityId });
            Attributes.appendAttribute(entityDescriptor, SAMLConstants.VALID_UNTIL_ATTIB_NAME, validUntil);
        }

        if (cacheDuration != null
                && !Attributes.hasAttribute(entityDescriptor, SAMLConstants.CACHE_DURATION_ATTRIB_NAME)) {
            log.debug("{} pipeline stage adding cacheDuration of {} to EntityDescriptor {}", new Object[] { getId(),
                    cacheDuration, entityId });
            Attributes.appendAttribute(entityDescriptor, SAMLConstants.CACHE_DURATION_ATTRIB_NAME, cacheDuration);
        }

        DomMetadata element = new DomMetadata(entityDescriptor);
        element.getMetadataInfo().put(new EntityIdInfo(entityDescriptor.getAttributeNS("null", entityId)));
        return new DomMetadata(entityDescriptor);
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        // nothing to do here
    }
}