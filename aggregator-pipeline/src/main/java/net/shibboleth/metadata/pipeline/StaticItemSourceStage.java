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
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * A stage which adds a static collection of Items to a {@link Item} collection.
 * 
 * @param <T> the type of item produced by this source
 */
@ThreadSafe
public class StaticItemSourceStage<T> extends BaseStage<T> {

    /** Collection of static Items added to each Item collection by {@link #execute(Collection)}. */
    private Collection<Item<T>> source = Collections.emptyList();

    /**
     * Gets the collection of static Items added to the Item collection by this stage.
     * 
     * @return collection of static Items added to the Item collection by this stage
     */
    @Nonnull @NonnullElements @Unmodifiable public Collection<Item<T>> getSourceItems() {
        return source;
    }

    /**
     * Sets the collection of Items added to the Item collection by this stage.
     * 
     * @param items collection of Items added to the Item collection by this stage
     */
    public synchronized void setSourceItems(@Nullable @NullableElements final Collection<Item<T>> items) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (items == null || items.isEmpty()) {
            source = Collections.emptyList();
        } else {
            source = ImmutableList.copyOf(Iterables.filter(items, Predicates.notNull()));
        }
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection)
            throws StageProcessingException {
        for (final Item<T> item : getSourceItems()) {
            if (item != null) {
                itemCollection.add(item.copy());
            }
        }
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        source = null;

        super.doDestroy();
    }
}