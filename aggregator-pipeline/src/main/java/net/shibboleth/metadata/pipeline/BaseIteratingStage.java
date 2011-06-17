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
import java.util.Iterator;

import net.shibboleth.metadata.Item;

/**
 * Base class for {@link Stage} implementations that iterate over each {@link Item} in a collection and do something.
 * 
 * @param <ItemType> type of Items this stage operates upon
 */
public abstract class BaseIteratingStage<ItemType extends Item<?>> extends BaseStage<ItemType> {

    /**
     * Iterates over each element of the Item collection and delegates the processing of that element to
     * {@link #doExecute(Item)}.
     * 
     * {@inheritDoc}
     */
    protected void doExecute(Collection<ItemType> itemCollection) throws StageProcessingException {
        Iterator<ItemType> itemIterator = itemCollection.iterator();

        ItemType item;
        while (itemIterator.hasNext()) {
            item = itemIterator.next();
            if (!doExecute(item)) {
                itemIterator.remove();
            }
        }
    }

    /**
     * Processes a given Item.
     * 
     * @param item Item on which to operate
     * 
     * @return true if the Item should be retained in the collection, false if not
     * 
     * @throws StageProcessingException thrown if there is a problem with the stage processing
     */
    protected abstract boolean doExecute(ItemType item) throws StageProcessingException;
}