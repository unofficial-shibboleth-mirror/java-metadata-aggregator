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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.util.ItemMetadataSupport;

import org.opensaml.util.xml.ElementSupport;
import org.w3c.dom.Element;

/**
 * A pipeline stage which transforms each element in the {@link DomElementItem} collection via an XSL transform. The
 * element that served as the source of the XSLT is replaced by the elements that result from the transform. The results
 * elements receives {@link net.shibboleth.metadata.InfoStatus}, {@link net.shibboleth.metadata.WarningStatus}, and
 * {@link net.shibboleth.metadata.ErrorStatus} metadata via the {@link StatusInfoAppendingErrorListener}.
 */
@ThreadSafe
public class XSLTransformationStage extends AbstractXSLProcessingStage {

    /** {@inheritDoc} */
    protected void executeTransformer(Transformer transformer, Collection<DomElementItem> itemCollection)
            throws StageProcessingException, TransformerConfigurationException {

        try {
            Element element;
            DOMResult result;
            List<Element> transformedElements;

            ArrayList<DomElementItem> newItems = new ArrayList<DomElementItem>();
            for (DomElementItem domItem : itemCollection) {
                transformer.setErrorListener(new StatusInfoAppendingErrorListener(domItem));
                element = domItem.unwrap();

                // we put things in a doc fragment, instead of new documents, because down the line
                // there is a good chance that at least some elements will get mashed together and this
                // may eliminate the need to adopt them in to other documents, an expensive operation
                result = new DOMResult(element.getOwnerDocument().createDocumentFragment());
                transformer.transform(new DOMSource(element), result);

                // The result of the transform contains a number of Elements, each of which
                // becomes a new DomElementItem in the output collection carrying the same
                // ItemMetadata objects as the input. The predominant case is for one
                // input element to be transformed into one output element, but it is
                // possible to have none, or many.
                transformedElements = ElementSupport.getChildElements(result.getNode());
                for (Element transformedElement : transformedElements) {
                    DomElementItem newItem = new DomElementItem(transformedElement);
                    ItemMetadataSupport.addToAll(newItem,
                            domItem.getItemMetadata().values().toArray(new ItemMetadata[] {}));
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