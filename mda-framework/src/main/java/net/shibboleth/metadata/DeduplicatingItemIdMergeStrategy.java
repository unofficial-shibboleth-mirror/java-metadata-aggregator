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

import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A merge strategy that adds source items to the target collection if none of the Items in the target collection have
 * the same {@link ItemId} as source item. If the source item does not contain a {@link ItemId} it is always added to
 * the target collection.
 */
@ThreadSafe
public class DeduplicatingItemIdMergeStrategy implements CollectionMergeStrategy {

    @Override
    public <T> void merge(@Nonnull @NonnullElements final List<Item<T>> target,
            @Nonnull @NonnullElements final List<List<Item<T>>> sources) {
        Constraint.isNotNull(target, "Target collection can not be null");
        Constraint.isNotNull(sources, "Source collections can not be null or empty");
        
        final HashSet<ItemId> presentItemIds = new HashSet<>();

        for (final Item<T> item : target) {
            presentItemIds.addAll(item.getItemMetadata().get(ItemId.class));
        }

        for (final List<Item<T>> source : sources) {
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
     * @param <T> type of data contained in the items
     */
    private <T> void merge(@Nonnull @NonnullElements final HashSet<ItemId> presentItemIds,
            @Nonnull @NonnullElements final List<Item<T>> target,
            @Nonnull @NonnullElements final List<Item<T>> sourceItems) {
        for (final Item<T> sourceItem : sourceItems) {
            final var itemIds = sourceItem.getItemMetadata().get(ItemId.class);
            if (itemIds.isEmpty()) {
                target.add(sourceItem);
                continue;
            }

            var itemAlreadyPresent = false;
            for (final ItemId itemId : itemIds) {
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
