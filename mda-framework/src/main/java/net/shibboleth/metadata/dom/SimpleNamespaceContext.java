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

package net.shibboleth.metadata.dom;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.NamespaceContext;

import net.shibboleth.shared.annotation.constraint.NullableElements;
import net.shibboleth.shared.primitive.StringSupport;
import net.shibboleth.shared.xml.XMLConstants;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;

/**
 * Simple implementation of {@link NamespaceContext} based on a map from prefix values to corresponding URIs. This
 * implementation only supports a single mapping for a given prefix, that is {@link #getPrefixes(String)} will always
 * contain at most 1 element.
 * 
 * @since 0.10.0
 */
@ThreadSafe
public class SimpleNamespaceContext implements NamespaceContext {

    /** Mappings between namespace prefixes and namespace URIs. */
    private final ImmutableBiMap<String, String> mappings;

    /** Constructor. */
    public SimpleNamespaceContext() {
        mappings = getMappingsBuilder().build();
    }

    /**
     * Constructor.
     * 
     * @param prefixToUriMappings Maps prefix values to the corresponding namespace URIs.
     */
    public SimpleNamespaceContext(@Nullable @NullableElements final Map<String, String> prefixToUriMappings) {
        final Builder<String,String> mappingBuilder = getMappingsBuilder();

        if (prefixToUriMappings == null || prefixToUriMappings.isEmpty()) {
            mappings = mappingBuilder.build();
            return;
        }

        String trimmedPrefix;
        String trimmedUri;
        for (final String key : prefixToUriMappings.keySet()) {
            trimmedPrefix = StringSupport.trimOrNull(key);
            if (trimmedPrefix == null) {
                continue;
            }

            trimmedUri = StringSupport.trimOrNull(prefixToUriMappings.get(key));
            if (trimmedUri != null) {
                mappingBuilder.put(trimmedPrefix, trimmedUri);
            }
        }

        mappings = mappingBuilder.build();
    }

    /** {@inheritDoc} */
    @Nullable public String getNamespaceURI(final String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix can not be null");
        }

        final String uri = mappings.get(prefix);
        if (uri == null) {
            return javax.xml.XMLConstants.NULL_NS_URI;
        }
        return uri;
    }

    /** {@inheritDoc} */
    @Nullable public String getPrefix(final String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI can not be null");
        }

        return mappings.inverse().get(namespaceURI);
    }

    /** {@inheritDoc} */
    @Nonnull public Iterator<String> getPrefixes(final String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI can not be null");
        }

        final String prefix = mappings.inverse().get(namespaceURI);
        if (prefix == null) {
            return Collections.<String> emptyList().iterator();
        }
        return Collections.singletonList(prefix).iterator();
    }
    
    /**
     * Build the initial set of mappings which contains entries for XML and XMLNS.
     * 
     * @return initial set of mappings
     */
    @Nonnull private Builder<String, String> getMappingsBuilder(){
        final Builder<String,String> mappingBuilder = new Builder<>();
        
        mappingBuilder.put(XMLConstants.XML_PREFIX, XMLConstants.XML_NS);
        mappingBuilder.put(XMLConstants.XMLNS_PREFIX, XMLConstants.XMLNS_NS);
        
        return mappingBuilder;
    }
}
