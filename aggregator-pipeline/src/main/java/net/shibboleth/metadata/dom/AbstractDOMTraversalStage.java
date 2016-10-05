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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.collection.ClassToInstanceMultiMap;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/**
 * A DOM traversal class using the template method pattern.
 */
@ThreadSafe
public abstract class AbstractDOMTraversalStage extends BaseStage<Element> {
    
    /** Context for a particular traversal. */
    protected class TraversalContext {
        
        /** The {@link Item} this traversal is being performed on. */
        private final Item<Element> item;
        
        /** Map of data for this traversal. */
        private final ClassToInstanceMultiMap<Object> stash = new ClassToInstanceMultiMap<>();
        
        /**
         * Constructor.
         * 
         * @param contextItem the {@link Item} this traversal is being performed on.
         */
        public TraversalContext(@Nonnull final Item<Element> contextItem) {
            item = contextItem;
        }
        
        /**
         * Get the {@link Item} this traversal is being performed on.
         * 
         * @return the context {@link Item}
         */
        public Item<Element> getItem() {
            return item;
        }
        
        /**
         * Get the stashed information for this traversal.
         * 
         * @return the stashed information
         */
        public ClassToInstanceMultiMap<Object> getStash() {
            return stash;
        }
        
    }

    /**
     * Indicates whether the visitor should be applied to a particular {@link Element}.
     * 
     * @param element {@link Element} to which we may wish to apply the visitor
     * 
     * @return <code>true</code> if the visitor should be applied to this {@link Element}.
     */
    protected abstract boolean applicable(@Nonnull final Element element);

    /**
     * Visit a particular {@link Element}.
     * 
     * @param element the {@link Element} to visit
     * @param context the traversal context
     * @throws StageProcessingException if errors occur during processing
     */
    protected abstract void visit(@Nonnull final Element element, @Nonnull final TraversalContext context)
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
    private void traverse(@Nonnull final Element element, @Nonnull final TraversalContext context) 
        throws StageProcessingException {
        final List<Element> children = ElementSupport.getChildElements(element);
        for (final Element child : children) {
            traverse(child, context);
        }
        if (applicable(element)) {
            visit(element, context);
        }
    }
    
    @Override
    protected void doExecute(final Collection<Item<Element>> itemCollection) throws StageProcessingException {
        for (final Item<Element> item : itemCollection) {
            final Element docElement = item.unwrap();
            final TraversalContext context = new TraversalContext(item);
            traverse(docElement, context);
        }
    }

    /**
     * Returns the {@link Element} representing the EntityDescriptor which is the
     * closest-containing ancestor of the given element.
     * 
     * @param element {@link Element} to locate the ancestor Entity of.
     * @return ancestor EntityDescriptor {@link Element}, or null.
     */
    protected Element ancestorEntity(@Nonnull final Element element) {
        assert element != null;
        for (Element e = element; e != null; e = (Element) e.getParentNode()) {
            if (SAMLMetadataSupport.isEntityDescriptor(e)) {
                return e;
            }
        }
        return null;
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
        assert item != null;
        assert element != null;
        assert error != null;
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        String prefix = "";
        if (SAMLMetadataSupport.isEntitiesDescriptor(element)) {
            final Element entity = ancestorEntity(element);
            final Attr id = entity.getAttributeNode("ID");
            if (id != null) {
                prefix = id.getTextContent() + ": ";
            } else {
                final Attr entityID = entity.getAttributeNode("entityID");
                if (entityID != null) {
                    prefix = entityID.getTextContent() + ": ";
                }
            }
        }
        metadata.put(new ErrorStatus(getId(), prefix + error));
    }

}
