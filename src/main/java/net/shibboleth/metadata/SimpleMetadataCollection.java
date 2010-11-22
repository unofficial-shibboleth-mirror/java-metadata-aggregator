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

package net.shibboleth.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.jcip.annotations.NotThreadSafe;

/**
 * A basic implementation of a metadata element collection.
 * 
 * @param <ElementType> type of elements in the collection
 */
@NotThreadSafe
public class SimpleMetadataCollection<ElementType extends Metadata<?>> implements MetadataCollection<ElementType> {

    /** Serial version UID. */
    private static final long serialVersionUID = 6445721225558015497L;

    /** Backing delegate collection. */
    private final Collection<ElementType> delegate;

    /** Constructor. */
    public SimpleMetadataCollection() {
        delegate = new ArrayList<ElementType>();
    }

    /** {@inheritDoc} */
    public boolean add(final ElementType o) {
        if (o == null) {
            return false;
        }

        return delegate.add(o);
    }

    /** {@inheritDoc} */
    public boolean addAll(final Collection<? extends ElementType> c) {
        if (c == null) {
            return false;
        }

        boolean hasChanged = false;
        for (ElementType element : c) {
            if (element != null) {
                delegate.add(element);
                hasChanged = true;
            }
        }

        return hasChanged;
    }

    /** {@inheritDoc} */
    public void clear() {
        delegate.clear();
    }

    /** {@inheritDoc} */
    public boolean contains(final Object o) {
        if (o == null) {
            return false;
        }
        return delegate.contains(o);
    }

    /** {@inheritDoc} */
    public boolean containsAll(final Collection<?> c) {
        return delegate.containsAll(c);
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /** {@inheritDoc} */
    public Iterator<ElementType> iterator() {
        return delegate.iterator();
    }

    /** {@inheritDoc} */
    public boolean remove(final Object o) {
        if (o == null) {
            return false;
        }
        return delegate.remove(o);
    }

    /** {@inheritDoc} */
    public boolean removeAll(final Collection<?> c) {
        if (c == null) {
            return false;
        }

        boolean hasChanged = false;
        for (Object element : c) {
            if (element != null) {
                delegate.remove(element);
                hasChanged = true;
            }
        }

        return hasChanged;
    }

    /** {@inheritDoc} */
    public boolean retainAll(final Collection<?> c) {
        if (c == null) {
            return false;
        }
        return delegate.retainAll(c);
    }

    /** {@inheritDoc} */
    public int size() {
        return delegate.size();
    }

    /** {@inheritDoc} */
    public Object[] toArray() {
        return delegate.toArray();
    }

    /** {@inheritDoc} */
    public <T> T[] toArray(final T[] a) {
        return delegate.toArray(a);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public MetadataCollection<ElementType> copy() {
        SimpleMetadataCollection<ElementType> copy = new SimpleMetadataCollection<ElementType>();
        for (ElementType element : delegate) {
            copy.add((ElementType) element.copy());
        }

        return copy;
    }
}