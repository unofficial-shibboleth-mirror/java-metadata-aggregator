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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.AbstractItem;
import net.shibboleth.metadata.Item;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.xml.ElementSupport;

/**
 * A {@link Item} whose data is a DOM, version 3, {@link Element}.
 * 
 * The {@link Element} wrapped by this {@link Item} is always the document element of the document that owns the
 * {@link Element}.
 */
@NotThreadSafe
public class DOMElementItem extends AbstractItem<Element> {

    /**
     * Constructor.
     * 
     * <p>
     * The document element of the given document becomes the {@link Element} value for this item.
     * </p>
     * 
     * @param document document whose document element becomes the value for this Item; may not be null and must have a
     *            document element
     */
    public DOMElementItem(@Nonnull final Document document) {
        super(processDocument(document));
    }

    /**
     * Constructor.
     * 
     * <p>
     * A new {@link Document} is created and the given {@link Element} is deep-imported in to the new
     * document via {@link Document#importNode(org.w3c.dom.Node, boolean)}. The resulting {@link Element} is set as
     * the new document's root.
     * </p>
     * 
     * @param element element that is copied to become the value of this Item
     */
    public DOMElementItem(@Nonnull final Element element) {
        super(processElement(element));
    }

    /**
     * Process a {@link Document} for wrapping by an {@code Item}.
     *
     * <p>
     * The {@code Item} simply wraps the {@link Document}'s document element.
     * </p>
     *
     * @param document {@link Document} to wrap
     * @return processed element
     */
    @Nonnull private static Element processDocument(@Nonnull final Document document) {
        Constraint.isNotNull(document, "DOM Document can not be null");
        
        final Element docElement = document.getDocumentElement();
        Constraint.isNotNull(docElement, "DOM Document Element may not be null");

        return docElement;
    }

    /**
     * Process an {@link Element} for wrapping by an {@code Item}.
     *
     * <p>
     * A new {@link Document} is created and the given {@link Element} is deep-imported in to the new
     * document via {@link Document#importNode(org.w3c.dom.Node, boolean)}. The resulting {@link Element} is set as
     * the new document's root.
     * </p>
     *
     * @param element {@link Element} to process
     * @return processed element
     */
    @Nonnull private static Element processElement(@Nonnull final Element element) {
        Constraint.isNotNull(element, "DOM Document Element may not be null");

        final DOMImplementation domImpl = element.getOwnerDocument().getImplementation();
        final Document newDocument = domImpl.createDocument(null, null, null);
        final Element newDocumentRoot = (Element) newDocument.importNode(element, true);
        ElementSupport.setDocumentElement(newDocument, newDocumentRoot);

        return newDocumentRoot;
    }

    @Override
    public Item<Element> copy() {
        final DOMElementItem clone = new DOMElementItem(unwrap());
        clone.getItemMetadata().putAll(getItemMetadata());
        return clone;
    }
}
