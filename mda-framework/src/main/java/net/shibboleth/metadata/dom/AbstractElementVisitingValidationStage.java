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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Abstract parent class for validation of DOM nodes within a DOM traversal.
 * 
 * <p>
 * Note that the nodes being validated may be either elements or attributes;
 * this class is parameterised by that type and a subclass must implement an
 * appropriate conversion.
 * </p>
 * 
 * <p>
 * This class manages the set of {@link Element} names for the traversal.
 * </p>
 *
 * @param <V> type of the values to be validated
 * @param <N> type of node being visited during the traversal
 */
public abstract class AbstractElementVisitingValidationStage<V, N>
    extends AbstractDOMValidationStage<V, DOMTraversalContext> {

    /** Collection of element names for those elements we will be visiting. */
    @NonnullElements @Unmodifiable @GuardedBy("this")
    private @Nonnull Set<QName> elementNames = Set.of();

    /**
     * Gets the collection of element names to visit.
     * 
     * @return collection of element names to visit.
     */
    @Nonnull public final synchronized Collection<QName> getElementNames() {
        return elementNames;
    }

    /**
     * Sets the collection of element names to visit.
     * 
     * @param names collection of element names to visit.
     */
    public final synchronized void setElementNames(@Nonnull final Collection<QName> names) {
        checkSetterPreconditions();
        Constraint.isNotNull(names, "elementNames may not be null");
        elementNames = Set.copyOf(names);
    }
    
    /**
     * Sets a single element name to be visited.
     * 
     * <p>Shorthand for {@link #setElementNames} with a singleton set.</p>
     * 
     * @param name {@link QName} for the element to be visited.
     */
    public final synchronized void setElementName(@Nonnull final QName name) {
        checkSetterPreconditions();
        Constraint.isNotNull(name, "elementName may not be null");
        elementNames = Set.of(name);
    }

    @Override
    protected @Nonnull DOMTraversalContext buildContext(@Nonnull final Item<Element> item) {
        return new SimpleDOMTraversalContext(item);
    }

    @Override
    protected boolean applicable(@Nonnull final Element e, @Nonnull final DOMTraversalContext context) {
        final QName q = new QName(e.getNamespaceURI(), e.getLocalName());
        return getElementNames().contains(q);
    }

    /**
     * Convert the visited {@link Node} to the type to be validated.
     *
     * @param node being validated
     * @return converted value
     */
    protected abstract @Nonnull V convert(@Nonnull final N node);

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (elementNames.isEmpty()) {
            throw new ComponentInitializationException("elementNames may not be empty");
        }
    }

}
