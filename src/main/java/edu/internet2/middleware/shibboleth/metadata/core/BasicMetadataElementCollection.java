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

package edu.internet2.middleware.shibboleth.metadata.core;

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
public class BasicMetadataElementCollection<ElementType extends MetadataElement<?>> implements
        MetadataElementCollection<ElementType> {

    /** Serial version UID. */
    private static final long serialVersionUID = 6445721225558015497L;
    
    /** Backing delegate collection. */
    private Collection<ElementType> delegate;

    /** Constructor. */
    public BasicMetadataElementCollection() {
        delegate = new ArrayList<ElementType>();
    }

    /** {@inheritDoc} */
    public boolean add(ElementType o) {
        return delegate.add(o);
    }

    /** {@inheritDoc} */
    public boolean addAll(Collection<? extends ElementType> c) {
        return delegate.addAll(c);
    }

    /** {@inheritDoc} */
    public void clear() {
        delegate.clear();
    }

    /** {@inheritDoc} */
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    /** {@inheritDoc} */
    public boolean containsAll(Collection<?> c) {
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
    public boolean remove(Object o) {
        return delegate.remove(o);
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
    public int size() {
        return delegate.size();
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
    @SuppressWarnings("unchecked")
    public MetadataElementCollection<ElementType> copy() {
        BasicMetadataElementCollection<ElementType> copy = new BasicMetadataElementCollection<ElementType>();
        for (ElementType element : delegate) {
            copy.add((ElementType) element.copy());
        }

        return copy;
    }
}