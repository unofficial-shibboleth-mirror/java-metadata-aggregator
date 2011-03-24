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

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.EntityIdInfo;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.BaseStage;

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
 * each metadata element.
 */
@ThreadSafe
public class EntitiesDescriptorDisassemblerStage extends BaseStage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntitiesDescriptorDisassemblerStage.class);

    /** {@inheritDoc} */
    protected void doExecute(final Collection<DomMetadata> metadataCollection) {
        // make a copy of the input collection and clear it so that we can iterate over
        // the copy and add to the provided collection
        ArrayList<DomMetadata> metadatas = new ArrayList<DomMetadata>(metadataCollection);
        metadataCollection.clear();

        Element metadataElement;
        for (DomMetadata metadata : metadatas) {
            metadataElement = metadata.getMetadata();
            if (MetadataHelper.isEntitiesDescriptor(metadataElement)) {
                processEntitiesDescriptor(metadataCollection, metadataElement);
            } else if (MetadataHelper.isEntityDescriptor(metadataElement)) {
                processEntityDescriptor(metadataCollection, metadataElement);
            } else {
                log.debug("{} pipeline stage: metadata element {} not supported, ignoring it", getId(),
                        QNameSupport.getNodeQName(metadataElement));
            }
        }
    }

    /**
     * Processes an EntitiesDescriptor element. All child EntityDescriptor elements are processed and
     * EntitiesDescriptors are run back through this method.
     * 
     * @param metadataCollection collection to which EntityDescriptor metadata elements are added
     * @param entitiesDescriptor the EntitiesDescriptor to break down
     */
    protected void processEntitiesDescriptor(final Collection<DomMetadata> metadataCollection,
            final Element entitiesDescriptor) {

        final List<Element> children = ElementSupport.getChildElements(entitiesDescriptor);
        for (Element child : children) {
            if (MetadataHelper.isEntitiesDescriptor(child)) {
                processEntitiesDescriptor(metadataCollection, child);
            }
            if (MetadataHelper.isEntityDescriptor(child)) {
                processEntityDescriptor(metadataCollection, child);
            }
        }
    }

    /**
     * Processes an EntityDescriptor element. Creates a {@link DomMetadata} element, adds it to the metadata
     * collections, and attaches a {@link EntityIdInfo} to it.
     * 
     * @param metadataCollection collection to which metadata is added
     * @param entityDescriptor entity descriptor to add to the metadata collection
     */
    protected void processEntityDescriptor(final Collection<DomMetadata> metadataCollection,
            final Element entityDescriptor) {
        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");
        final DomMetadata element = new DomMetadata(entityDescriptor);
        element.getMetadataInfo().put(new EntityIdInfo(entityId));
        metadataCollection.add(element);
    }
}