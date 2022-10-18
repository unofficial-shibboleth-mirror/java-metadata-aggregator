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
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Abstract base class allowing a selected subset of {@link Element}s in a DOM document
 * to be validated as a given type.
 *
 * @param <T> type to convert each {@link Element} to for validation
 */
@ThreadSafe
public abstract class AbstractElementValidationStage<T> extends AbstractDOMValidationStage<T, DOMTraversalContext> {

    /** Collection of element names for those elements we will be visiting. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Set<QName> elementNames = Set.of();

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
     * Shorthand for {@link #setElementNames} with a singleton set.
     * 
     * @param name {@link QName} for the element to be visited.
     */
    public final synchronized void setElementName(@Nonnull final QName name) {
        checkSetterPreconditions();
        Constraint.isNotNull(name, "elementName may not be null");
        elementNames = Set.of(name);
    }
    
    @Override
    protected boolean applicable(@Nonnull final Element e, @Nonnull final DOMTraversalContext context) {
        final QName q = new QName(e.getNamespaceURI(), e.getLocalName());
        return getElementNames().contains(q);
    }

    /**
     * Convert the visited {@link Element} to the type to be validated.
     *
     * @param element {@link Element} being validated
     * @return converted value
     */
    @Nonnull
    protected abstract T convert(@Nonnull final Element element);

    @Override
    protected void visit(@Nonnull final Element element, @Nonnull final DOMTraversalContext context)
            throws StageProcessingException {
        applyValidators(convert(element), context);
    }
    
    @Override
    @Nonnull
    protected DOMTraversalContext buildContext(@Nonnull final Item<Element> item) {
        return new SimpleDOMTraversalContext(item);
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (elementNames.isEmpty()) {
            throw new ComponentInitializationException("elementNames may not be empty");
        }
    }

}
