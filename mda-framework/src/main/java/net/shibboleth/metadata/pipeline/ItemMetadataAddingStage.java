/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.logic.Constraint;

/**
 * A pipeline stage that adds a collection of {@link ItemMetadata} objects to each {@link Item}'s item metadata.
 * 
 * @param <T> type of {@link Item} this stage operates upon
 *
 * @since 0.9.0
 */
@ThreadSafe
public class ItemMetadataAddingStage<T> extends AbstractIteratingStage<T> {

    /** {@link ItemMetadata} objects to add to each {@link Item}'s item metadata. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<ItemMetadata> additionalItemMetadata = CollectionSupport.emptyList();

    /**
     * Gets the {@link ItemMetadata} being added to each {@link Item}'s item metadata.
     * 
     * @return the {@link ItemMetadata} being added to each {@link Item}'s item metadata
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Collection<ItemMetadata> getAdditionalItemMetadata() {
        return additionalItemMetadata;
    }

    /**
     * Gets the {@link ItemMetadata} to be added to each {@link Item}'s item metadata.
     * 
     * @param metadata the {@link ItemMetadata} to be added to each {@link Item}'s item metadata
     */
    public synchronized void setAdditionalItemMetadata(
            @Nonnull @NonnullElements @Unmodifiable final Collection<ItemMetadata> metadata) {
        checkSetterPreconditions();

        Constraint.isNotNull(metadata, "additional metadata collection must not be null");
        additionalItemMetadata = CollectionSupport.copyToList(metadata);
    }

    @Override
    protected void doExecute(@Nonnull final Item<T> item) throws StageProcessingException {
        item.getItemMetadata().putAll(getAdditionalItemMetadata());
    }
}
