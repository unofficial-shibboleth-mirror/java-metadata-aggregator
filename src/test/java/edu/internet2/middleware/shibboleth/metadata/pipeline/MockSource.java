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

package edu.internet2.middleware.shibboleth.metadata.pipeline;

import edu.internet2.middleware.shibboleth.metadata.MockMetadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.ComponentInitializationException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.Source;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.SourceProcessingException;

public class MockSource extends AbstractComponent implements Source<MockMetadata> {

    private MetadataCollection<MockMetadata> source;

    public MockSource(MockMetadata... metadatas) {
        this("MockSource", metadatas);
    }

    public MockSource(String id, MockMetadata... metadatas) {
        super(id);
        source = new SimpleMetadataCollection<MockMetadata>();
        for (MockMetadata md : metadatas) {
            source.add(md);
        }
    }

    public MetadataCollection<MockMetadata> execute() throws SourceProcessingException {
        return source;
    }

    protected void doInitialize() throws ComponentInitializationException {

    }
}