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

package net.shibboleth.metadata;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.util.ClassToInstanceMultiMap;

/**
 * Base implementation of an {@link Item}.
 * 
 * @param <T> type of data contained in the item
 */
@ThreadSafe
public abstract class AbstractItem<T> implements Item<T> {

    /** Serial version UID. */
    private static final long serialVersionUID = -3694943988855243697L;

    /** The actual data held by the item. */
    private T data;

    /** Additional processing information associated with this Item. */
    private final ClassToInstanceMultiMap<ItemMetadata> metadata;

    /** Constructor. */
    protected AbstractItem() {
        metadata = new ClassToInstanceMultiMap<ItemMetadata>(true);
    }

    /** {@inheritDoc} */
    public T unwrap() {
        return data;
    }

    /**
     * Sets the data wrapped by this Item.
     * 
     * @param newData the data
     */
    protected synchronized void setData(final T newData) {
        data = newData;
    }

    /** {@inheritDoc} */
    public ClassToInstanceMultiMap<ItemMetadata> getItemMetadata() {
        return metadata;
    }
}