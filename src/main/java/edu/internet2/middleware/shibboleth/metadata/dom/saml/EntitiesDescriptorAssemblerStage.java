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

import org.opensaml.util.xml.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;
import edu.internet2.middleware.shibboleth.metadata.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Stage;

/**
 * A {@link Stage} capable of assembling a collection of EntityDescriptor elements in to a single EntitiesDescriptor
 * element.
 */
@ThreadSafe
public class EntitiesDescriptorAssemblerStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntitiesDescriptorAssemblerStage.class);

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     */
    public EntitiesDescriptorAssemblerStage(String stageId, ParserPool pool) {
        super(stageId);
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> metadataCollection) {
        log.debug("{} pipeline stage fetching owning for metadata element", getId());

        Element entitiesDescriptor = MetadataHelper.buildEntitiesDescriptor(metadataCollection);
        SimpleMetadataCollection<DomMetadata> mec = new SimpleMetadataCollection<DomMetadata>();
        mec.add(new DomMetadata(entitiesDescriptor));
        
        return mec;
    }
}