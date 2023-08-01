/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import net.shibboleth.metadata.dom.Container;
import net.shibboleth.metadata.dom.SimpleElementMaker;
import net.shibboleth.shared.logic.Constraint;

/**
 * A class for constructing SAML <code>Attribute</code> elements
 * for use with the {@link Container} system.
 *
 * @since 0.10.0
 */
@Immutable
public class AttributeElementMaker extends SimpleElementMaker {

    /** Value for the <code>Name</code> XML attribute. */
    @Nonnull
    private final String attributeName;

    /** Value for the <code>NameFormat</code> XML attribute. */
    @Nonnull
    private final String attributeNameFormat;

    /**
     * Constructor.
     * 
     * @param name value for the <code>Name</code> XML attribute
     * @param nameFormat value for the <code>NameFormat</code> XML attribute
     */
    public AttributeElementMaker(@Nonnull final String name, @Nonnull final String nameFormat) {
        super(SAMLSupport.ATTRIBUTE_NAME);
        attributeName = Constraint.isNotNull(name, "attribute name must not be null");
        attributeNameFormat = Constraint.isNotNull(nameFormat, "attribute name format must not be null");
    }

    @Override
    public @Nonnull Element make(final @Nonnull Container container) {
        final Element newElement = super.make(container);
        newElement.setAttributeNS(null, "Name", attributeName);
        newElement.setAttributeNS(null, "NameFormat", attributeNameFormat);
        return newElement;
    }
}
