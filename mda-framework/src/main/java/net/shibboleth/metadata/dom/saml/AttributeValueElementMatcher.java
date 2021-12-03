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

import net.shibboleth.metadata.dom.ElementMatcher;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Match {@link java.util.function.Predicate} for SAML <code>AttributeValue</code> elements with specific
 * text values,
 * for use with the {@link net.shibboleth.metadata.dom.Container} system.
 */
@Immutable
public class AttributeValueElementMatcher extends ElementMatcher {

    /** <code>Attribute</code> value to match. */
    @Nonnull private final String matchValue;

    /**
     * Constructor.
     * 
     * @param value <code>Attribute</code> value to match
     */
    public AttributeValueElementMatcher(@Nonnull final String value) {
        super(SAMLSupport.ATTRIBUTE_VALUE_NAME);
        matchValue = Constraint.isNotNull(value, "attribute value must not be null");
    }

    @Override
    public boolean test(@Nonnull final Element element) {
        // check for element name
        if (!super.test(element)) {
            return false;
        }

        // now check attribute value
        return matchValue.equals(element.getTextContent());
    }
}
