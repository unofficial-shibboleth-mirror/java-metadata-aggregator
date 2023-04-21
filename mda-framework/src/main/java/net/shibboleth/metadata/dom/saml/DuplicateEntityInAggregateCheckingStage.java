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

import java.util.HashSet;

import javax.annotation.Nonnull;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.shared.xml.AttributeSupport;

/**
 * A stage which detects duplication of <code>entityID</code> values in
 * SAML entity collections.
 *
 * <p>
 * Any duplicate <code>entityID</code> results in an {@link ErrorStatus}
 * being added to the item.
 * </p>
 *
 * <p>
 * Note that the stage operates on each item independently under the
 * assumption that the stage is to be presented with a single item containing an
 * <code>EntitiesDescriptor</code> aggregate of many individual entities as
 * a final validity check prior to publication.
 * </p>
 */
public class DuplicateEntityInAggregateCheckingStage extends AbstractIteratingStage<Element> {

    @Override
    protected void doExecute(final @Nonnull Item<Element> item)
            throws StageProcessingException {
        final @Nonnull Element element = item.unwrap();

        // List all the relevant elements in this document in document order
        final NodeList eList = element.getElementsByTagNameNS(SAMLMetadataSupport.MD_NS,
                SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME.getLocalPart());

        final @Nonnull var ids = new HashSet<String>();
        final @Nonnull var reported = new HashSet<String>();
        final var count = eList.getLength();
        for (int eIndex = 0; eIndex < count; eIndex++) {
            final Element entity = (Element) eList.item(eIndex);
            assert entity != null;
            final var id = AttributeSupport.getAttributeValue(entity, null, "entityID");
            if (id != null) {
                if (ids.contains(id)) {
                    // Report duplicate, but only once.
                    if (!reported.contains(id)) {
                        item.getItemMetadata().put(new ErrorStatus(ensureId(), "duplicate entityID: " + id));
                        reported.add(id);
                    }
                } else {
                    ids.add(id);
                }
            }
        }
    }

}
