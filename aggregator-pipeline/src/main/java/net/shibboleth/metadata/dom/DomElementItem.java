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

package net.shibboleth.metadata.dom;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.AbstractItem;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.util.ItemMetadataSupport;

import org.opensaml.util.Assert;
import org.opensaml.util.xml.ElementSupport;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link Item} whose data is a DOM, version 3, {@link Element}.
 * 
 * The {@link Element} wrapped by this {@link Item} is always the document element of the document that owns the
 * {@link Element}.
 */
@ThreadSafe
public class DomElementItem extends AbstractItem<Element> {

    /** Serial version UID. */
    private static final long serialVersionUID = -5122481126094725529L;

    /**
     * Constructor. The document element of the given document becomes the {@link Element} value for this item.
     * 
     * @param document document whose document element becomes the value for this Item; may not be null and must have a
     *            document element
     */
    public DomElementItem(final Document document) {
        super();

        Assert.isNotNull(document, "DOM Document can not be null");

        final Element docElement = document.getDocumentElement();
        Assert.isNotNull(docElement, "DOM Document Element may not be null");

        setData(document.getDocumentElement());
    }

    /**
     * Constructor. A new {@link Document} is created and the given {@link Element} is deep-imported in to the new
     * document via {@link Document#importNode(org.w3c.dom.Node, boolean), and the resultant {@link Element} is set as
     * the new document's root.
     * 
     * @param element element that is copied to become the value of this Item
     */
    public DomElementItem(final Element element) {
        super();

        Assert.isNotNull(element, "DOM Document Element may not be null");

        final DOMImplementation domImpl = element.getOwnerDocument().getImplementation();
        final Document newDocument = domImpl.createDocument(null, null, null);
        final Element newDocumentRoot = (Element) newDocument.importNode(element, true);
        ElementSupport.setDocumentElement(newDocument, newDocumentRoot);

        setData(newDocumentRoot);
    }

    /** {@inheritDoc} */
    public Item<Element> copy() {
        final DomElementItem clone = new DomElementItem(unwrap());
        ItemMetadataSupport.addToAll(clone, getItemMetadata().values().toArray(new ItemMetadata[] {}));
        return clone;
    }
}