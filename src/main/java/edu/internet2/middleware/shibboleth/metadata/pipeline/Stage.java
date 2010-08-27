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

import net.jcip.annotations.ThreadSafe;
import edu.internet2.middleware.shibboleth.metadata.Metadata;
import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;

/**
 * A stage in a {@link edu.internet2.middleware.shibboleth.metadata.pipeline.Pipeline} that transforms data in a
 * particular manner.
 * 
 * Stages must be thread safe and reusable.
 * 
 * @param <ElementType> type of metadata element which is produced by this source
 */
@ThreadSafe
public interface Stage<ElementType extends Metadata<?>> extends Component {

    /**
     * Transforms the given input data.
     * 
     * @param metadataCollection the data to be transformed
     * 
     * @return the output of the transformation
     * 
     * @throws StageProcessingException thrown if there is a problem running this stage on the given input
     */
    public MetadataCollection<ElementType> execute(MetadataCollection<ElementType> metadataCollection)
            throws StageProcessingException;
}