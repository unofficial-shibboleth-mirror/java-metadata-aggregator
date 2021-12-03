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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.collection.ClassToInstanceMultiMap;

/**
 * A {@link Stage} that filters out {@link Item} if they have a specific type of {@link ItemMetadata} attached to them.
 * 
 * This is useful, for example, in removing all {@link Item} elements which have an associated
 * {@link net.shibboleth.metadata.ErrorStatus}.
 * 
 * @param <T> type of items the stage operates on
 */
@ThreadSafe
public class ItemMetadataFilterStage<T> extends AbstractItemMetadataSelectionStage<T, ItemMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ItemMetadataFilterStage.class);

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items,
            @Nonnull final Item<T> matchingItem,
            @Nonnull @NonnullElements final ClassToInstanceMultiMap<ItemMetadata> matchingMetadata)
            throws StageProcessingException {

        final String itemId = getItemIdentificationStrategy().getItemIdentifier(matchingItem);
        log.debug("Item {} was removed because it was marked with {}", itemId, matchingMetadata.keys());

        items.remove(matchingItem);
    }
}
