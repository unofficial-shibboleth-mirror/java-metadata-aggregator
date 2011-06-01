/*
 * Licensed to the University Corporation for Advanced Internet Development, Inc.
 * under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache 
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
import java.util.Iterator;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;

/**
 * A {@link Stage} that filters out Items if they have a specific type of {@link ItemMetadata} attached to
 * them.
 * 
 * This is useful, for example, in removing all {@link Item} elements which have an associated
 * {@link net.shibboleth.metadata.ErrorStatus}.
 */
public class StatusMetadataFilterStage extends BaseStage<Item<?>> {

    /** {@link ItemMetadata} classes that, if a {@link Item} contains, will be filtered out. */
    private Collection<Class<ItemMetadata>> filterRequirements = Collections.emptyList();

    /**
     * Gets the {@link ItemMetadata} classes that, if a {@link Item} contains, will be filtered out.
     * 
     * @return {@link ItemMetadata} classes that, if a {@link Item} contains, will be filtered out, never null nor
     *         containing null elements
     */
    public Collection<Class<ItemMetadata>> getFilterRequirements() {
        return filterRequirements;
    }

    /**
     * Sets the {@link ItemMetadata} classes that, if a {@link Item} contains, will be filtered out.
     * 
     * @param requirements {@link ItemMetadata} classes that, if a {@link Item} contains, will be filtered out, may
     *            be null or contain null elements
     */
    public void setFilterRequirements(Collection<Class<ItemMetadata>> requirements) {
        if (isInitialized()) {
            return;
        }
        filterRequirements = Collections.unmodifiableList(CollectionSupport.addNonNull(requirements,
                new LazyList<Class<ItemMetadata>>()));
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<Item<?>> itemCollection) throws StageProcessingException {
        Item<?> item;
        Iterator<Item<?>> itemIterator = itemCollection.iterator();
        while (itemIterator.hasNext()) {
            item = itemIterator.next();
            for (Class infoClass : filterRequirements) {
                if (item.getItemMetadata().containsKey(infoClass)) {
                    itemIterator.remove();
                    break;
                }
            }
        }
    }
}