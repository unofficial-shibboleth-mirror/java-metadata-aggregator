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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Abstract parent class for stages which visit {@link Element}s named by a
 * collection of {@link QName}s.
 */
public abstract class AbstractElementVisitingStage extends AbstractDOMTraversalStage {

    /** Collection of element names for those elements we will be visiting. */
    @Nonnull private Set<QName> elementNames = Collections.emptySet();

    @Override
    protected void doDestroy() {
        elementNames = null;

        super.doDestroy();
    }

    /**
     * Gets the collection of element names to visit.
     * 
     * @return collection of element names to visit.
     */
    @Nonnull public Collection<QName> getElementNames() {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        return elementNames;
    }

    /**
     * Sets the collection of element names to visit.
     * 
     * @param names collection of element names to visit.
     */
    public void setElementNames(@Nonnull final Collection<QName> names) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(names, "elementNames may not be null");
        elementNames = new HashSet<>(names);
    }
    
    /**
     * Sets a single element name to be visited.
     * 
     * Shorthand for {@link #setElementNames} with a singleton set.
     * 
     * @param name {@link QName} for the element to be visited.
     */
    public void setElementName(@Nonnull final QName name) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(name, "elementName may not be null");
        elementNames = Collections.singleton(name);
    }
    
    @Override
    protected boolean applicable(@Nonnull final Element e) {
        final QName q = new QName(e.getNamespaceURI(), e.getLocalName());
        return elementNames.contains(q);
    }

}
