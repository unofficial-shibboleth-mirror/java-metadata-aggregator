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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.collection.ClassToInstanceMultiMap;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/**
 * An abstract DOM traversal class using the template method pattern.
 *
 * <p>
 * A context object, extending {@link DOMTraversalContext}, is created by the
 * implementing subclass and passed to the {@link #visit} method when
 * each applicable {@link Element} is visited. In very simple cases, the
 * {@link SimpleDOMTraversalContext} may suffice, but more complicated
 * behaviour can be built up by extending or re-implementing that class.
 * </p>
 * 
 * <p>
 * Which {@link Element} nodes are visited during the traversal is controlled
 * by n {@link #applicable} method implemented by subclasses. Traversal within
 * elements to DOM attributes is not supported directly.
 * </p>
 * 
 * <p>
 * At the end of the traversal, the context's {@link DOMTraversalContext#end()}
 * method is called to perform any post-processing required.
 * </p>
 *
 * <p>
 * This {@link Stage} 
 * @param <C> the context to carry through the traversal
 *
 * @since 0.9.0
 */
@ThreadSafe
public abstract class AbstractDOMTraversalStage<C extends DOMTraversalContext>
    extends AbstractIteratingStage<Element> {

    /**
     * Build the context for a particular traversal.
     *
     * @param item the {@link Item} we are traversing
     *
     * @return an appropriate context
     */
    @Nonnull
    protected abstract C buildContext(@Nonnull final Item<Element> item);

    /**
     * Indicates whether the visitor should be applied to a particular {@link Element}.
     * 
     * @param element {@link Element} to which we may wish to apply the visitor
     * @param context {@link DOMTraversalContext} implementation being used to manage the traversal
     * 
     * @return <code>true</code> if the visitor should be applied to this {@link Element}.
     */
    protected abstract boolean applicable(@Nonnull final Element element,
            @Nonnull final C context);

    /**
     * Visit a particular {@link Element}.
     * 
     * @param element the {@link Element} to visit
     * @param context the traversal context
     * @throws StageProcessingException if errors occur during processing
     */
    protected abstract void visit(@Nonnull final Element element, @Nonnull final C context)
        throws StageProcessingException;

    /**
     * Depth-first traversal of the DOM tree rooted in an element, applying the
     * visitor when appropriate.  The traversal snapshots the child elements at
     * each level, so that the visitor could in principle reorder or delete them
     * during processing.
     * 
     * @param element {@link Element} to start from
     * @param context context for the traversal
     * @throws StageProcessingException if errors occur during processing
     */
    private void traverse(@Nonnull final Element element, @Nonnull final C context) 
        throws StageProcessingException {
        final List<Element> children = ElementSupport.getChildElements(element);
        for (@Nonnull final Element child : children) {
            traverse(child, context);
        }
        if (applicable(element, context)) {
            visit(element, context);
        }
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element docElement = item.unwrap();
        final C context = buildContext(item);
        traverse(docElement, context);
        context.end();
    }

    /**
     * Computes a prefix to be put in front of the message in {@link #addError}.
     *
     * @param element {@link Element} forming the context for the prefix
     * @return a prefix for the error message
     */
    protected String errorPrefix(@Nonnull final Element element) {
        return "";
    }

    /**
     * Add an {@link ErrorStatus} to the given item, in respect of the given {@link Element}.
     * If the item is an EntitiesDescriptor, interpose an identifier for the individual
     * EntityDescriptor.
     * 
     * @param item      {@link Item} to add the error to
     * @param element   {@link Element} the error reflects
     * @param error     error text
     */
    protected void addError(@Nonnull final Item<Element> item, @Nonnull final Element element,
            @Nonnull final String error) {
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final String prefix = errorPrefix(element);
        metadata.put(new ErrorStatus(getId(), prefix + error));
    }

}
