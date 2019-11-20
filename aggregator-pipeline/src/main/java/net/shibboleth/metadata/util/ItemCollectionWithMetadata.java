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

package net.shibboleth.metadata.util;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.collection.ClassToInstanceMultiMap;
import net.shibboleth.utilities.java.support.collection.LazyList;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A wrapper around a delegate collection that allows the collection to carry item metadata.
 * 
 * @param <T> type of items stored in this collection
 */
public class ItemCollectionWithMetadata<T> implements Collection<Item<T>> {

    /** The delegate collection. */
    private final Collection<Item<T>> delegate;

    /** Additional processing information associated with this collection of Items. */
    private final ClassToInstanceMultiMap<ItemMetadata> metadata;

    /** Constructor. */
    public ItemCollectionWithMetadata() {
        delegate = new LazyList<>();
        metadata = new ClassToInstanceMultiMap<>(true);
    }

    /**
     * Constructor.
     * 
     * @param wrappedCollection the underlying collection that holds the items
     */
    public ItemCollectionWithMetadata(@Nonnull @NonnullElements final Collection<Item<T>> wrappedCollection) {
        delegate = Constraint.isNotNull(wrappedCollection, "Wrapped collection can not be null");
        delegate.clear();

        metadata = new ClassToInstanceMultiMap<>(true);
    }

    /**
     * Gets the {@link ItemMetadata} for this collection.
     * 
     * @return the {@link ItemMetadata} for this collection, never null
     */
    @Nonnull @NonnullElements public ClassToInstanceMultiMap<ItemMetadata> getCollectionMetadata() {
        return metadata;
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return delegate.size();
    }

    /** {@inheritDoc} */
    @Override public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /** {@inheritDoc} */
    @Override public boolean contains(final Object o) {
        return delegate.contains(o);
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public Iterator<Item<T>> iterator() {
        return delegate.iterator();
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public Object[] toArray() {
        return delegate.toArray();
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements public <TT> TT[] toArray(@Nonnull final TT[] a) {
        Constraint.isNotNull(a, "Target array can not be null");
        return delegate.toArray(a);
    }

    /** {@inheritDoc} */
    @Override public boolean add(@Nullable final Item<T> e) {
        if (e == null) {
            return false;
        }

        return delegate.add(e);
    }

    /** {@inheritDoc} */
    @Override public boolean remove(@Nullable final Object o) {
        if (o == null) {
            return false;
        }

        return delegate.remove(o);
    }

    /** {@inheritDoc} */
    @Override public boolean containsAll(@Nonnull final Collection<?> c) {
        Constraint.isNotNull(c, "Collection can not be null");

        return delegate.containsAll(c);
    }

    /** {@inheritDoc} */
    @Override public boolean addAll(@Nullable final Collection<? extends Item<T>> c) {
        if (c == null) {
            return false;
        }

        return delegate.addAll(c);
    }

    /** {@inheritDoc} */
    @Override public boolean removeAll(@Nullable final Collection<?> c) {
        if (c == null) {
            return false;
        }

        return delegate.removeAll(c);
    }

    /** {@inheritDoc} */
    @Override public boolean retainAll(@Nonnull final Collection<?> c) {
        Constraint.isNotNull(c, "Collection can not be null");

        return delegate.retainAll(c);
    }

    /** {@inheritDoc} */
    @Override public void clear() {
        delegate.clear();
    }
}