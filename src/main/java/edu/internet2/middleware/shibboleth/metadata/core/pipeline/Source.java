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

import java.util.Map;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElement;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;

/**
 * A component of a {@link Pipeline} which produces the input to the pipeline.
 * 
 * @param <ElementType> type of metadata element which is produced by this source
 */
public interface Source<ElementType extends MetadataElement<?>> extends Component {

    /**
     * Produces the input to the {@link Pipeline}.
     * 
     * @param parameters parameters which <strong>may</strong> may be used to override initialization time parameters
     *            for this invocation
     * 
     * @return the information produced by the source
     */
    public MetadataElementCollection<ElementType> execute(Map<String, Object> parameters);
}