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
import javax.annotation.concurrent.Immutable;

import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.SimpleElementMatcher;
import net.shibboleth.shared.logic.Constraint;

/**
 * {@link ElementMatcher} for SAML <code>Attribute</code> elements with specific
 * <code>Name</code> and <code>NameFormat</code> attributes,
 * for use with the {@link net.shibboleth.metadata.dom.Container} system.
 *
 * @since 0.10.0
 */
@Immutable
public class AttributeElementMatcher extends SimpleElementMatcher {

    /** <code>NameFormat</code> attribute value to match. */
    @Nonnull private final String matchFormat;

    /** <code>Name</code> attribute value to match. */
    @Nonnull private final String matchName;

    /**
     * Constructor.
     * 
     * @param name <code>Name</code> attribute value to match
     * @param format <code>NameFormat</code> attribute value to match
     */
    public AttributeElementMatcher(@Nonnull final String name, @Nonnull final String format) {
        super(SAMLSupport.ATTRIBUTE_NAME);
        matchName = Constraint.isNotNull(name, "attribute name must not be null");
        matchFormat = Constraint.isNotNull(format, "attribute name format must not be null");
    }

    @Override
    public boolean match(@Nonnull final Element element) {
        // check for element name
        if (!super.match(element)) {
            return false;
        }

        // now check attributes
        return matchFormat.equals(SAMLSupport.extractAttributeNameFormat(element)) &&
                matchName.equals(element.getAttribute("Name"));
    }
}
