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

package net.shibboleth.metadata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import net.shibboleth.shared.annotation.constraint.NonnullElements;

/**
 * A simple {@link ItemCollectionSerializer} which just serializes each {@link Item}
 * in turn.
 * 
 * @param <T> type of data contained in each item
 */
@Immutable
public class SimpleItemCollectionSerializer<T> implements ItemCollectionSerializer<T> {

    /** The {@link ItemSerializer} to use on each {@link Item} in turn. */
    private final ItemSerializer<T> serializer;

    /**
     * Constructor.
     *
     * @param ser {@link ItemSerializer} to use on each {@link Item} in turn
     */
    public SimpleItemCollectionSerializer(@Nonnull final ItemSerializer<T> ser) {
        serializer = ser;
    }

    @Override
    public void serializeCollection(@Nonnull @NonnullElements final Collection<Item<T>> items,
            @Nonnull final OutputStream output)
        throws IOException {
        for (final Item<T> item : items) {
            assert item != null;
            serializer.serialize(item, output);
        }
    }

}
