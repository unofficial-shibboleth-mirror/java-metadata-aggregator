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

package net.shibboleth.metadata.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;

/**
 * An append-only multimap where each entry associates a raw type (i.e. class) to instances of that class. In addition
 * the map may also associate any supertype (i.e. all superclasses and interfaces implemented by the class) with the
 * class.
 * 
 * Null values are not supported.
 * 
 * @param <B> a bound for the types of values in the map
 */
@NotThreadSafe
public final class ClassToInstanceMultiMap<B> {

    /** Whether supertypes should also be indexed. */
    private final boolean indexSupertypes;

    /** Map which backs this map. */
    private final HashMap<Class<?>, List<B>> backingMap;

    /** List of values that are indexed. */
    private final List<B> values;

    /** Constructor. Does not index supertypes. */
    public ClassToInstanceMultiMap() {
        this(false);
    }

    /**
     * Constructor.
     * 
     * @param indexSupertypes indicates whether supertypes of a value should be indexed
     */
    public ClassToInstanceMultiMap(final boolean indexSupertypes) {
        backingMap = new HashMap<Class<?>, List<B>>();
        values = new ArrayList<B>();
        this.indexSupertypes = indexSupertypes;
    }

    /** Removes all mappings from this map. */
    public void clear() {
        values.clear();
        backingMap.clear();
    }

    /**
     * Returns true if the map contains a mapping for the given key.
     * 
     * @param key key to check for in the map
     * 
     * @return true if the map contains a mapping for the specified key
     */
    public boolean containsKey(final Class<?> key) {
        if (key == null) {
            return false;
        }

        return backingMap.containsKey(key);
    }

    /**
     * Returns true if the map contains a mapping to the given value.
     * 
     * @param value value to check for in this map
     * 
     * @return true if the map contains a mapping to the specified value
     */
    public boolean containsValue(final B value) {
        if (value == null) {
            return false;
        }

        return values.contains(value);
    }

    /**
     * Gets the instances mapped to the given type or an empty list, immutable, list otherwise.
     * 
     * @param <T> type identifier
     * @param type map key
     * 
     * @return instances mapped to the given type or an empty list, immutable, list otherwise
     */
    public <T> List<T> get(final Class<T> type) {
        if (type == null) {
            return Collections.emptyList();
        }

        final List<T> values = (List<T>) backingMap.get(type);
        if (values == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Returns true if this map contains no entries, false otherwise.
     * 
     * @return true if this map contains no entries, false otherwise
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Gets the set of keys contained in this map. The set is backed by the map so changes made to the map are reflected
     * in the set. However the set does not allow direct modification, any changes need to be done through this map.
     * 
     * @return set of keys contained in this map
     */
    public Set<Class<?>> keys() {
        return Collections.unmodifiableSet(backingMap.keySet());
    }

    /**
     * Adds a value to this map. If {@link #indexSupertypes} is false only the values class type is used as a key to the
     * value. If {@link #indexSupertypes} is true, then the class types, all its supertypes, and all implemented
     * interfaces are used as keys to the value.
     * 
     * Duplicate values, as determined by the values {@link Object#hashCode()} and {@link Object#equals(Object)}
     * methods, are not stored. Only one instance of the value is ever stored in the map.
     * 
     * @param value value to be stored in the map
     */
    public void put(final B value) {
        if (value == null) {
            return;
        }

        if (!values.contains(value)) {
            values.add(value);
        }

        final ArrayList<Class<?>> valueTypes = new ArrayList<Class<?>>();
        valueTypes.add(value.getClass());

        if (indexSupertypes) {
            getSuperTypes(value.getClass(), valueTypes);
        }

        List<B> indexValues;
        for (Class<?> valueType : valueTypes) {
            indexValues = backingMap.get(valueType);

            if (indexValues == null) {
                indexValues = new ArrayList<B>();
                backingMap.put(valueType, indexValues);
            }

            indexValues.add(value);
        }
    }

    /**
     * Adds all the values to the map.
     * 
     * @param values values to be added
     * 
     * @see ClassToInstanceMultiMap#put(Object)
     */
    public void putAll(final Iterable<? extends B> values) {
        if (values == null) {
            return;
        }

        for (B value : values) {
            put(value);
        }
    }

    /**
     * Adds all the values to the map. This operations operates only on the given map's value collection. Therefore,
     * regardless of the given map's policy on indexing by value supertypes, this map will index values based on its
     * policy.
     * 
     * @param values values to be added
     * 
     * @see ClassToInstanceMultiMap#put(Object)
     */
    public void putAll(final ClassToInstanceMultiMap<? extends B> values) {
        if (values == null) {
            return;
        }

        for (B value : values.values()) {
            put(value);
        }
    }

    /**
     * The collection of values currently present in the map. This collection is backed by the map so changeds to the
     * map will be reflected in the collection. However the collection does not allow direct modification so any changes
     * must be done through this map.
     * 
     * @return collection of values currently present in the map
     */
    public Collection<? extends B> values() {
        return Collections.unmodifiableList(values);
    }

    /**
     * Gets all of the superclasses and interfaces implemented by the given class.
     * 
     * @param clazz class for which supertypes will be determined
     * @param accumulator collection to which supertypes are added as they are determined
     */
    private void getSuperTypes(final Class<?> clazz, final ArrayList<Class<?>> accumulator) {
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            accumulator.add(superclass);
            getSuperTypes(superclass, accumulator);
        }

        final Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            for (Class<?> iface : interfaces) {
                accumulator.add(iface);
                getSuperTypes(iface, accumulator);
            }
        }
    }

    /** {@inheritDoc} */
    public int hashCode() {
        // TODO
        return super.hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        // TODO
        return super.equals(obj);
    }
}