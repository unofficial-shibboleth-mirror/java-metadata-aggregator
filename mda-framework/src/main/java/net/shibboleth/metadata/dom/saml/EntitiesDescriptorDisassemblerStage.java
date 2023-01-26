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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.xml.ElementSupport;
import net.shibboleth.shared.xml.QNameSupport;

/**
 * A pipeline stage that replaces any SAML EntitiesDescriptor found in the Item collection with the EntityDescriptor
 * elements contained therein.
 */
@ThreadSafe
public class EntitiesDescriptorDisassemblerStage extends AbstractStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntitiesDescriptorDisassemblerStage.class);

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<Element>> items) {
        // make a copy of the input collection and clear it so that we can iterate over
        // the copy and add to the provided collection
        final ArrayList<Item<Element>> newItems = new ArrayList<>(items);
        items.clear();

        Element element;
        for (final Item<Element> item : newItems) {
            element = item.unwrap();
            if (SAMLMetadataSupport.isEntitiesDescriptor(element)) {
                processEntitiesDescriptor(items, element);
            } else if (SAMLMetadataSupport.isEntityDescriptor(element)) {
                processEntityDescriptor(items, element);
            } else {
                log.debug("{} pipeline stage: DOM Element {} not supported, ignoring it", getId(),
                        QNameSupport.getNodeQName(element));
            }
        }
    }

    /**
     * Processes an EntitiesDescriptor element. All child EntityDescriptor elements are processed and
     * EntitiesDescriptors are run back through this method.
     * 
     * @param items collection to which EntityDescriptor metadata elements are added
     * @param entitiesDescriptor the EntitiesDescriptor to break down
     */
    protected void processEntitiesDescriptor(@Nonnull @NonnullElements final List<Item<Element>> items,
            @Nonnull final Element entitiesDescriptor) {

        final List<Element> children = ElementSupport.getChildElements(entitiesDescriptor);
        for (final Element child : children) {
            if (SAMLMetadataSupport.isEntitiesDescriptor(child)) {
                processEntitiesDescriptor(items, child);
            }
            if (SAMLMetadataSupport.isEntityDescriptor(child)) {
                processEntityDescriptor(items, child);
            }
        }
    }

    /**
     * Processes an EntityDescriptor element. Creates a {@link DOMElementItem} element and adds it to the item
     * collections.
     * 
     * @param items collection to which metadata is added
     * @param entityDescriptor entity descriptor to add to the item collection
     */
    protected void processEntityDescriptor(@Nonnull @NonnullElements final List<Item<Element>> items,
            @Nonnull final Element entityDescriptor) {
        final DOMElementItem item = new DOMElementItem(entityDescriptor);
        items.add(item);
    }
}
