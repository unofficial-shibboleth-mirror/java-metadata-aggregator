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

package edu.internet2.middleware.shibboleth.metadata.core.pipeline.stage;

import java.util.Map;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElement;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.Component;

/**
 * A stage in a {@link edu.internet2.middleware.shibboleth.metadata.core.pipeline.Pipeline} that transforms data in a
 * particular manner.
 * 
 * @param <ElementType> type of metadata element which is produced by this source
 */
public interface Stage<ElementType extends MetadataElement<?>> extends Component {

    /**
     * Transforms the given input data.
     * 
     * @param parameters parameters which <strong>may</strong> may be used to override initialization time parameters
     *            for this invocation
     * @param metadataCollection the data to be transformed
     * 
     * @return the output of the transformation
     * 
     * @throws PipelineStageException thrown if there is a problem running this stage on the given input
     */
    public MetadataElementCollection<ElementType> execute(Map<String, Object> parameters,
            MetadataElementCollection<ElementType> metadataCollection) throws PipelineStageException;
}