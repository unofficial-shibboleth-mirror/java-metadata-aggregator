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

package edu.internet2.middleware.shibboleth.metadata.core.pipeline;

import net.jcip.annotations.ThreadSafe;
import edu.internet2.middleware.shibboleth.metadata.core.Metadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;

/**
 * A component of a {@link edu.internet2.middleware.shibboleth.metadata.core.pipeline.Pipeline} which produces the input
 * to the pipeline.
 * 
 * Sources must be thread safe and reusable. They may cache some or all of their state between requests but if an
 * implementations does so it must ensure proper thread safety.
 * 
 * @param <ElementType> type of metadata element which is produced by this source
 */
@ThreadSafe
public interface Source<ElementType extends Metadata<?>> extends Component {

    /**
     * Produces the input to the {@link edu.internet2.middleware.shibboleth.metadata.core.pipeline.Pipeline}.
     * 
     * @return the information produced by the source
     * 
     * @throws SourceProcessingException thrown if there is a problem producing the initial metadata element collection
     */
    public MetadataCollection<ElementType> execute() throws SourceProcessingException;
}