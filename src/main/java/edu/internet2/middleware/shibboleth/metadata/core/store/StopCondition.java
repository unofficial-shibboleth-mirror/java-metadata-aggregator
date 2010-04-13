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
 * Represents the condition under which a {@link Store} query should stop.
 * 
 * @param <ElementType> the metadata element type which is stored
 */
@ThreadSafe
public interface StopCondition<ElementType extends MetadataElement<?>> {

    /** An implementation of {@link StopCondition} that stops the search once a single result has been found. */
    public static final StopCondition<MetadataElement<?>> ONE_ELEMENT_SELECTED = new StopCondition<MetadataElement<?>>() {

        /** {@inheritDoc} */
        public boolean shouldStop(MetadataElementCollection<MetadataElement<?>> searchResults) {
            if (searchResults.size() == 1) {
                return true;
            }

            return false;
        }
    };

    /**
     * Determines whether a {@link Store} should stop return results.
     * 
     * @param searchResults the current queries results
     * 
     * @return true if the query should stop, otherwise false
     */
    public boolean shouldStop(MetadataElementCollection<ElementType> searchResults);
}