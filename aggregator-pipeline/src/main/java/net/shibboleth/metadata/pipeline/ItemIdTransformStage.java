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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Function;
import com.google.common.base.Predicates;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.collection.LazyList;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

/**
 * A pipeline stage that, taking each {@link ItemId} associated with an {@link Item}, transforms its
 * value using a set of registered transformers, and associates an additional {@link ItemId} (whose value is the
 * result of the transform) with the element.
 * 
 * @param <T> type of {@link Item} this stage operates upon
 */
@ThreadSafe
public class ItemIdTransformStage<T> extends BaseIteratingStage<T> {

    /** Transformers used on IDs. */
    private Collection<Function<String, String>> idTransformers = new LazyList<>();

    /**
     * Gets the transforms used to produce the transformed entity IDs.
     * 
     * @return transforms used to produce the transformed entity IDs, never null
     */
    public Collection<Function<String, String>> getIdTransformers() {
        return idTransformers;
    }

    /**
     * Sets the transforms used to produce the transformed entity IDs.
     * 
     * @param transformers transforms used to produce the transformed entity IDs
     */
    public synchronized void setIdTransformers(final Collection<Function<String, String>> transformers) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        CollectionSupport.addIf(idTransformers, transformers, Predicates.notNull());
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doExecute(@Nonnull final Item<T> item) throws StageProcessingException {
        final List<ItemId> ids = item.getItemMetadata().get(ItemId.class);

        final List<ItemId> transformedIds = new ArrayList<>();
        for (final ItemId id : ids) {
            for (final Function<String, String> idTransform : idTransformers) {
                final String transformedId = idTransform.apply(id.getId());
                transformedIds.add(new ItemId(transformedId));
            }
        }
        item.getItemMetadata().putAll(transformedIds);
        
        return true;
    }
}