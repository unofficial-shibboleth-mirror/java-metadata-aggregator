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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.collection.LazyList;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import com.google.common.base.Predicates;

/**
 * A pipeline stage that adds a collection of {@link ItemMetadata} objects to each {@link Item}'s item metadata.
 * 
 * @param <T> type of {@link Item} this stage operates upon
 */
@ThreadSafe
public class ItemMetadataAddingStage<T> extends BaseIteratingStage<T> {

    /** {@link ItemMetadata} objects to add to each {@link Item}'s item metadata. */
    private Collection<ItemMetadata> additionalItemMetadata = new LazyList<>();

    /**
     * Gets the {@link ItemMetadata} being added to each {@link Item}'s item metadata.
     * 
     * @return the {@link ItemMetadata} being added to each {@link Item}'s item metadata
     */
    @Nonnull
    public Collection<ItemMetadata> getAdditionalItemMetadata() {
        return additionalItemMetadata;
    }

    /**
     * Gets the {@link ItemMetadata} to be added to each {@link Item}'s item metadata.
     * 
     * @param metadata the {@link ItemMetadata} to be added to each {@link Item}'s item metadata
     */
    public void setAdditionalItemMetadata(@Nonnull final Collection<ItemMetadata> metadata) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        Constraint.isNotNull(metadata, "additional metadata collection must not be null");

        additionalItemMetadata = new LazyList<>();
        CollectionSupport.addIf(additionalItemMetadata, metadata, Predicates.notNull());
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doExecute(@Nonnull final Item<T> item) throws StageProcessingException {
        item.getItemMetadata().putAll(additionalItemMetadata);
        return true;
    }
}
