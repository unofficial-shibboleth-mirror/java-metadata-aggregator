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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A stage which removes all instances of the specified elements from DOM metadata.
 *
 * The elements to be removed are specified by the combination of a namespace
 * (as in {@link ElementStrippingStage}) and a collection of names.
 *
 * The stage ignores all elements not in the specified namespace.
 * 
 * If an element is in the specified namespace, it is by default removed if its
 * local name is in the specified list of names. In other words, the list of names acts
 * by default as a blacklist of element names.
 *
 * The default behaviour can be changed if the <code>whitelisting</code> property
 * is set. In this case, the stage still ignores any elements not in the specified
 * namespace, but elements within that namespace will be removed if their local names
 * do <i>not</i> appear in the collection of names.
 */
@ThreadSafe
public class ElementsStrippingStage extends AbstractDOMTraversalStage<ElementsStrippingStage.Context> {

    /** Context class for this kind of traversal. */
    protected static class Context extends SimpleDOMTraversalContext {

        /**
         * List of {@link Element}s to be removed from the document at the
         * end of the traversal.
         */
        private List<Element> elements = new ArrayList<>();

        /**
         * Constructor.
         *
         * @param contextItem the {@link Item} we are traversing
         */
        public Context(@Nonnull final Item<Element> contextItem) {
            super(contextItem);
        }

        /**
         * Add the given {@link Element} to the list for later removal.
         *
         * @param element the {@link Element} to be removed
         */
        protected void add(@Nonnull final Element element) {
            elements.add(element);
        }

        @Override
        public void end() {
            for (final Element element : elements) {
                element.getParentNode().removeChild(element);
            }
        }
    }

    /** Namespace of the elements to strip. */
    private String elementNamespace;

    /** Names of the elements to strip. */
    private Set<String> elementNames = new HashSet<>();

    /** Whether we are operating in a whitelisting mode (<code>false</code> by default). */
    private boolean whitelisting;

    /**
     * Get the namespace of the elements to strip.
     * 
     * @return namespace of the elements to strip
     */
    @Nullable public String getElementNamespace() {
        return elementNamespace;
    }

    /**
     * Set the namespace of the elements to strip.
     * 
     * @param namespace namespace of the elements to strip
     */
    public void setElementNamespace(@Nonnull @NotEmpty final String namespace) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        elementNamespace = Constraint.isNotNull(StringSupport.trimOrNull(namespace),
                "target namespace can not be null or empty");
    }

    /**
     * Get the names of the elements to strip.
     * 
     * @return the names of the elements to strip
     */
    @Nullable public Collection<String> getElementNames() {
        return elementNames;
    }

    /**
     * Set the names of the elements to strip.
     * 
     * @param names the names of the elements to strip
     */
    public void setElementNames(@Nonnull @NotEmpty final Collection<String> names) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        elementNames = new HashSet<String>(names);
    }

    /**
     * Set whether the {@link #elementNames} are to be used as a whitelist.
     *
     * The default behaviour is for the names to be used as a blacklist.
     *
     * @param whitelist <code>true</code> if the names are to be used as a whitelist
     */
    public void setWhitelisting(final boolean whitelist) {
        whitelisting = whitelist;
    }

    /**
     * Indicates whether the {@link #elementNames} are being used as a whitelist.
     *
     * @return <code>true</code> if the names are being used as a whitelist
     */
    public boolean isWhitelisting() {
        return whitelisting;
    }

    @Override
    protected boolean applicable(@Nonnull final Element element) {
        // ignore all elements not in the given namespace
        if (!elementNamespace.equals(element.getNamespaceURI())) {
            return false;
        }

        // Whitelisting reverses the meaning of presence in the list
        return whitelisting ^ elementNames.contains(element.getLocalName());
    }

    @Override
    protected void visit(@Nonnull final Element element,
            @Nonnull final Context context) {
        context.add(element);
    }

    @Override
    protected void doDestroy() {
        elementNamespace = null;
        elementNames = null;

        super.doDestroy();
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (elementNamespace == null) {
            throw new ComponentInitializationException("target namespace can not be null or empty");
        }
    }

    @Override
    protected Context buildContext(@Nonnull final Item<Element> item) {
        return new Context(item);
    }

}
