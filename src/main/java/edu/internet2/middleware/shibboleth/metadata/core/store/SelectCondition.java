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

/**
 * Represents the condition used by a {@link Store} query to determine if a particular element should be returned in the
 * query result.
 * 
 * @param <ElementType> type of metadata element which is produced by this source
 */
@ThreadSafe
public interface SelectCondition<ElementType extends MetadataElement<?>> {

    /**
     * Determines whether the given element should be selected by the query.
     * 
     * @param element the current element being tested
     * 
     * @return true if the given element should be selected as a result from the query, false otherwise
     */
    public boolean isSelected(ElementType element);
}