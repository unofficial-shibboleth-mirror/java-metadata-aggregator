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

import java.util.List;

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.xml.ElementSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/** Filtering stage that removes ContactPerson elements from EntityDescriptors. */
public class RemoveContactPersonStage extends BaseIteratingStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(RemoveContactPersonStage.class);

    /** {@inheritDoc} */
    protected boolean doExecute(DomElementItem item) throws StageProcessingException {
        Element descriptor = item.unwrap();
        if (SamlMetadataSupport.isEntitiesDescriptor(descriptor)) {
            processEntitiesDescriptor(descriptor);
        } else if (SamlMetadataSupport.isEntityDescriptor(descriptor)) {
            processEntityDescriptor(descriptor);
        }
        return true;
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
            if (SamlMetadataSupport.isEntitiesDescriptor(child)) {
                processEntitiesDescriptor(child);
            } else if (SamlMetadataSupport.isEntityDescriptor(child)) {
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
                SamlMetadataSupport.MD_NS, "ContactPerson");
        if (!contactPersons.isEmpty()) {
            log.debug("{} pipeline stage filtering ContactPerson from EntityDescriptor {}", getId(), entityId);
            for (Element contactPerson : contactPersons) {
                entityDescriptor.removeChild(contactPerson);
            }
        }
    }
}