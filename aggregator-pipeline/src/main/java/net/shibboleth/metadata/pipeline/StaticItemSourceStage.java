/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Item;

/**
 * A stage which adds a static collection of Items to a {@link Item} collection.
 * 
 * @param <ItemType> the type of Item produced by this source
 */
@ThreadSafe
public class StaticItemSourceStage<ItemType extends Item<?>> extends BaseStage<ItemType> {

    /** Collection of static Items added to each Item collection by {@link #execute(Collection)}. */
    private Collection<ItemType> source;

    /**
     * Gets the collection of static Items added to the Item collection by this stage.
     * 
     * @return collection of static Items added to the Item collection by this stage
     */
    public Collection<ItemType> getSourceMetadata() {
        return source;
    }

    /**
     * Sets the collection of Items added to the Item collection by this stage.
     * 
     * @param items collection of Items added to the Item collection by this stage
     */
    public synchronized void setSourceMetadata(final Collection<ItemType> items) {
        if (isInitialized()) {
            return;
        }

        if (items == null || items.isEmpty()) {
            source = Collections.emptyList();
            return;
        }

        source = new ArrayList<ItemType>();
        for (ItemType item : items) {
            if (item != null) {
                source.add(item);
            }
        }
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<ItemType> itemCollection) throws StageProcessingException {
        for (ItemType items : getSourceMetadata()) {
            if (items != null) {
                itemCollection.add((ItemType) items.copy());
            }
        }
    }
}