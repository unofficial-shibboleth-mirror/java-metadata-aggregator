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

package edu.internet2.middleware.shibboleth.metadata.core.store;

import net.jcip.annotations.ThreadSafe;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElement;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;

/**
 * A store of metadata which may be queried.
 * 
 * @param <ElementType> type of metadata upon which the store operates
 */
@ThreadSafe
public interface Store<ElementType extends MetadataElement<?>> {

    /**
     * Gets the metadata present in this store.
     * 
     * @return the metadata present in this store
     */
    public MetadataElementCollection<ElementType> getMetadata();

    /**
     * Searches the collection of metadata within the store.
     * 
     * @param select the criteria that determines if a given {@link MetadataElement} is returned in the result set
     * @param stop the criteria that determines that a given search is completed
     * 
     * @return the non-null results of the search
     */
    public MetadataElementCollection<ElementType> searchMetadata(SelectCondition<ElementType> select,
            StopCondition<ElementType> stop);

}