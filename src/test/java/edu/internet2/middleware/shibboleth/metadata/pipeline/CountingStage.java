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

import edu.internet2.middleware.shibboleth.metadata.Metadata;
import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.pipeline.ComponentInitializationException;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Stage;
import edu.internet2.middleware.shibboleth.metadata.pipeline.StageProcessingException;

public class CountingStage<MetadataType extends Metadata<?>> extends AbstractComponent implements Stage<MetadataType> {

    private int counter = 0;

    public CountingStage() {
        this("CountingStage");
    }

    public CountingStage(String id) {
        super(id);
    }

    public int getCount() {
        return counter;
    }

    public MetadataCollection<MetadataType> execute(MetadataCollection<MetadataType> metadataCollection)
            throws StageProcessingException {
        counter += 1;
        return metadataCollection;
    }

    protected void doInitialize() throws ComponentInitializationException {

    }
}