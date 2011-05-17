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

package net.shibboleth.metadata.dom.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.util.ItemMetadataSupport;

import org.opensaml.util.resource.Resource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.util.xml.ElementSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/** A pipeline stage which applies and XSLT to each element in the {@link DomElementItem} collection. */
@ThreadSafe
public class XSLTStage extends BaseStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XSLTStage.class);

    /** Resource that provides the XSL document. */
    private Resource xslResource;

    /** XSL template used to transform Elements. */
    private Templates xslTemplate;
    
    /**
     * Collection of named parameters to make available to the transform.
     * 
     * If not set, an empty collection.
     */
    private Map<String, Object> parameters = new HashMap<String, Object>();

    /**
     * Gets the resource that provides the XSL document.
     * 
     * @return resource that provides the XSL document
     */
    public Resource getXslResource() {
        return xslResource;
    }

    /**
     * Sets the resource that provides the XSL document.
     * 
     * @param resource resource that provides the XSL document
     */
    public synchronized void setXslResource(final Resource resource) {
        if (isInitialized()) {
            return;
        }
        xslResource = resource;
    }
    
    /**
     * Sets the named parameters for the transform.
     * 
     * @param parameterMap parameters for the transform
     */
    public synchronized void setParameters(Map<String, Object> parameterMap) {
        if (isInitialized()) {
            return;
        }
        this.parameters = parameterMap;
    }

    /** {@inheritDoc} */
    protected void doExecute(final Collection<DomElementItem> itemCollection) throws StageProcessingException {

        try {
            final Transformer transform = xslTemplate.newTransformer();

            // Pass any parameters through to the transform.
            for (Map.Entry<String, Object> entry: parameters.entrySet()) {
                transform.setParameter(entry.getKey(), entry.getValue());
            }
            
            Element element;
            DOMResult result;
            List<Element> transformedElements;

            ArrayList<DomElementItem> newItems = new ArrayList<DomElementItem>();
            for (DomElementItem domItem: itemCollection) {
                element = domItem.unwrap();

                // we put things in a doc fragment, instead of new documents, because down the line
                // there is a good chance that at least some elements will get mashed together and this
                // may eliminate the need to adopt them in to other documents, an expensive operation
                result = new DOMResult(element.getOwnerDocument().createDocumentFragment());
                transform.transform(new DOMSource(element), result);

                // The result of the transform contains a number of Elements, each of which
                // becomes a new DomElementItem in the output collection carrying the same
                // ItemMetadata objects as the input.  The predominant case is for one
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
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("XSL transformation engine misconfigured", e);
        } catch (TransformerException e) {
            throw new StageProcessingException("Unable to transform DOM Element", e);
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        if (xslResource == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", XslResource must not be null");
        }

        try {
            if (!xslResource.exists()) {
                throw new ComponentInitializationException("Unable to initialize " + getId() + ", XslResource "
                        + xslResource.getLocation() + " does not exist");
            }

            final TransformerFactory tfactory = TransformerFactory.newInstance();
            // TODO features and attributes

            log.debug("{} pipeline stage compiling XSL file {}", getId(), xslResource);
            xslTemplate = tfactory.newTemplates(new StreamSource(xslResource.getInputStream()));
        } catch (TransformerConfigurationException e) {
            throw new ComponentInitializationException("XSL transformation engine misconfigured", e);
        } catch (ResourceException e) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", error reading XslResource " + xslResource.getLocation() + " information", e);
        }
    }
}