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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A stage which removes all instances of the specified element from DOM metadata.
 */
@ThreadSafe
public class ElementStrippingStage extends AbstractIteratingStage<Element> {

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

    @Override
    protected void doExecute(@Nonnull final Item<Element> item)
            throws StageProcessingException {
        final Element docElement = item.unwrap();

        // List all the matching descendant elements in this document in document order
        // Note that this list will never include the document element itself
        final NodeList nodeList = docElement.getElementsByTagNameNS(getElementNamespace(), getElementName());

        // Copy these into a list, because a NodeList can change length at any time
        final int nNodes = nodeList.getLength();
        final List<Element> elements = new ArrayList<>(nNodes);
        for (int eIndex = 0; eIndex < nNodes; eIndex++) {
            elements.add((Element) nodeList.item(eIndex));
        }

        // Remove the elements from the document
        for (final Element element : elements) {
            element.getParentNode().removeChild(element);
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
