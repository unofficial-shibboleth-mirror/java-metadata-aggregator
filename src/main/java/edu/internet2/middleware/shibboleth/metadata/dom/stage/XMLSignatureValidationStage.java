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

import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.ComponentInitializationException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.Stage;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;

/**
 * A pipeline stage which validates the XML digital signature found on the metadata elements.
 */
public class XMLSignatureValidationStage extends AbstractComponent implements Stage<DomMetadata> {

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     */
    public XMLSignatureValidationStage(String stageId) {
        super(stageId);
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> metadata) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        // TODO Auto-generated method stub

    }
}