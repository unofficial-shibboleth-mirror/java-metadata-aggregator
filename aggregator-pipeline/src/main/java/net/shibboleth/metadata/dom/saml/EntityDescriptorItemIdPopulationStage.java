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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;

/**
 * A stage which, for each EntityDescriptor collection element, adds an {@link ItemId}, with the entity's entity ID, to
 * the metadata item.
 */
@ThreadSafe
public class EntityDescriptorItemIdPopulationStage extends AbstractIteratingStage<Element> {

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element metadataElement = item.unwrap();

        if (SAMLMetadataSupport.isEntityDescriptor(metadataElement)) {
            final String entityId = AttributeSupport.getAttributeValue(metadataElement, null, "entityID");
            item.getItemMetadata().put(new ItemId(entityId));
        }
    }
}
