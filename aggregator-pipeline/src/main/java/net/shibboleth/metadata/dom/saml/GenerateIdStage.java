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
import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.security.Type4UUIDIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;

/** A stage that populates the ID attribute of an EntitiesDescriptor or EntityDescriptor. */
public class GenerateIdStage extends AbstractIteratingStage<Element> {

    /** QName of the ID attribute added to the descriptor. */
    public static final QName ID_ATTRIB = new QName("ID");

    /** Strategy used to generate identifiers. */
    private final IdentifierGenerationStrategy idGenerator;

    /** Constructor. Initialized the {@link #idGenerator} to a {@link Type4UUIDIdentifierGenerationStrategy}. */
    public GenerateIdStage() {
        idGenerator = new Type4UUIDIdentifierGenerationStrategy();
    }

    /**
     * Constructor.
     * 
     * @param generator ID generation strategy used
     */
    public GenerateIdStage(@Nonnull final IdentifierGenerationStrategy generator) {
        idGenerator = Constraint.isNotNull(generator, "ID generation strategy can not be null");
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element element = item.unwrap();
        if (!SAMLMetadataSupport.isEntityOrEntitiesDescriptor(element)) {
            return;
        }

        Attr idAttribute = AttributeSupport.getAttribute(element, ID_ATTRIB);
        if (idAttribute == null) {
            idAttribute = AttributeSupport.constructAttribute(element.getOwnerDocument(), ID_ATTRIB);
            element.setAttributeNode(idAttribute);
        }

        idAttribute.setValue(idGenerator.generateIdentifier());
    }
}
