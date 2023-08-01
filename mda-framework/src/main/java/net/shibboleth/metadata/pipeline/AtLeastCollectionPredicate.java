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
import java.util.function.Predicate;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Predicate} which returns <code>true</code> if and only if the number of elements
 * in the supplied collection is at least the configured minimum value.
 * 
 * @param <T> type of item element in the collection
 *
 * @since 0.9.0
 */
@ThreadSafe
public class AtLeastCollectionPredicate<T> implements Predicate<Collection<T>> {

    /**
     * Minimum element count which will satisfy the condition. Default value: 0.
     */
    @GuardedBy("this") private int minimum;

    /**
     * Returns the minimum number of elements which will result in a <code>true</code>
     * result.
     * 
     * @return the minimum number of elements which will result in a <code>true</code>
     * result.
     */
    public final synchronized int getMinimum() {
        return minimum;
    }
    
    /**
     * Sets the minimum number of elements which will result in a <code>true</code>
     * result.
     * 
     * @param min minimum number of elements which will result in a <code>true</code>
     * result.
     */
    public synchronized void setMinimum(final int min) {
        minimum = min;
    }
    
    @Override
    public boolean test(final Collection<T> input) {
        return input.size() >= getMinimum();
    }

}
