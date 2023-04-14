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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.shared.annotation.constraint.NonnullAfterInit;
import net.shibboleth.shared.annotation.constraint.NotEmpty;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.StringSupport;
import net.shibboleth.shared.xml.ElementSupport;

/**
 * A stage which removes all empty instances of the named container element from DOM metadata.
 */
@ThreadSafe
public class EmptyContainerStrippingStage extends AbstractIteratingStage<Element> {

    /** Namespace of the element to strip. */
    @NonnullAfterInit @NotEmpty @GuardedBy("this")
    private String elementNamespace;

    /** Name of the element to strip. */
    @NonnullAfterInit @NotEmpty @GuardedBy("this")
    private String elementName;

    /**
     * Get the namespace of the element to strip.
     * 
     * @return namespace of the element to strip
     */
    @Nullable public final synchronized String getElementNamespace() {
        return elementNamespace;
    }

    /**
     * Set the namespace of the element to strip.
     * 
     * @param namespace namespace of the element to strip
     */
    public synchronized void setElementNamespace(@Nonnull @NotEmpty final String namespace) {
        checkSetterPreconditions();
        elementNamespace = Constraint.isNotNull(StringSupport.trimOrNull(namespace),
                "target namespace can not be null or empty");
    }

    /**
     * Get the name of the element to strip.
     * 
     * @return the name of the element to strip
     */
    @Nullable public final synchronized String getElementName() {
        return elementName;
    }

    /**
     * Set the name of the element to strip.
     * 
     * @param name the name of the element to strip
     */
    public synchronized void setElementName(@Nonnull @NotEmpty final String name) {
        checkSetterPreconditions();
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
        final Node firstChild = ElementSupport.getFirstChildElement(element);
        return firstChild != null;
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) {
        final Element element = item.unwrap();

        // List all the relevant elements in this document in document order
        final NodeList extensionList = element.getElementsByTagNameNS(getElementNamespace(), getElementName());

        /*
         * Because NodeList is a live collection, we don't want to modify the document while
         * we are traversing it, as the result is that the list is recomputed every time
         * a change is made to the document. Instead, convert to a non-live list and
         * traverse that. See MDA-278 for details.
         *
         * Process in reverse order so that, for example, Extensions inside Extensions are
         * handled correctly.
         */
        final var containers = new ArrayList<Element>(extensionList.getLength());
        for (int eIndex = extensionList.getLength()-1; eIndex >= 0; eIndex--) {
            containers.add((Element)extensionList.item(eIndex));
        }

        // Now, actually remove any empty container elements.
        for (final var container : containers) {
            assert container != null;
            if (!hasChildElements(container)) {
                container.getParentNode().removeChild(container);
            }
        }
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (elementNamespace == null) {
            throw new ComponentInitializationException("target namespace can not be null or empty");
        }
        if (elementName == null) {
            throw new ComponentInitializationException("target element name can not be null or empty");
        }
    }

}
