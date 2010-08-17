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

package edu.internet2.middleware.shibboleth.metadata.dom.stage;

import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.opensaml.util.Strings;
import org.opensaml.util.xml.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.ComponentInitializationException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.Stage;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.StageProcessingException;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;

/**
 * A pipeline stage which applies and XSLT to each element in the metadata collection.
 */
public class XSLTStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XSLTStage.class);

    /** Filesystem path to the XSL file. */
    private String xslFile;

    /** XSL template used to transform metadata. */
    private Templates xslTemplate;

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     * @param xslFilePath filesystem path the XSL file that will be used to transform each metadata element.
     */
    public XSLTStage(String stageId, String xslFilePath) {
        super(stageId);
        xslFile = xslFilePath;
    }

    /**
     * Gets the filesystem path of the XSL file.
     * 
     * @return filesystem path of the XSL file
     */
    public String getXslFile() {
        return xslFile;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> metadataCollection)
            throws StageProcessingException {

        SimpleMetadataCollection<DomMetadata> mec = new SimpleMetadataCollection<DomMetadata>();

        try {
            Transformer transform = xslTemplate.newTransformer();

            Element metadataElement;
            DOMResult result;
            List<Element> transformedElements;
            for (DomMetadata metadata : metadataCollection) {
                metadataElement = metadata.getMetadata();
                // we put things in a doc fragment, instead of new documents, because down the line
                // there is a good chance that at least some elements will get mashed together and this
                // may eliminate the need to adopt them in to other documents, an expensive operation
                result = new DOMResult(metadataElement.getOwnerDocument().createDocumentFragment());

                transform.transform(new DOMSource(metadataElement), result);

                transformedElements = Elements.getChildElements(result.getNode());
                for (Element transformedElement : transformedElements) {
                    mec.add(new DomMetadata(transformedElement));
                }
            }
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("XSL transofrmation engine misconfigured", e);
        } catch (TransformerException e) {
            throw new StageProcessingException("Unable to transform metadata element", e);
        }

        return mec;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        String trimmedXslFile = Strings.trimOrNull(xslFile);
        if (trimmedXslFile == null) {
            throw new ComponentInitializationException("XSL file path may not be null or empty");
        }

        TransformerFactory tfactory = TransformerFactory.newInstance();
        // TODO features and attributes

        try {
            log.debug("{} pipeline stage compiling XSL file {}", getId(), xslFile);
            xslTemplate = tfactory.newTemplates(new StreamSource(xslFile));
        } catch (TransformerConfigurationException e) {
            throw new ComponentInitializationException("XSL transformation engine misconfigured", e);
        }
    }
}