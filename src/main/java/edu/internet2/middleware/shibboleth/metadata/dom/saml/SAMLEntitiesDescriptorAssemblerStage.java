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

package edu.internet2.middleware.shibboleth.metadata.dom.saml;

import net.jcip.annotations.ThreadSafe;

import org.opensaml.util.xml.Elements;
import org.opensaml.util.xml.ParserPool;
import org.opensaml.util.xml.XMLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.ComponentInitializationException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.Stage;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;

/**
 * A {@link Stage} capable of assembling a collection of EntityDescriptor elements in to a single EntitiesDescriptor
 * element.
 */
@ThreadSafe
public class SAMLEntitiesDescriptorAssemblerStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(SAMLEntitiesDescriptorAssemblerStage.class);

    /** Pool of DOM parsers used to construct new {@link Document} objects to hold the assembled metadata. */
    ParserPool parserPool;

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     */
    public SAMLEntitiesDescriptorAssemblerStage(String stageId, ParserPool pool) {
        super(stageId);
        parserPool = pool;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> metadataCollection) {
        log.debug("{} pipeline stage fetching owning for metadata element", getId());

        Document owningDocument = null;
        try {
            owningDocument = parserPool.newDocument();
        } catch (XMLParserException e) {
            e.printStackTrace();
            return null;
        }

        Element entitiesDescriptor = buildEntitiesDescriptor(owningDocument);

        Element entityDescriptor;
        for (DomMetadata metadata : metadataCollection) {
            entityDescriptor = metadata.getMetadata();
            log.debug("{} pipeline stage adding EntityDescriptor {} to EntitiesDescriptor", getId(), entityDescriptor
                    .getAttributeNS(null, "entityID"));
            Elements.appendChildElement(entitiesDescriptor, entityDescriptor);
        }

        SimpleMetadataCollection<DomMetadata> mec = new SimpleMetadataCollection<DomMetadata>();
        mec.add(new DomMetadata(entitiesDescriptor));
        return mec;
    }

    /**
     * Builds the EntitiesDescriptor element that will receive the EntityDescriptor elements and makes it the document
     * root element.
     * 
     * @param owningDocument document which will own the EntitiesDescriptor element and all the root elements
     * 
     * @return the constructed EntitiesDescriptor element
     */
    protected Element buildEntitiesDescriptor(Document owningDocument) {
        Element entitiesDescriptor = Elements.constructElement(owningDocument, SAMLConstants.ENTITIES_DESCRIPTOR_NAME);

        // TODO namespace decl, name

        Elements.setDocumentElement(owningDocument, entitiesDescriptor);
        return entitiesDescriptor;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        // nothing to do here
    }
}