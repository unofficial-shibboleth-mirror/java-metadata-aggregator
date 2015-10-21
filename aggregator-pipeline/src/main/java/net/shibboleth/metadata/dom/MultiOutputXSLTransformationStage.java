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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.util.ItemMetadataSupport;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.w3c.dom.Element;

/**
 * A pipeline stage which transforms each item in the {@link DOMElementItem} collection via an XSL transform. Each of
 * the input items may result in zero, one or more XML elements, each of which results in a {@link DOMElementItem} in
 * the resulting collection. The resulting {@link DOMElementItem}s receive {@link net.shibboleth.metadata.InfoStatus},
 * {@link net.shibboleth.metadata.WarningStatus}, and {@link net.shibboleth.metadata.ErrorStatus} metadata via the
 * {@link AbstractXSLProcessingStage.StatusInfoAppendingErrorListener}.
 */
@ThreadSafe
public class MultiOutputXSLTransformationStage extends AbstractXSLProcessingStage {

    /** {@inheritDoc} */
    @Override protected void executeTransformer(@Nonnull final Transformer transformer,
            @Nonnull @NonnullElements final Collection<Item<Element>> itemCollection) throws StageProcessingException,
            TransformerConfigurationException {

        try {
            final ArrayList<Item<Element>> newItems = new ArrayList<>();
            for (Item<Element> domItem : itemCollection) {
                transformer.setErrorListener(new StatusInfoAppendingErrorListener(domItem));
                final Element element = domItem.unwrap();

                // Collect the potentially multiple results from the transform in a document fragment.
                final DOMResult result = new DOMResult(element.getOwnerDocument().createDocumentFragment());
                transformer.transform(new DOMSource(element.getOwnerDocument()), result);

                // The document fragment contains a number of Elements, each of which
                // becomes a new DomElementItem in the output collection carrying the same
                // ItemMetadata objects as the input.
                final List<Element> transformedElements = ElementSupport.getChildElements(result.getNode());
                for (Element transformedElement : transformedElements) {
                    DOMElementItem newItem = new DOMElementItem(transformedElement);
                    ItemMetadataSupport.addAll(newItem, domItem.getItemMetadata().values());
                    newItems.add(newItem);
                }
            }
            itemCollection.clear();
            itemCollection.addAll(newItems);
        } catch (TransformerException e) {
            throw new StageProcessingException("Unable to transform DOM Element", e);
        }
    }
}