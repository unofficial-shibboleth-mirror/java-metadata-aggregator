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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;

/**
 * A stage which adds a static collection of Items to a {@link Item} collection.
 * 
 * @param <T> the type of item produced by this source
 */
@ThreadSafe
public class StaticItemSourceStage<T> extends AbstractStage<T> {

    /** Collection of static Items added to each Item collection by {@link #execute(List)}. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Item<T>> source = List.of();

    /**
     * Gets the collection of static Items added to the Item collection by this stage.
     * 
     * @return collection of static Items added to the Item collection by this stage
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized List<Item<T>> getSourceItems() {
        return source;
    }

    /**
     * Sets the collection of Items added to the Item collection by this stage.
     * 
     * @param items collection of Items added to the Item collection by this stage
     */
    public synchronized void setSourceItems(
            @Nonnull @NonnullElements @Unmodifiable final List<Item<T>> items) {
        throwSetterPreconditionExceptions();
        source = List.copyOf(items);
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        for (final Item<T> item : getSourceItems()) {
            items.add(item.copy());
        }
    }

    @Override
    protected void doDestroy() {
        source = null;

        super.doDestroy();
    }
}
