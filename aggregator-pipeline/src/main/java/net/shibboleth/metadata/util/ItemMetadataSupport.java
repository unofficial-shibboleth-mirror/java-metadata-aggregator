/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
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

package net.shibboleth.metadata.util;

import java.util.Collection;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;

/** Helper class for dealing with {@link ItemMetadata} operations. */
public final class ItemMetadataSupport {

    /** Constructor. */
    private ItemMetadataSupport() {

    }

    /**
     * Adds all the give {@link ItemMetadata} items to each {@link Item} element in the given collection.
     * 
     * @param itemCollection collection of {@link Item} elements
     * @param metadatas collection of {@link ItemMetadata} items to be added to each {@link Item} element of the given
     *            collection
     */
    public static void addToAll(final Collection<? extends Item> itemCollection, final ItemMetadata... metadatas) {
        if (itemCollection == null || metadatas == null || metadatas.length == 0) {
            return;
        }

        for (Item<?> metadata : itemCollection) {
            addToAll(metadata, metadatas);
        }
    }

    /**
     * Adds all the given {@link ItemMetadata} items to the given {@link Item} element.
     * 
     * @param item element to which {@link ItemMetadata} will be added
     * @param metadatas {@link ItemMetadata} to be added to the metadata element
     */
    public static void addToAll(final Item<?> item, final ItemMetadata... metadatas) {
        if (item == null || metadatas == null || metadatas.length == 0) {
            return;
        }

        for (ItemMetadata info : metadatas) {
            item.getItemMetadata().put(info);
        }
    }

    /**
     * Adds one or more {@link ItemMetadata} to the given collection if the collection is an instance of
     * {@link ItemCollectionWithMetadata}.
     * 
     * @param itemCollection collection to which the metadata may be added
     * @param itemMetadatas the metadata which may be added to the collection
     */
    public static void addToCollection(final Collection<? extends Item> itemCollection,
            final ItemMetadata... itemMetadatas) {
        if (itemCollection == null || !(itemCollection instanceof ItemCollectionWithMetadata) || itemMetadatas == null
                || itemMetadatas.length == 0) {
            return;
        }

        ItemCollectionWithMetadata collection = (ItemCollectionWithMetadata) itemCollection;
        for (ItemMetadata metadata : itemMetadatas) {
            collection.getCollectionMetadata().put(metadata);
        }
    }
}