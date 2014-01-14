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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A stage which removes all instances of the specified element from DOM metadata.
 */
@ThreadSafe
public class ElementStrippingStage extends BaseStage<Element> {

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

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull @NonnullElements final Collection<Item<Element>> items)
            throws StageProcessingException {
        for (Item<Element> item : items) {
            final Element docElement = item.unwrap();

            // List all the matching descendant elements in this document in document order
            // Note that this list will never include the document element itself
            NodeList nodeList = docElement.getElementsByTagNameNS(elementNamespace, elementName);

            // Copy these into a list, because a NodeList can change length at any time
            final int nNodes = nodeList.getLength();
            final List<Element> elements = new ArrayList<>(nNodes);
            for (int eIndex = 0; eIndex < nNodes; eIndex++) {
                elements.add((Element) nodeList.item(eIndex));
            }
            
            // Remove the elements from the document
            for (Element element : elements) {
                element.getParentNode().removeChild(element);
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