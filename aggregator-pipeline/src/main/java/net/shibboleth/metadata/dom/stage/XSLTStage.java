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
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.MetadataInfo;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.util.MetadataInfoHelper;

import org.opensaml.util.resource.Resource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.util.xml.ElementSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/** A pipeline stage which applies and XSLT to each element in the metadata collection. */
@ThreadSafe
public class XSLTStage extends BaseStage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XSLTStage.class);

    /** Resource that provides the XSL document. */
    private Resource xslResource;

    /** XSL template used to transform metadata. */
    private Templates xslTemplate;

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

    /** {@inheritDoc} */
    protected void doExecute(final Collection<DomMetadata> metadataCollection) throws StageProcessingException {

        try {
            final Transformer transform = xslTemplate.newTransformer();

            
            Element metadataElement;
            DOMResult result;
            List<Element> transformedElements;

            ArrayList<DomMetadata> newMetadataElements = new ArrayList<DomMetadata>();
            for (DomMetadata domMetadata: metadataCollection) {
                metadataElement = domMetadata.getMetadata();

                // we put things in a doc fragment, instead of new documents, because down the line
                // there is a good chance that at least some elements will get mashed together and this
                // may eliminate the need to adopt them in to other documents, an expensive operation
                result = new DOMResult(metadataElement.getOwnerDocument().createDocumentFragment());
                transform.transform(new DOMSource(metadataElement), result);

                // The result of the transform contains a number of Elements, each of which
                // becomes a new DomMetadata in the output collection carrying the same
                // MetadataInfo objects as the input.  The predominant case is for one
                // input element to be transformed into one output element, but it is
                // possible to have none, or many.
                transformedElements = ElementSupport.getChildElements(result.getNode());
                for (Element transformedElement : transformedElements) {
                    DomMetadata newMetadata = new DomMetadata(transformedElement);
                    MetadataInfoHelper.addToAll(newMetadata,
                            domMetadata.getMetadataInfo().values().toArray(new MetadataInfo[] {}));
                    newMetadataElements.add(newMetadata);
                }
            }
            metadataCollection.clear();
            metadataCollection.addAll(newMetadataElements);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("XSL transformation engine misconfigured", e);
        } catch (TransformerException e) {
            throw new StageProcessingException("Unable to transform metadata element", e);
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