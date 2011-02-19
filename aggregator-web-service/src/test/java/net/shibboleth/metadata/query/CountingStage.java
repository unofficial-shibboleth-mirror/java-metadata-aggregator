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

package net.shibboleth.metadata.query;

import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.Stage;
import net.shibboleth.metadata.pipeline.StageProcessingException;

public class CountingStage<MetadataType extends Metadata<?>> extends AbstractComponent implements Stage<MetadataType> {

    private int counter = 0;

    public CountingStage() {
        setId("CountingStage");
    }

    public int getCount() {
        return counter;
    }

    public MetadataCollection<MetadataType> execute(final MetadataCollection<MetadataType> metadataCollection)
            throws StageProcessingException {
        counter += 1;
        return metadataCollection;
    }
}