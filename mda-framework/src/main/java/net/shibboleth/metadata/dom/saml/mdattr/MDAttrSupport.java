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

package net.shibboleth.metadata.dom.saml.mdattr;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.Container;
import net.shibboleth.metadata.dom.ElementMaker;
import net.shibboleth.metadata.dom.ElementMatcher;

/**
 * Helper class for dealing with MDAttr metadata.
 *
 * @since 0.9.0
 */
@ThreadSafe
public final class MDAttrSupport {

    /** MDAttr namespace. */
    public static final @Nonnull String MDATTR_NS = "urn:oasis:names:tc:SAML:metadata:attribute";
    
    /** MDAttr conventional prefix. */
    public static final @Nonnull String MDATTR_PREFIX = "mdattr";

    /** mdattr:EntityAttributes element. */
    public static final @Nonnull QName ENTITY_ATTRIBUTES_NAME = new QName(MDATTR_NS, "EntityAttributes", MDATTR_PREFIX);

    /**
     * Matcher for the <code>EntityAttributes</code> element, for use with the {@link Container} system.
     *
     * @since 0.10.0
     */
    public static final @Nonnull Predicate<Element> ENTITY_ATTRIBUTES_MATCHER =
            new ElementMatcher(ENTITY_ATTRIBUTES_NAME);

    /**
     * Maker for the <code>EntityAttributes</code> element, for use with the {@link Container} system.
     *
     * @since 0.10.0
     */
    public static final @Nonnull Function<Container, Element> ENTITY_ATTRIBUTES_MAKER =
            new ElementMaker(ENTITY_ATTRIBUTES_NAME);

    /** Constructor. */
    private MDAttrSupport() {
    }

}
