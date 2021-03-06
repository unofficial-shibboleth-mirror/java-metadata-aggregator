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

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

/**
 * A pipeline stage which transforms each item in the {@link DOMElementItem} collection via an XSL transform. Each item
 * is replaced by an item constructed from the result of the transform. The result {@link DOMElementItem} receives
 * {@link net.shibboleth.metadata.InfoStatus}, {@link net.shibboleth.metadata.WarningStatus}, and
 * {@link net.shibboleth.metadata.ErrorStatus} metadata via the
 * {@link AbstractXSLProcessingStage.StatusInfoAppendingErrorListener}.
 */
@ThreadSafe
public class XSLTransformationStage extends AbstractXSLProcessingStage {

    @Override
    protected void executeTransformer(@Nonnull final Transformer transformer,
            @Nonnull @NonnullElements final Collection<Item<Element>> items) throws StageProcessingException,
            TransformerConfigurationException {

        try {
            final ArrayList<Item<Element>> newItems = new ArrayList<>();
            for (final Item<Element> domItem : items) {
                transformer.setErrorListener(new StatusInfoAppendingErrorListener(domItem));
                final Element element = domItem.unwrap();

                // Create a new document to hold the result of the transform.
                final DOMImplementation domImpl = element.getOwnerDocument().getImplementation();
                final Document newDocument = domImpl.createDocument(null, null, null);

                // perform the transformation
                transformer.transform(new DOMSource(element.getOwnerDocument()), new DOMResult(newDocument));

                // Create the result Item and copy across the input's ItemMetadata objects.
                final Item<Element> newItem = new DOMElementItem(newDocument);
                newItem.getItemMetadata().putAll(domItem.getItemMetadata());
                newItems.add(newItem);
            }
            items.clear();
            items.addAll(newItems);
        } catch (final TransformerException e) {
            throw new StageProcessingException("Unable to transform DOM Element", e);
        }
    }
}
