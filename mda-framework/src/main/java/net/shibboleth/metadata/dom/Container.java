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

import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.xml.ElementSupport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A wrapper for a DOM {@link Element} allowing its use as a container for either
 * a simple text value or for other {@link Element}s. In the latter case, white-space
 * formatting is handled automatically for nested containers.
 *
 * @since 0.10.0
 */
public class Container {

    /**
     * When a container needs to add a new child {@link Element}, use a functor
     * with this interface to create the appropriate context for the
     * new child and to add it in place.
     */
    public interface ChildAddingStrategy {

        /**
         * Add the element in the appropriate position within the provided container.
         * 
         * @param parent container into which to add the new child
         * @param child child {@link Element} to add
         * @return a new child {@link Container} representing the child {@link Element}
         */
        @Nonnull Container addChild(@Nonnull Container parent, @Nonnull Element child);
    }

    /**
     * Child adding strategy which adds the new child as the first child of the
     * container.
     */
    @Nonnull public static final ChildAddingStrategy FIRST_CHILD = new ChildAddingStrategy() {

        @Override
        public @Nonnull Container addChild(@Nonnull final Container parent, @Nonnull final Element child) {
            final Element parentElement = parent.unwrap();
            final String indent = "\n" + parent.indentInner;
            final Node indentNode = parentElement.getOwnerDocument().createTextNode(indent);
            parent.prime();
            parentElement.insertBefore(child, parentElement.getFirstChild());
            parentElement.insertBefore(indentNode, parentElement.getFirstChild());
            return new Container(child, parent);
        }

    };

    /**
     * Child adding strategy which adds the new child as the last child of the
     * container.
     */
    @Nonnull public static final ChildAddingStrategy LAST_CHILD = new ChildAddingStrategy() {

        @Override
        public @Nonnull Container addChild(@Nonnull final Container parent, @Nonnull final Element child) {
            final Element parentElement = parent.unwrap();
            final Document document = parentElement.getOwnerDocument();
            parent.prime();

            // The end of the parent's node list will be the indentation for the closing tag,
            // so push in some additional indentation for the child.
            parentElement.appendChild(document.createTextNode(parent.indentStep));
            parentElement.appendChild(child);

            // Add a newline and indentation for the closing tag again
            parentElement.appendChild(document.createTextNode("\n" + parent.indentOuter));

            return new Container(child, parent);
        }

    };

    /** Default indentation of four spaces. */
    @Nonnull private static final String DEFAULT_INDENT = "    ";

    /** The wrapped {@link Element}. */
    @Nonnull private final Element element;

    /** The parent {@link Container}, or <code>null</code>. */
    @Nullable private final Container parentContainer;

    /** The indentation applied to the opening and closing tags. */
    @Nonnull private final String indentOuter;

    /** The indentation applied to child containers. */
    @Nonnull private final String indentInner;

    /** The difference between outer and inner indentation. */
    @Nonnull private final String indentStep;

    /**
     * Constructor.
     * 
     * @param elem {@link Element} on which to base the {@link Container}
     * @param parent {@link Container} to link as the parent, or <code>null</code>
     * @param outer indentation to be used for this container's start and end tags
     * @param inner indentation to be used for child containers
     * @param step difference between inner and outer indentation
     */
    private Container(@Nonnull final Element elem,
            @Nullable final Container parent,
            @Nonnull final String outer,
            @Nonnull final String inner,
            @Nonnull final String step) {
        element = Constraint.isNotNull(elem, "element must not be null");
        parentContainer = parent;
        indentOuter = Constraint.isNotNull(outer, "outer indent must not be null");
        indentInner = Constraint.isNotNull(inner, "inner indent must not be null");
        indentStep = Constraint.isNotNull(step, "indent step must not be null");
    }

    /**
     * Constructor.
     * 
     * This variant is used internally to construct {@link Container}s
     * corresponding to child elements.
     * 
     * @param child child {@link Element} for which to construct a {@link Container}
     * @param parent parentContainer {@link Container} to link this child to
     */
    private Container(@Nonnull final Element child, @Nonnull final Container parent) {
        this(child, parent, parent.indentInner, parent.indentInner + parent.indentStep, parent.indentStep);
    }

