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
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Abstract base class allowing a selected subset of XML attributes in a DOM document
 * to be validated as a given type.
 *
 * @param <T> type to convert each attribute to for validation
 *
 * @since 0.10.0
 */
public abstract class AbstractAttributeValidationStage<T> extends AbstractElementValidationStage<T> {

    /**
     * Collection of attribute names for those attributes we will be visiting.
     * 
     * <p>Internally, this is always held as a {@link Set} of {@link QName}s,
     * and that's how it will be returned by the getter. There are four setters,
     * however, for both collections and singletons, and both simple attribute
     * names and qualified names (@link QName}s).</p>
     * 
     * <p>Because of type erasure, those setters can't all be overloads of the
     * same method name, so the {@link String}-based unqualified name forms
     * (which will by far dominate use cases) are given the preferred
     * identifiers.</p>
     */
    @SuppressWarnings("null")
    @NonnullElements @Unmodifiable @GuardedBy("this")
    private @Nonnull Set<QName> attributeNames = Set.of();

    /**
     * Gets the collection of attribute names to visit.
     * 
     * @return collection of attribute names to visit
     */
    public final synchronized @Nonnull Collection<QName> getAttributeNames() {
        return attributeNames;
    }

    /**
     * Sets the collection of attribute names to visit, as a collection of unqualified
     * {@link String} values.
     *
     * @param names collection of attribute names to visit
     */
    public final synchronized void setAttributeNames(@Nonnull final Collection<String> names) {
        checkSetterPreconditions();
        Constraint.isNotNull(names, "attributeNames may not be null");
        final var qnames = new HashSet<QName>();
        for (final var name : names) {
            qnames.add(new QName(name));
        }
        attributeNames = Set.copyOf(qnames);
    }
    
    /**
     * Sets a single attribute name to be visited.
     * 
     * <p>Shorthand for use when a single unqualified attribute name, expressed as a
     * {@link String}, is used.</p>
     * 
     * @param name name for the attribute to be visited
     */
    public final synchronized void setAttributeName(@Nonnull final String name) {
        checkSetterPreconditions();
        Constraint.isNotNull(name, "unqualifiedAttributeName may not be null");
        attributeNames = Set.of(new QName(name));
    }
    
    /**
     * Sets the collection of attribute names to visit.
     * 
     * @param names collection of qualified attribute names to visit
     */
    public final synchronized void setQualifiedAttributeNames(@Nonnull final Collection<QName> names) {
        checkSetterPreconditions();
        Constraint.isNotNull(names, "attributeNames may not be null");
        attributeNames = Set.copyOf(names);
    }
    
    /**
     * Sets a single attribute name to be visited.
     * 
     * @param name {@link QName} for the attribute to be visited
     */
    public final synchronized void setQualifiedAttributeName(@Nonnull final QName name) {
        checkSetterPreconditions();
        Constraint.isNotNull(name, "attributeName may not be null");
        attributeNames = Set.of(name);
    }
    
    @Override
    protected @Nonnull T convert(final @Nonnull Element element) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void visit(@Nonnull final Element element, @Nonnull final DOMTraversalContext context)
            throws StageProcessingException {
        
        // Look at the attributes. If there are none, we're done.
        final var attributes = element.getAttributes();
        if (attributes == null) return;

        // Iterate through the attributes on this element.
        for (int i = 0; i < attributes.getLength(); i++) {
            final var attr = (Attr)attributes.item(i);
            if (attr != null && applicable(attr, context)) {
                applyValidators(convert(attr), context);
            }
        }
    }
    
    /**
     * Convert the visited {@link Attr} to the type to be validated.
     *
     * @param attr {@link Attr} being validated
     * @return converted value
     */
    protected abstract @Nonnull T convert(final @Nonnull Attr attr);

    /**
     * Returns whether the given attribute is applicable to our traversal.
     *
     * @param attr {@link Attr} candidate for visiting
     * @param context {@link DOMTraversalContext} implementation being used to manage the traversal
     * @return returns whether the given attribute is applicable to our traversal
     */
    protected boolean applicable(@Nonnull final Attr attr, @Nonnull final DOMTraversalContext context) {
        final var attrName = new QName(attr.getNamespaceURI(), attr.getLocalName());
        return attributeNames.contains(attrName);
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (attributeNames.isEmpty()) {
            throw new ComponentInitializationException("attributeNames may not be empty");
        }
    }
}
