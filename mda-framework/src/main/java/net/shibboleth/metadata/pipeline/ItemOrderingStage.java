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
import net.shibboleth.metadata.pipeline.impl.NoOpItemOrderingStrategy;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A {@link net.shibboleth.metadata.pipeline.Stage} capable of ordering a collection of {@link Item}s
 * according to a supplied strategy.
 * 
 * @param <T> type of the items to be ordered
 *
 * @since 0.10.0
 */
@ThreadSafe
public class ItemOrderingStage<T> extends AbstractStage<T> {

    /** Strategy used to order a collection of Items. The default strategy performs no ordering. */
    @Nonnull @GuardedBy("this")
    private ItemOrderingStrategy<T> orderingStrategy = new NoOpItemOrderingStrategy<>();

    /**
     * Gets the strategy used to order a collection of Items.
     * 
     * @return strategy used to order a collection of Items
     */
    @Nonnull public final synchronized ItemOrderingStrategy<T> getItemOrderingStrategy() {
        return orderingStrategy;
    }

    /**
     * Sets the strategy used to order a collection of Items.
     * 
     * @param strategy strategy used to order a collection of Items
     */
    public synchronized void setItemOrderingStrategy(@Nonnull final ItemOrderingStrategy<T> strategy) {
        throwSetterPreconditionExceptions();
        orderingStrategy = Constraint.isNotNull(strategy, "Item ordering strategy can not be null");
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        final var orderedItems = getItemOrderingStrategy().order(items);
        items.clear();
        items.addAll(orderedItems);
    }

}
