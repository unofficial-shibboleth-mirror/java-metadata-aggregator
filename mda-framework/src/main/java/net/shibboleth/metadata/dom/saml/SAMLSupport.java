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

package net.shibboleth.metadata.dom.saml;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.Container;
import net.shibboleth.metadata.dom.ElementMaker;
import net.shibboleth.metadata.dom.SimpleElementMaker;
import net.shibboleth.metadata.dom.ElementMatcher;
import net.shibboleth.metadata.dom.SimpleElementMatcher;

/**
 * Helper class for dealing with SAML documents.
 *
 * @since 0.9.0
 */
@ThreadSafe
public final class SAMLSupport {

    /** Namespace URI for SAML elements. */
    public static final @Nonnull String SAML_NS = "urn:oasis:names:tc:SAML:2.0:assertion";
    
    /** Conventional prefix for SAML elements. */
    public static final @Nonnull String SAML_PREFIX = "saml";
    
    /** saml:Attribute element. */
    public static final @Nonnull QName ATTRIBUTE_NAME = new QName(SAML_NS, "Attribute", SAML_PREFIX);
    
    /** Unspecified default <code>NameFormat</code> value for <code>Attribute</code> elements. */
    public static final @Nonnull String ATTRNAME_FORMAT_UNSPECIFIED =
            "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified";
    
    /** saml:AttributeValue element. */
    public static final @Nonnull QName ATTRIBUTE_VALUE_NAME = new QName(SAML_NS, "AttributeValue", SAML_PREFIX);

    /**
     * Matcher for the <code>Extensions</code> element, for use with the {@link Container} system.
     *
     * @since 0.10.0
     */
    public static final @Nonnull ElementMatcher EXTENSIONS_MATCHER =
            new SimpleElementMatcher(SAMLMetadataSupport.EXTENSIONS_NAME);

    /**
     * Maker for the <code>Extensions</code> element, for use with the {@link Container} system.
     *
     * @since 0.10.0
     */
    public static final @Nonnull ElementMaker EXTENSIONS_MAKER =
            new SimpleElementMaker(SAMLMetadataSupport.EXTENSIONS_NAME);

    /** Constructor. */
    private SAMLSupport() {
    }

    /**
     * Extract an <code>Attribute</code> element's <code>NameFormat</code>, applying the
     * SAML standard's specified default if the XML attribute is not present.
     *  
     * @param attribute <code>Attribute</code> {@link Element}
     * @return <code>NameFormat</code> value, or the "unspecified" default
     */
    public static @Nonnull String extractAttributeNameFormat(@Nonnull final Element attribute) {
        final Attr attr = attribute.getAttributeNode("NameFormat");
        if (attr == null) {
            return ATTRNAME_FORMAT_UNSPECIFIED;
        }
        final var value = attr.getValue();
        assert value != null;
        return value;
    }

}
