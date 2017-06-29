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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/** Filtering stage that removes Organization elements from EntityDescriptors. */
@ThreadSafe
public class RemoveOrganizationStage extends BaseIteratingStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ContactPersonFilterStage.class);

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element descriptor = item.unwrap();
        if (SAMLMetadataSupport.isEntitiesDescriptor(descriptor)) {
            processEntitiesDescriptor(descriptor);
        } else if (SAMLMetadataSupport.isEntityDescriptor(descriptor)) {
            processEntityDescriptor(descriptor);
        }
    }

    /**
     * Iterates over all child EntitiesDescriptor, passing each to {@link #processEntitiesDescriptor(Element)}, and
     * EntityDescriptor, passing each to {@link #processEntityDescriptor(Element)}.
     * 
     * @param entitiesDescriptor EntitiesDescriptor being processed
     */
    protected void processEntitiesDescriptor(@Nonnull final Element entitiesDescriptor) {
        final List<Element> children = ElementSupport.getChildElements(entitiesDescriptor);
        for (final Element child : children) {
            if (SAMLMetadataSupport.isEntitiesDescriptor(child)) {
                processEntitiesDescriptor(child);
            } else if (SAMLMetadataSupport.isEntityDescriptor(child)) {
                processEntityDescriptor(child);
            }
        }
    }

    /**
     * Processes an EntityDescriptor by removing any Organization that is within it.
     * 
     * @param entityDescriptor entity descriptor being processed
     */
    protected void processEntityDescriptor(@Nonnull final Element entityDescriptor) {
        final String entityId = entityDescriptor.getAttributeNS(null, "entityID");

        final List<Element> organizations =
                ElementSupport.getChildElementsByTagNameNS(entityDescriptor, SAMLMetadataSupport.MD_NS, "Organization");
        if (!organizations.isEmpty()) {
            log.debug("{} pipeline stage filtering Organization from EntityDescriptor {}", getId(), entityId);
            for (final Element organization : organizations) {
                entityDescriptor.removeChild(organization);
            }
        }
    }
}
