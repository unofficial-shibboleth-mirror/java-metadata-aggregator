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
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

/**
 * Base class for {@link Stage} implementations that iterate over each {@link Item} in a collection and do something.
 * 
 * @param <T> type of metadata this stage operates upon
 *
 * @since 0.10.0
 */
@ThreadSafe
public abstract class AbstractIteratingStage<T> extends AbstractStage<T> {

    /**
     * Iterates over each element of the Item collection and delegates the processing of that element to
     * {@link #doExecute(Item)}.
     *
     * {@inheritDoc}
     */
    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        for (final Item<T> item : items) {
            doExecute(item);
        }
    }

    /**
     * Processes a given {@link Item}.
     * 
     * @param item {@link Item} on which to operate
     * 
     * @throws StageProcessingException thrown if there is a problem with the stage processing
     */
    protected abstract void doExecute(@Nonnull final Item<T> item) throws StageProcessingException;
}
