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

import javax.annotation.Nonnull;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;

/**
 * A strategy that defines how to order a {@link net.shibboleth.metadata.Item} collection.
 *
 * @param <T> type of item to be handled
 */
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
    List<Item<T>> order(@Nonnull @NonnullElements @Unmodifiable final Collection<Item<T>> items)
        throws StageProcessingException;

}
