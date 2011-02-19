/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInfo;
import net.shibboleth.metadata.pipeline.Stage;
import net.shibboleth.metadata.util.MetadataInfoHelper;

import org.opensaml.util.xml.ElementSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/** Filtering stage that removes ContactPerson elements from EntityDescriptors. */
public class RemoveContactPersonStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(RemoveContactPersonStage.class);

    /**
     * Filters out any ContactPerson elements found within EntityDescriptors.
     * 
     * @param metadataCollection collection of metadata on which to operate
     * 
     * @return the resulting, filtered, metadata collection
     */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> metadataCollection) {
        final ComponentInfo compInfo = new ComponentInfo(this);

        Element descriptor;
        for (DomMetadata metadata : metadataCollection) {
            descriptor = metadata.getMetadata();
            if (MetadataHelper.isEntitiesDescriptor(descriptor)) {
                processEntitiesDescriptor(descriptor);
            } else if (MetadataHelper.isEntityDescriptor(descriptor)) {
                processEntityDescriptor(descriptor);
            }
        }

        compInfo.setCompleteInstant();
        MetadataInfoHelper.addToAll(metadataCollection, compInfo);
        return metadataCollection;
    }

    /**
     * Iterates over all child EntitiesDescriptor, passing each to {@link #processEntitiesDescriptor(Element)}, and
     * EntityDescriptor, passing each to {@link #processEntityDescriptor(Element)}.
     * 
     * @param entitiesDescriptor EntitiesDescriptor being processed
     */
    protected void processEntitiesDescriptor(final Element entitiesDescriptor) {
        final List<Element> children = ElementSupport.getChildElements(entitiesDescriptor);
        for (Element child : children) {
            if (MetadataHelper.isEntitiesDescriptor(child)) {
                processEntitiesDescriptor(child);
            } else if (MetadataHelper.isEntityDescriptor(child)) {
                processEntityDescriptor(child);
            }
        }
    }

    /**
     * Processes an EntityDescriptor by removing any ContactPerson that is within it.
     * 
     * @param entityDescriptor entity descriptor being processed
     */
    protected void processEntityDescriptor(final Element entityDescriptor) {
        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");

        final List<Element> contactPersons = ElementSupport.getChildElementsByTagNameNS(entityDescriptor,
                MetadataHelper.MD_NS, "ContactPerson");
        if (!contactPersons.isEmpty()) {
            log.debug("{} pipeline stage filtering ContactPerson from EntityDescriptor {}", getId(), entityId);
            for (Element contactPerson : contactPersons) {
                entityDescriptor.removeChild(contactPerson);
            }
        }
    }
}