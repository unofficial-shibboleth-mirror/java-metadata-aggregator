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

package net.shibboleth.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A merge strategy that adds source items to the target collection if none of the Items in the target collection have
 * the same {@link ItemId} as source item. If the source item does not contain a {@link ItemId} it is always added to
 * the target collection.
 */
public class DeduplicatingItemIdMergeStrategy implements CollectionMergeStrategy {

    /** {@inheritDoc} */
    public void mergeCollection(Collection<Item<?>> target, Collection<Item<?>>... sources) {
        List<ItemId> itemIds;
        HashSet<ItemId> presentItemIds = new HashSet<ItemId>();

        for (Item item : target) {
            itemIds = item.getItemMetadata().get(ItemId.class);
            if (itemIds != null) {
                presentItemIds.addAll(itemIds);
            }
        }

        for (Collection<Item<?>> source : sources) {
            merge(presentItemIds, target, source);
        }
    }

    /**
     * Adds source items to the target collection if none of the Items in the target collection have the same
     * {@link ItemId} as source item. If the source item does not contain a {@link ItemId} it is always added to the
     * target collection.
     * 
     * @param presentItemIds IDs that are already present in the target collection
     * @param target the collection to which items will be merged in to
     * @param sourceItems the collection of items to be merged in to the target
     */
    private void merge(HashSet<ItemId> presentItemIds, Collection<Item<?>> target, Collection<Item<?>> sourceItems) {
        boolean itemAlreadyPresent;
        List<ItemId> itemIds;
        for (Item sourceItem : sourceItems) {
            itemIds = sourceItem.getItemMetadata().get(ItemId.class);
            if (itemIds == null || itemIds.isEmpty()) {
                target.add(sourceItem);
                continue;
            }

            itemAlreadyPresent = false;
            for (ItemId itemId : itemIds) {
                if (presentItemIds.contains(itemId)) {
                    itemAlreadyPresent = true;
                    break;
                }
            }

            if (!itemAlreadyPresent) {
                target.add(sourceItem);
                presentItemIds.addAll(itemIds);
            }
        }
    }
}