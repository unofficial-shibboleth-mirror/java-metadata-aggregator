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

import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import net.jcip.annotations.ThreadSafe;

/**
 * Simple implementation of {@link NamespaceContext} based on a map from prefix values to corresponding URIs. This
 * is not a complete implementation, but does have enough functionality for use within XPath evaluations.
 */
@ThreadSafe
public class SimpleNamespaceContext implements NamespaceContext {

    /** Mapping from prefix values to the corresponding namespace URIs. */
    private final Map<String, String> prefixMappings;

    /**
     * Constructor.
     * 
     * @param mappings Maps prefix values to the corresponding namespace URIs.
     */
    public SimpleNamespaceContext(Map<String, String> mappings) {
        this.prefixMappings = mappings;
    }

    /** {@inheritDoc} */
    public String getNamespaceURI(String prefix) {
        return prefixMappings.get(prefix);
    }

    /** {@inheritDoc} */
    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public Iterator<String> getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

}
