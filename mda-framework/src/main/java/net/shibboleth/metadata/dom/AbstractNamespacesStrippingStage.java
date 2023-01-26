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
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.XMLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.shared.collection.ClassToInstanceMultiMap;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.xml.ElementSupport;

/**
 * An abstract stage which removes all evidence of given XML namespaces from each metadata item.
 * 
 * Determining the affected namespaces is delegated to subclasses. Elements, attributes and
 * namespace prefix definitions associated with a given namespace will be removed
 * or retained as determined by the {@link #removingNamespace} function.
 * 
 * Attributes without an explicit namespace prefix will never be removed.
 *
 * @since 0.9.0
 */
@ThreadSafe
public abstract class AbstractNamespacesStrippingStage extends AbstractIteratingStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractNamespacesStrippingStage.class);

    /**
     * Determine whether a particular namespace should be stripped.
     * 
     * @param namespace potentially stripped namespace
     * @return <code>true</code> if this namespace should be stripped
     */
    protected abstract boolean removingNamespace(@Nullable final String namespace);

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) {
        final Element element = item.unwrap();
    
        /*
         * We can't, by definition, remove the document element from a {@link DOMElementItem},
         * so fail quickly if the document element is in the target namespace.
         */
        if (removingNamespace(element.getNamespaceURI())) {
            final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
            metadata.put(new ErrorStatus(getId(), "can't strip namespace from document element"));
            return;
        }
    
        processElement(element, 0);
    }

    /**
     * Process the attributes on an element.
     * 
     * Assumes that the element itself does not reside in the target namespace,
     * and that all child elements have already been processed.
     * 
     * @param element the {@link Element} to process
     */
    private void processAttributes(@Nonnull final Element element) {
        Constraint.isNotNull(element, "Element can not be null");
        
        /*
         * Process the attributes on this element.  Because the NamedNodeMap
         * associated with an element is "live", we need to collect the attributes
         * we want to remove and do that at the end.
         */
        final NamedNodeMap attributes = element.getAttributes();
        final List<Attr> removeTarget = new ArrayList<>();
        final List<Attr> removePrefix = new ArrayList<>();
        for (int aIndex = 0; aIndex < attributes.getLength(); aIndex++) {
            final Attr attribute = (Attr) attributes.item(aIndex);
            final String attrNamespace = attribute.getNamespaceURI();
            final String attrLocalName = attribute.getLocalName();
            log.trace("checking attribute {{}}:{}", attrNamespace, attrLocalName);
            
            // Handle namespace prefix definitions first
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attrNamespace)) {
                // namespace prefix definition
                if (removingNamespace(attribute.getTextContent())) {
                    // remove prefix definition
                    log.trace("   prefix {} definition; will remove", attrLocalName);
                    removeTarget.add(attribute);
                }
    
            } else if (attrNamespace != null && removingNamespace(attrNamespace)) {
                // remove attribute in target namespace
                // never remove attributes without an explicit namespace prefix
                log.trace("   in target namespace; will remove");
                removePrefix.add(attribute);
            }
        }
        
        /*
         * Actually remove attributes we don't want any more.
         * 
         * Remove the prefix declarations last, just in case that matters.
         */
        for (final Attr a: removeTarget) {
            element.removeAttributeNode(a);
        }
        for (final Attr a: removePrefix) {
            element.removeAttributeNode(a);
        }
    }

    /**
     * Process an individual DOM element.
     * 
     * @param element element to process
     * @param depth processing depth, starting with 0 for the document element.
     */
    private void processElement(@Nonnull final Element element, final int depth) {
        Constraint.isNotNull(element, "Element can not be null");
        log.trace("{}: element {}", depth, element.getLocalName());
    
        /*
         * If this element is in the target namespace, remove it from the DOM entirely and we're done.
         */
        if (removingNamespace(element.getNamespaceURI())) {
            log.trace("{}: removing element entirely", depth);
            element.getParentNode().removeChild(element);
            return;
        }
    
        /*
         * Recursively process the DOM below this element.
         */
        final List<Element> children = ElementSupport.getChildElements(element);
        for (final Element child : children) {
            processElement(child, depth+1);
        }
    
        /*
         * Process the attribute collection on this element,
         * including attributes acting as namespace prefix definitions.
         */
        processAttributes(element);
    }

}
