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

package net.shibboleth.metadata;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.pipeline.PipelineMergeStage;
import net.shibboleth.metadata.pipeline.SplitMergeStage;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

/**
 * Strategy used to merge the a number of source item collections into a target item collection.
 * 
 * <p>
 * All implementations of this interface <strong>must</strong> be thread-safe.
 * </p>
 * 
 * @see PipelineMergeStage
 * @see SplitMergeStage
 */
@ThreadSafe
public interface CollectionMergeStrategy {

    /**
     * Merges the results of each child pipeline in to the collection of Item given to this stage.
     * 
     * @param target collection in to which all the Items should be merged, never null
     * @param sources collections of Items to be merged in to the target, never null not containing any null elements
     * @param <T> type of data contained in the items
     */
    <T> void merge(@Nonnull @NonnullElements List<Item<T>> target,
            @Nonnull @NonnullElements List<List<Item<T>>> sources);
}
