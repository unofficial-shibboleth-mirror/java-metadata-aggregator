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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;

/**
 * A strategy that defines how to order a {@link net.shibboleth.metadata.Item} collection.
 * 
 * <p>
 * All implementations of this interface <strong>must</strong> be thread-safe.
 * </p>
 *
 * @param <T> type of item to be handled
 *
 * @since 0.10.0
 */
@ThreadSafe
public interface ItemOrderingStrategy<T> {

    /**
     * Orders a given Item collection.
     * 
     * @param items collection of {@link Item}s, never null
     * 
     * @return sorted {@link List} of {@link Item}s, never null
     * 
     * @throws StageProcessingException if the items in the collection cannot be ordered, for example
     *      because they do not meet required pre-conditions
     */
    @Nonnull @NonnullElements @Unmodifiable
    List<Item<T>> order(@Nonnull @NonnullElements @Unmodifiable final List<Item<T>> items)
        throws StageProcessingException;

}
