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

import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.Container;
import net.shibboleth.metadata.dom.ElementMaker;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A class for constructing SAML <code>AttributeValue</code> elements
 * for use with the {@link Container} system.
 */
@ThreadSafe
public class AttributeValueElementMaker extends ElementMaker {

    /** Value for the attribute. */
    @Nonnull
    private final String attributeValue;

    /**
     * Constructor.
     * 
     * @param value value for the attribute
     */
    public AttributeValueElementMaker(@Nonnull final String value) {
        super(SAMLSupport.ATTRIBUTE_VALUE_NAME);
        attributeValue = Constraint.isNotNull(value, "attribute value must not be null");
    }

    @Override
    public Element apply(@Nonnull final Container container) {
        final Element newElement = super.apply(container);
        newElement.setTextContent(attributeValue);
        return newElement;
    }
}
