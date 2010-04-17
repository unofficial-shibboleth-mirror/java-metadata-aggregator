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

import java.util.Map;

import org.opensaml.util.xml.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.core.BasicMetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.stage.Stage;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadataElement;

/**
 * A {@link Stage} capable of assembling a collection of EntityDescriptor elements in to a single EntitiesDescriptor
 * element.
 */
public class SAMLEntitiesDescriptorAssemblerStage extends AbstractComponent implements Stage<DomMetadataElement> {

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     */
    public SAMLEntitiesDescriptorAssemblerStage(String stageId) {
        super(stageId);
    }

    /** {@inheritDoc} */
    public MetadataElementCollection<DomMetadataElement> execute(Map<String, Object> parameters,
            MetadataElementCollection<DomMetadataElement> metadata) {

        Document owningDocument = metadata.iterator().next().getEntityMetadata().getOwnerDocument();

        Element entitiesDescriptor = buildEntitiesDescriptor(owningDocument);

        for (DomMetadataElement metadataElement : metadata) {
            Elements.appendChildElement(entitiesDescriptor, metadataElement.getEntityMetadata());
        }

        BasicMetadataElementCollection<DomMetadataElement> mec = new BasicMetadataElementCollection<DomMetadataElement>();
        mec.add(new DomMetadataElement(entitiesDescriptor));
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
}