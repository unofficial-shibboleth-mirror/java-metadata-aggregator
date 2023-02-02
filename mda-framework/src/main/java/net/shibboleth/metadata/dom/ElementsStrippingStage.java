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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.shared.annotation.constraint.NonnullAfterInit;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.NotEmpty;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.StringSupport;

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
 *
 * @since 0.10.0
 */
@ThreadSafe
public class ElementsStrippingStage extends AbstractDOMTraversalStage<ElementsStrippingStage.Context> {

    /**
     * Context class for this kind of traversal.
     * 
     * <p>
     * An instance of this class is passed around during the traversal.
     * To reduce the number of synchronizations, it includes a copy of
     * each of the guarded state variables from the stage class instance.
     * </p>
     */
    @Immutable
    protected static final class Context extends SimpleDOMTraversalContext {

        /**
         * List of {@link Element}s to be removed from the document at the
         * end of the traversal.
         */
        @Nonnull @NonnullElements
        private final List<Element> elements = new ArrayList<>();

        /** Namespace of the elements to strip. */
        @Nonnull @NotEmpty
        private final String elementNamespace;

        /** Names of the elements to strip. */
        @Nonnull @NonnullElements @Unmodifiable
        private final Collection<String> elementNames;

        /** Whether we are operating in a whitelisting mode (<code>false</code> by default). */
        private final boolean whitelisting;

        /**
         * Constructor.
         *
         * @param contextItem the {@link Item} we are traversing
         * @param namespace XML namespace for each of the elements to handle
         * @param names collection of element names within the namespace
         * @param wl whether the named elements should be removed (false) or preserved (true)
         */
        public Context(@Nonnull final Item<Element> contextItem,
                @Nonnull @NotEmpty final String namespace,
                @Nonnull @NonnullElements @Unmodifiable final Collection<String> names,
                final boolean wl) {
            super(contextItem);
            elementNamespace = namespace;
            elementNames = names;
            whitelisting = wl;
        }

        protected final String getElementNamespace() {
            return elementNamespace;
        }

        protected final Collection<String> getElementNames() {
            return elementNames;
        }
        
        protected final boolean isWhitelisting() {
            return whitelisting;
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
    @NonnullAfterInit @NotEmpty @GuardedBy("this")
    private String elementNamespace;

    /** Names of the elements to strip. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Set<String> elementNames = CollectionSupport.emptySet();

    /** Whether we are operating in a whitelisting mode (<code>false</code> by default). */
    private boolean whitelisting;

    /**
     * Get the namespace of the elements to strip.
     * 
     * @return namespace of the elements to strip
     */
    @NonnullAfterInit @NotEmpty public final synchronized String getElementNamespace() {
        return elementNamespace;
    }

    /**
     * Set the namespace of the elements to strip.
     * 
     * @param namespace namespace of the elements to strip
     */
    public void setElementNamespace(@Nonnull @NotEmpty final String namespace) {
        checkSetterPreconditions();
        elementNamespace = Constraint.isNotNull(StringSupport.trimOrNull(namespace),
                "target namespace can not be null or empty");
    }

    /**
     * Get the names of the elements to strip.
     * 
     * @return the names of the elements to strip
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Collection<String> getElementNames() {
        return elementNames;
    }

    /**
     * Set the names of the elements to strip.
     * 
     * @param names the names of the elements to strip
     */
    public synchronized void setElementNames(
            @Nonnull @NonnullElements @Unmodifiable @NotEmpty final Collection<String> names) {
        checkSetterPreconditions();
        elementNames = Set.copyOf(names);
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
    protected boolean applicable(@Nonnull final Element element, @Nonnull final Context context) {
        // ignore all elements not in the given namespace
        if (!context.getElementNamespace().equals(element.getNamespaceURI())) {
            return false;
        }

        // Whitelisting reverses the meaning of presence in the list
        return context.isWhitelisting() ^ context.getElementNames().contains(element.getLocalName());
    }

    @Override
    protected void visit(@Nonnull final Element element,
            @Nonnull final Context context) {
        context.add(element);
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (elementNamespace == null) {
            throw new ComponentInitializationException("target namespace can not be null or empty");
        }
    }

    @Override
    protected synchronized Context buildContext(@Nonnull final Item<Element> item) {
        return new Context(item, getElementNamespace(), getElementNames(), isWhitelisting());
    }

}
