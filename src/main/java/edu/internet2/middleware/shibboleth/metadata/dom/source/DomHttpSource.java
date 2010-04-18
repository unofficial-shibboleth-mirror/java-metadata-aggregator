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

package edu.internet2.middleware.shibboleth.metadata.dom.source;

import java.util.Map;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.PipelineInitializationException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.source.Source;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadataElement;

/**
 * A pipeline source which reads an XML document from an HTTP source, parses the document, and returns the resultant
 * document (root) element as the metadata within the returned collection.
 */
public class DomHttpSource extends AbstractComponent implements Source<DomMetadataElement> {

    /**
     * Constructor.
     * 
     * @param sourceId unique ID of this source
     */
    public DomHttpSource(String sourceId) {
        super(sourceId);
    }

    /** {@inheritDoc} */
    public MetadataElementCollection<DomMetadataElement> execute(Map<String, Object> parameters) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /** {@inheritDoc} */
    protected void doInitialize() throws PipelineInitializationException {
        // TODO Auto-generated method stub
        
    }
}