    /**
     * Constructor.
     * 
     * This variant is used to start a {@link Container} tree
     * from an existing root {@link Element}. It is the only
     * public constructor.
     * 
     * @param root the existing {@link Element} to wrap.
     */
    public Container(@Nonnull final Element root) {
        this(root, null, "", DEFAULT_INDENT, DEFAULT_INDENT);
    }

    /**
     * Get the container's parent, if any.
     *
     * @return the container's parent container, or <code>null</code>
     */
    public @Nullable Container getParent() {
        return parentContainer;
    }

    /**
     * Return the wrapped {@link Element}.
     * 
     * @return the wrapped {@link Element}
     */
    @Nonnull
    public Element unwrap() {
        return element;
    }

    /**
     * Set the text content of the wrapped {@link Element}.
     * 
     * @param text the text content to set within the element
     */
    public void setText(@Nonnull final String text) {
        Constraint.isNotNull(text, "text content must not be null");
        element.setTextContent(text);
    }

    /**
     * Make sure that the container is able to receive additional child
     * containers.
     * 
     * If the container is empty, add in text content so that its opening
     * and closing tags are on different lines but are indented in the same
     * way.
     * 
     * The resulting container will have at least one child node, almost
     * always a text node starting with "\n".
     */
    public void prime() {
        if (!element.hasChildNodes()) {
            setText("\n" + indentOuter);
        }
    }

    /**
     * Find an existing child matching the {@link ElementMatcher}, if there is one.
     * 
     * @param matcher {@link ElementMatcher} to match against existing children
     * @return a child {@link Container} whose {@link Element} matches
     *      the supplied {@link ElementMatcher}.
     */
    public @Nullable Container findChild(final @Nonnull ElementMatcher matcher) {
        for (final Element e : ElementSupport.getChildElements(element)) {
            assert e != null;
            if (matcher.match(e)) {
                return new Container(e, this);
            }
        }
        return null;
    }

    /**
     * Find all existing children matching the {@link ElementMatcher}.
     * 
     * @param matcher {@link ElementMatcher} to match against existing children
     * @return a {@link List} of all matching children
     */
    public @Nonnull @NonnullElements List<Container> findChildren(final @Nonnull ElementMatcher matcher) {
        final List<Container> list = new ArrayList<>();
        for (final Element e : ElementSupport.getChildElements(element)) {
            assert e != null;
            if (matcher.match(e)) {
                list.add(new Container(e, this));
            }
        }
        return list;
    }

    /**
     * Add a child to the container.
     * 
     * @param child the new child element to add
     * @param adder strategy class to place the new child inside the container
     * @return a container wrapping the new child element
     */
    @Nonnull
    public Container addChild(@Nonnull final Element child,
            @Nonnull final ChildAddingStrategy adder) {
        return adder.addChild(this, child);
    }

    /**
     * Add a child to the container.
     * 
     * @param maker {@link ElementMaker} to create the new child element
     * @param adder strategy class to place the new child inside the container
     * @return a container wrapping the new child element
     */
    public @Nonnull Container addChild(@Nonnull final ElementMaker maker,
            @Nonnull final ChildAddingStrategy adder) {
        final Element child = maker.make(this);
        assert child != null;
        return addChild(child, adder);
    }

    /**
     * Locate a child container matching the {@link ElementMatcher}, creating one
     * if necessary.
     * 
     * @param matcher {@link ElementMatcher} to match against existing children
     * @param maker a {@link ElementMaker} to create a new child {@link Element}
     * @param adder a {@link ChildAddingStrategy} determining where to place the new child
     * @return a child {@link Container}, possibly just created
     */
    public @Nonnull Container locateChild(final @Nonnull ElementMatcher matcher,
            @Nonnull final ElementMaker maker,
            @Nonnull final ChildAddingStrategy adder) {

        // Return an existing child if one exists
        final Container existing = findChild(matcher);
        if (existing != null) {
            return existing;
        }

        // Construct a new child and add it in the right place
        return addChild(maker, adder);
    }
}
