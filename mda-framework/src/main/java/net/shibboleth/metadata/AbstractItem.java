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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.collection.ClassToInstanceMultiMap;
import net.shibboleth.shared.logic.Constraint;

/**
 * Base implementation of an {@link Item}.
 * 
 * @param <T> type of data contained in the item
 */
@NotThreadSafe
public abstract class AbstractItem<T> implements Item<T> {

    /** The actual data held by the item. */
    @Nonnull private final T data;

    /** Additional processing information associated with this {@code Item}. */
    @Nonnull @NonnullElements private final ClassToInstanceMultiMap<ItemMetadata> metadata;

    /**
     * Constructor.
     *
     * @param newData data to wrap in the {@code Item}
     */
    protected AbstractItem(@Nonnull final T newData) {
        Constraint.isNotNull(newData, "data to wrap can not be null");
        metadata = new ClassToInstanceMultiMap<>(true);
        data = newData;
    }

    @Override
    @Nonnull public final T unwrap() {
        return data;
    }

    @Override
    @Nonnull @NonnullElements public final ClassToInstanceMultiMap<ItemMetadata> getItemMetadata() {
        return metadata;
    }
}
