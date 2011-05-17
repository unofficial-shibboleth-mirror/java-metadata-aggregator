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

package net.shibboleth.metadata.dom;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.AbstractItem;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.util.ItemMetadataSupport;

import org.w3c.dom.Element;

/** A {@link Item} whose data is a DOM, version 3, {@link Element}. */
@ThreadSafe
public class DomElementItem extends AbstractItem<Element> {

    /** Serial version UID. */
    private static final long serialVersionUID = 8677951541031584666L;

    /**
     * Constructor.
     * 
     * @param metadata DOM metadata element
     */
    public DomElementItem(final Element metadata) {
        super();
        setData(metadata);
    }

    /** {@inheritDoc} */
    public Item<Element> copy() {
        final Element domClone = (Element) unwrap().cloneNode(true);
        final DomElementItem clone = new DomElementItem(domClone);
        ItemMetadataSupport.addToAll(clone, getItemMetadata().values().toArray(new ItemMetadata[] {}));
        return clone;
    }
}