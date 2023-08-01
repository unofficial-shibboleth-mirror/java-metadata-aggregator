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

import javax.annotation.Nonnull;

import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.collection.ClassToInstanceMultiMap;

// Checkstyle: LineLength OFF
/**
 * A wrapper around a piece of information processed by pipeline stages.
 * 
 * <p>
 * Implementations of {@code Item} are <strong>not</strong> expected to be thread-safe.
 * </p>
 * 
 * <p>
 * In multi-threaded applications, use of {@code Item} objects should be
 * confined to a single thread at a time. When responsibility for an {@code Item}
 * passes from one thread to another, actions in the sending and receiving threads
 * must be synchronized such that the last use of the {@code Item} in the sending
 * thread <em>happens-before</em> the first use of the {@code Item} in the receiving
 * thread. The same must be true in reverse if responsibility for the {@code Item}
 * is later transferred back to the original thread.
 * </p>
 * 
 * <p>
 * Many constructs in the Java API, such as {@link java.util.concurrent.ExecutorService},
 * provide such <em>happens-before</em> guarantees. Similarly, classes within the MDA
 * framework such as {@link net.shibboleth.metadata.pipeline.PipelineDemultiplexerStage}
 * provide the same guarantees and can be used without concern for synchronization.
 * </p>
 * 
 * <p>
 * If you pass {@code Item}s between threads in some other way, however, you <strong>must</strong>
 * make sure that no data races can occur.
 * </p>
 * 
 * <p>
 * Note that if you call {@link Item#copy} to duplicate an item, the copy is entirely independent
 * of the original except that they will share any attached immutable {@link ItemMetadata} objects.
 * The two {@code Item} objects can then be used without additional synchronization, except for
 * that involved in publishing the copy to the thread in which it will be used: a <em>happens-before</em>
 * relationship is still required to guarantee that the receiving thread sees a consistent state
 * for the object transferred.
 * </p>
 * 
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/package-summary.html#MemoryVisibility"
 * >Memory Consistency Properties; <code>java.util.concurrent</code> package documentation</a>
 * 
 * @param <T> type of item data
 */
// Checkstyle: LineLength ON
public interface Item<T> {

    /**
     * Gets the wrapped item data.
     * 
     * @return the wrapped item data
     */
    @Nonnull T unwrap();

    /**
     * Gets all of the metadata attached to this Item.
     * 
     * @return metadata attached to this Item
     */
    @Nonnull @NonnullElements ClassToInstanceMultiMap<ItemMetadata> getItemMetadata();

    /**
     * Performs a copy of this Item. All member fields, except {@link ItemMetadata}, should be deep cloned.
     * {@link ItemMetadata} objects must be shared between the clone and the original.
     * 
     * @return the clone of this {@code Item}
     */
    @Nonnull Item<T> copy();
}
