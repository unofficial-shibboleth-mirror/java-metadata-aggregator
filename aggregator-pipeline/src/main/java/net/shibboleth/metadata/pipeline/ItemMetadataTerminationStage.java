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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Stage} that terminates pipeline processing if an {@link Item} has a specific type of {@link ItemMetadata}
 * attached to it.
 * 
 * @param <T> type of items the stage operates on
 */
@ThreadSafe
public class ItemMetadataTerminationStage<T> extends AbstractItemMetadataSelectionStage<T> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ItemMetadataTerminationStage.class);

    @Override
    protected void doExecute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection,
            final Item<T> matchingItem,
            final Map<Class<? extends ItemMetadata>, List<? extends ItemMetadata>> matchingMetadata)
            throws TerminationException {

        final String itemId = getItemIdentificationStrategy().getItemIdentifier(matchingItem);
        log.error("Item {} caused processing to terminate because it was marked with a {}", itemId,
                matchingMetadata.keySet());

        throw new TerminationException("Item " + itemId + " marked with metadata of type "
                + matchingMetadata.keySet());
    }
}
