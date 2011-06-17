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
import java.util.Collection;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseStage;

import org.opensaml.util.xml.ElementSupport;
import org.opensaml.util.xml.QNameSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A pipeline stage that replaces any SAML EntitiesDescriptor found in the Item collection with the EntityDescriptor
 * elements contained therein.
 */
@ThreadSafe
public class EntitiesDescriptorDisassemblerStage extends BaseStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntitiesDescriptorDisassemblerStage.class);

    /** {@inheritDoc} */
    protected void doExecute(final Collection<DomElementItem> itemCollection) {
        // make a copy of the input collection and clear it so that we can iterate over
        // the copy and add to the provided collection
        ArrayList<DomElementItem> items = new ArrayList<DomElementItem>(itemCollection);
        itemCollection.clear();

        Element element;
        for (DomElementItem item : items) {
            element = item.unwrap();
            if (MetadataHelper.isEntitiesDescriptor(element)) {
                processEntitiesDescriptor(itemCollection, element);
            } else if (MetadataHelper.isEntityDescriptor(element)) {
                processEntityDescriptor(itemCollection, element);
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
     * @param itemCollection collection to which EntityDescriptor metadata elements are added
     * @param entitiesDescriptor the EntitiesDescriptor to break down
     */
    protected void processEntitiesDescriptor(final Collection<DomElementItem> itemCollection,
            final Element entitiesDescriptor) {

        final List<Element> children = ElementSupport.getChildElements(entitiesDescriptor);
        for (Element child : children) {
            if (MetadataHelper.isEntitiesDescriptor(child)) {
                processEntitiesDescriptor(itemCollection, child);
            }
            if (MetadataHelper.isEntityDescriptor(child)) {
                processEntityDescriptor(itemCollection, child);
            }
        }
    }

    /**
     * Processes an EntityDescriptor element. Creates a {@link DomElementItem} element and adds it to the item
     * collections.
     * 
     * @param itemCollection collection to which metadata is added
     * @param entityDescriptor entity descriptor to add to the item collection
     */
    protected void processEntityDescriptor(final Collection<DomElementItem> itemCollection,
            final Element entityDescriptor) {
        final DomElementItem item = new DomElementItem(entityDescriptor);
        itemCollection.add(item);
    }
}