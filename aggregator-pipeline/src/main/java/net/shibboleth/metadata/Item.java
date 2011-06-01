/*
 * Licensed to the University Corporation for Advanced Internet Development, Inc.
 * under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata;

import java.io.Serializable;

import net.shibboleth.metadata.util.ClassToInstanceMultiMap;

/**
 * A wrapper around a piece of information processed by pipeline stages.
 * 
 * @param <T> type of metadata element
 */
public interface Item<T> extends Serializable {

    /**
     * Gets the wrapped item data.
     * 
     * @return the wrapped item data
     */
    public T unwrap();

    /**
     * Gets all of the metadata attached to this Item.
     * 
     * @return metadata attached to this Item
     */
    public ClassToInstanceMultiMap<ItemMetadata> getItemMetadata();

    /**
     * Performs a copy of this Item. All member fields, except {@link ItemMetadata}, should be deep cloned.
     * {@link ItemMetadata} objects must be shared between the clone and the original.
     * 
     * @return the clone of this element
     */
    public Item<T> copy();
}