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

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.collection.ClassToInstanceMultiMap;
import net.shibboleth.shared.primitive.LoggerFactory;

/**
 * A {@link Stage} that terminates pipeline processing if an {@link Item} has a specific type of {@link ItemMetadata}
 * attached to it.
 * 
 * @param <T> type of items the stage operates on
 */
@ThreadSafe
public class ItemMetadataTerminationStage<T> extends AbstractItemMetadataSelectionStage<T, ItemMetadata> {

    /** Class logger. */
    private static final @Nonnull Logger LOG = LoggerFactory.getLogger(ItemMetadataTerminationStage.class);

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items,
            @Nonnull final Item<T> matchingItem,
            @Nonnull @NonnullElements final ClassToInstanceMultiMap<ItemMetadata> matchingMetadata)
            throws TerminationException {

        final String itemId = getItemIdentificationStrategy().getItemIdentifier(matchingItem);
        LOG.error("Item {} caused processing to terminate because it was marked with a {}", itemId,
                matchingMetadata.keys());

        throw new TerminationException("Item " + itemId + " marked with metadata of type "
                + matchingMetadata.keys());
    }
}
