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

import com.google.common.base.Predicate;

/**
 * A {@link Predicate} which returns <code>true</code> if and only if the number of elements
 * in the supplied collection is at least the configured minimum value.
 * 
 * @param <T> type of item element in the collection
 */
public class AtLeastCollectionPredicate<T> implements Predicate<Collection<T>> {

    /**
     * Minimum element count which will satisfy the condition. Default value: 0.
     */
    private int minimum;

    /**
     * Returns the minimum number of elements which will result in a <code>true</code>
     * result.
     * 
     * @return the minimum number of elements which will result in a <code>true</code>
     * result.
     */
    public int getMinimum() {
        return minimum;
    }
    
    /**
     * Sets the minimum number of elements which will result in a <code>true</code>
     * result.
     * 
     * @param min minimum number of elements which will result in a <code>true</code>
     * result.
     */
    public void setMinimum(int min) {
        minimum = min;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean apply(final Collection<T> input) {
        return input.size() >= minimum;
    }

}
