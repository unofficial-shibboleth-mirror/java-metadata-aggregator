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

package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.shibboleth.metadata.FirstItemIdItemIdentificationStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemIdentificationStrategy;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.utilities.java.support.logic.Assert;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * A {@link Stage} that selects Items for further processing if they have a specific type of {@link ItemMetadata}
 * attached to them.
 */
public abstract class AbstractItemMetadataSelectionStage extends BaseStage<Item<?>> {

    /** {@link ItemMetadata} classes that, if the an {@Item} contains, will cause the {@link Item} to be selected. */
    private Collection<Class<ItemMetadata>> selectionRequirements = Collections.emptyList();

    /** Strategy used to generate {@link Item} identifiers for logging purposes. */
    private ItemIdentificationStrategy identifierStrategy = new FirstItemIdItemIdentificationStrategy();

    /**
     * Gets the {@link ItemMetadata} classes that, if the an {@Item} contains, will cause the {@link Item} to be
     * selected.
     * 
     * @return {@link ItemMetadata} classes that, if the an {@Item} contains, will cause the {@link Item} to be
     *         selected, never null nor containing null elements
     */
    public Collection<Class<ItemMetadata>> getSelectionRequirements() {
        return selectionRequirements;
    }

    /**
     * Sets the {@link ItemMetadata} classes that, if the an {@Item} contains, will cause the {@link Item} to be
     * selected.
     * 
     * @param requirements {@link ItemMetadata} classes that, if the an {@Item} contains, will cause the
     *            {@link Item} to be selected, may be null or contain null elements
     */
    public synchronized void setSelectionRequirements(Collection<Class<ItemMetadata>> requirements) {
        if (isInitialized()) {
            return;
        }
        selectionRequirements = ImmutableList.copyOf(Iterables.filter(requirements, Predicates.notNull()));
    }

    /**
     * Gets the strategy used to generate {@link Item} identifiers for logging purposes.
     * 
     * @return strategy used to generate {@link Item} identifiers for logging purposes
     */
    public ItemIdentificationStrategy getItemIdentifierStrategy() {
        return identifierStrategy;
    }

    /**
     * Sets the strategy used to generate {@link Item} identifiers for logging purposes.
     * 
     * @param strategy strategy used to generate {@link Item} identifiers for logging purposes, can not be null
     */
    public synchronized void setIdentifierStrategy(ItemIdentificationStrategy strategy) {
        if (isInitialized()) {
            return;
        }

        identifierStrategy = Assert.isNotNull(strategy, "Item identification strategy can not be null");
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<Item<?>> itemCollection) throws StageProcessingException {
        // we make a defensive copy here in case logic in the delegate #doExecute makes changes
        // to the itemCollection and thus would cause issues if we were iterating over it directly
        ArrayList<Item<?>> collectionCopy = new ArrayList<Item<?>>(itemCollection);

        HashMap<Class<? extends ItemMetadata>, List<? extends ItemMetadata>> matchingMetadata;
        for (Item<?> item : collectionCopy) {
            matchingMetadata = new HashMap<Class<? extends ItemMetadata>, List<? extends ItemMetadata>>();

            for (Class<ItemMetadata> infoClass : selectionRequirements) {
                if (item.getItemMetadata().containsKey(infoClass)) {
                    matchingMetadata.put(infoClass, item.getItemMetadata().get(infoClass));
                }
            }

            if (!matchingMetadata.isEmpty()) {
                doExecute(itemCollection, item, matchingMetadata);
            }
        }
    }

    /**
     * Performs the stage's logic on the given item that contained metadata of the given type.
     * 
     * @param itemCollection current item collection
     * @param matchingItem matching item
     * @param matchingMetadata all the {@link ItemMetadata} instances that match a selection criteria
     * 
     * @throws StageProcessingException thrown if there is a problem processing the item
     */
    protected abstract void doExecute(Collection<Item<?>> itemCollection, Item<?> matchingItem,
            Map<Class<? extends ItemMetadata>, List<? extends ItemMetadata>> matchingMetadata)
            throws StageProcessingException;
}