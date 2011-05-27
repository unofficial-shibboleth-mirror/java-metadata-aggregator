/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata.util;

import java.util.Collection;
import java.util.Iterator;

import org.opensaml.util.Assert;
import org.opensaml.util.collections.LazyList;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;

/**
 * A wrapper around a delegate collection that allows the collection to carry item metadata.
 * 
 * @param <ItemType> type of items stored in this collection
 */
public class ItemCollectionWithMetadata<ItemType extends Item> implements Collection<ItemType> {

    /** The delegate collection. */
    private final Collection<ItemType> delegate;

    /** Additional processing information associated with this collection of Items. */
    private final ClassToInstanceMultiMap<ItemMetadata> metadata;

    /** Constructor. */
    public ItemCollectionWithMetadata() {
        delegate = new LazyList<ItemType>();
        metadata = new ClassToInstanceMultiMap<ItemMetadata>(true);
    }

    /**
     * Constructor.
     * 
     * @param wrappedCollection the underlying collection that holds the items
     */
    public ItemCollectionWithMetadata(Collection<ItemType> wrappedCollection) {
        Assert.isNotNull(wrappedCollection, "Wrapped collection can not be null");
        delegate = wrappedCollection;
        metadata = new ClassToInstanceMultiMap<ItemMetadata>(true);
    }

    /**
     * Gets the underlying collection used to store Items.
     * 
     * @param <T> type of the underlying collection
     * 
     * @return the underlying collection, never null
     */
    public <T extends Collection<ItemType>> T unwrap() {
        return (T) delegate;
    }

    /**
     * Gets the {@link ItemMetadata} for this collection.
     * 
     * @return the {@link ItemMetadata} for this collection, never null
     */
    public ClassToInstanceMultiMap<ItemMetadata> getCollectionMetadata() {
        return metadata;
    }

    /** {@inheritDoc} */
    public int size() {
        return delegate.size();
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /** {@inheritDoc} */
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    /** {@inheritDoc} */
    public Iterator<ItemType> iterator() {
        return delegate.iterator();
    }

    /** {@inheritDoc} */
    public Object[] toArray() {
        return delegate.toArray();
    }

    /** {@inheritDoc} */
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    /** {@inheritDoc} */
    public boolean add(ItemType e) {
        return delegate.add(e);
    }

    /** {@inheritDoc} */
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    /** {@inheritDoc} */
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    /** {@inheritDoc} */
    public boolean addAll(Collection<? extends ItemType> c) {
        return delegate.addAll(c);
    }

    /** {@inheritDoc} */
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    /** {@inheritDoc} */
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    /** {@inheritDoc} */
    public void clear() {
        delegate.clear();
    }
}