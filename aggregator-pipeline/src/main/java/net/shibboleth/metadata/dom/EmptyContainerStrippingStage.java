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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A stage which removes all empty instances of the named container element from DOM metadata.
 */
@ThreadSafe
public class EmptyContainerStrippingStage extends AbstractStage<Element> {

    /** Namespace of the element to strip. */
    private String elementNamespace;

    /** Name of the element to strip. */
    private String elementName;

    /**
     * Get the namespace of the element to strip.
     * 
     * @return namespace of the element to strip
     */
    @Nullable public String getElementNamespace() {
        return elementNamespace;
    }

    /**
     * Set the namespace of the element to strip.
     * 
     * @param namespace namespace of the element to strip
     */
    public void setElementNamespace(@Nonnull @NotEmpty final String namespace) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        elementNamespace = Constraint.isNotNull(StringSupport.trimOrNull(namespace),
                "target namespace can not be null or empty");
    }

    /**
     * Get the name of the element to strip.
     * 
     * @return the name of the element to strip
     */
    @Nullable public String getElementName() {
        return elementName;
    }

    /**
     * Set the name of the element to strip.
     * 
     * @param name the name of the element to strip
     */
    public void setElementName(@Nonnull @NotEmpty final String name) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        elementName = Constraint.isNotNull(StringSupport.trimOrNull(name),
                "target element name can not be null or empty");
    }

    /**
     * Determines whether a given DOM element has any element children.
     * 
     * @param element Element to check for child elements.
     * @return true if and only if the Element has child elements.
     */
    private boolean hasChildElements(@Nonnull final Element element) {
        final Node firstChild = ElementSupport.getFirstChildElement(Constraint.isNotNull(element, "Element can not be null"));
        return firstChild != null;
    }
    
    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull @NonnullElements final Collection<Item<Element>> items)
            throws StageProcessingException {
        for (final Item<Element> item : items) {
            final Element element = item.unwrap();
            
            // List all the relevant elements in this document in document order
            final NodeList extensionList = element.getElementsByTagNameNS(elementNamespace, elementName);
            
            // Process in reverse order so that, for example, Extensions inside Extensions are
            // handled correctly.
            for (int eIndex = extensionList.getLength()-1; eIndex >= 0; eIndex--) {
                final Element extensions = (Element) extensionList.item(eIndex);
                if (!hasChildElements(extensions)) {
                    extensions.getParentNode().removeChild(extensions);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        elementNamespace = null;
        elementName = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (elementNamespace == null) {
            throw new ComponentInitializationException("target namespace can not be null or empty");
        }
        if (elementName == null) {
            throw new ComponentInitializationException("target element name can not be null or empty");
        }
    }

}
