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

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.shibboleth.metadata.core.BasicMetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.stage.Stage;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadataElement;

/**
 * A pipeline stage that replaces any SAML EntitiesDescriptors found in the metadata collection and replaces them with
 * the EntityDescriptor elements contained therein.
 * 
 * TODO document what information is pulled off the EntitiesDescriptor and pushed in to the EntityDescriptors
 */
public class SAMLEntitySplitterStage extends AbstractComponent implements Stage<DomMetadataElement> {

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

    /** {@inheritDoc} */
    public MetadataElementCollection<DomMetadataElement> execute(Map<String, Object> parameters,
            MetadataElementCollection<DomMetadataElement> metadata) {
        BasicMetadataElementCollection<DomMetadataElement> metadataCollection = new BasicMetadataElementCollection<DomMetadataElement>();

        Element descriptor = metadata.iterator().next().getEntityMetadata();
        if (SAMLConstants.MD_NS.equals(descriptor.getNamespaceURI())) {
            if (SAMLConstants.ENTITIES_DESCRIPTOR_LOCAL_NAME.equals(descriptor.getLocalName())) {
                processEntitiesDescriptor(metadataCollection, descriptor, null, null);
            } else {
                metadataCollection.add(new DomMetadataElement(descriptor));
            }
        }

        return metadataCollection;
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
    protected void processEntitiesDescriptor(MetadataElementCollection<DomMetadataElement> metadataCollection,
            Element entitiesDescriptor, String validUntil, String cacheDuration) {
        String desciptorValidUntil = null;
        if (pushValidUntil) {
            if (entitiesDescriptor.hasAttributeNS(null, SAMLConstants.VALID_UNTIL_ATTIB_LOCAL_NAME)) {
                desciptorValidUntil = entitiesDescriptor.getAttributeNS(null,
                        SAMLConstants.VALID_UNTIL_ATTIB_LOCAL_NAME);
            } else {
                desciptorValidUntil = validUntil;
            }
        }

        String descriptorCacheDuration = null;
        if (pushCacheDuration) {
            if (entitiesDescriptor.hasAttributeNS(null, SAMLConstants.CACHE_DURATION_ATTRIB_LOCAL_NAME)) {
                descriptorCacheDuration = entitiesDescriptor.getAttributeNS(null,
                        SAMLConstants.CACHE_DURATION_ATTRIB_LOCAL_NAME);
            } else {
                descriptorCacheDuration = cacheDuration;
            }
        }

        NodeList children = entitiesDescriptor.getChildNodes();
        Node child;
        for (int i = 0; i < children.getLength(); i++) {
            child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (SAMLConstants.MD_NS.equals(child.getNamespaceURI())) {
                    if (SAMLConstants.ENTITIES_DESCRIPTOR_LOCAL_NAME.equals(child.getLocalName())) {
                        processEntitiesDescriptor(metadataCollection, (Element) child, desciptorValidUntil,
                                descriptorCacheDuration);
                    } else {
                        metadataCollection.add(buildMetadataElement((Element) child, desciptorValidUntil,
                                descriptorCacheDuration));
                    }
                }
            }
        }
    }

    /**
     * Builds a {@link DomMetadataElement} from the given EntityDescriptor element. If the EntityDescriptor does not
     * already have a validUntil or cacheDuration attribute these attributes are created and populated with the given
     * values.
     * 
     * @param entityDescriptor EntityDescriptor element to be made in to a {@link DomMetadataElement}
     * @param validUntil validUntil attribute value to be added to the EntityDescriptor if the attribute does not
     *            already exist
     * @param cacheDuration cacheDuration attribute value to be added to the EntityDescriptor if the attribute does not
     *            already exist
     * 
     * @return the constructed metadata element
     */
    protected DomMetadataElement buildMetadataElement(Element entityDescriptor, String validUntil, String cacheDuration) {
        if (validUntil != null && !entityDescriptor.hasAttributeNS(null, SAMLConstants.VALID_UNTIL_ATTIB_LOCAL_NAME)) {
            entityDescriptor.setAttributeNS(null, SAMLConstants.VALID_UNTIL_ATTIB_LOCAL_NAME, validUntil);
        }

        if (cacheDuration != null
                && !entityDescriptor.hasAttributeNS(null, SAMLConstants.CACHE_DURATION_ATTRIB_LOCAL_NAME)) {
            entityDescriptor.setAttributeNS(null, SAMLConstants.CACHE_DURATION_ATTRIB_LOCAL_NAME, cacheDuration);
        }

        return new DomMetadataElement(entityDescriptor);
    }
}