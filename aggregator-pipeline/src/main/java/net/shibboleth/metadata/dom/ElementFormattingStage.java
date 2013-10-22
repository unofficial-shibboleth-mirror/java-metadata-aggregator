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
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.util.ItemMetadataSupport;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.logic.TrimOrNullStringFunction;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * A stage that formats a collection of DOM Elements.
 * 
 * <p>
 * Note, this stage uses Xalan so Xalan must be included on the classpath. However, Xalan does <strong>not</strong> have
 * to be the system default {@link TransformerFactory} implementation.
 * </p>
 */
public class ElementFormattingStage extends BaseStage<Element> {

    /** Line separator character to use. Default value: \n */
    private String lineSeparator = "\n";

    /** Whether to indent elements. Default value: true */
    private boolean indented = true;

    /** Number of spaces used to indent elements. Default value: 4 */
    private int indentSize = 4;

    /**
     * List of elements whose content should be wrapped in CDATA sections. Elements are specified either by their local
     * name, if they are not in a namespace, or via the form '{' + namespace URI + '}' + local name if they are in a
     * namespace.
     */
    private List<String> cdataSectionElements = Collections.emptyList();

    /** The factory used to create the {@link Transformer} used to format the elements. */
    private TransformerFactory transformerFactory;

    /**
     * Gets the line separator character to use.
     * 
     * @return line separator character to use
     */
    @Nonnull public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Sets the line separator character to use.
     * 
     * @param separator line separator character to use
     */
    public synchronized void setLineSeparator(@Nullable final String separator) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (separator == null) {
            lineSeparator = "";
        } else {
            lineSeparator = separator;
        }
    }

    /**
     * Gets whether to indent elements.
     * 
     * @return whether to indent elements
     */
    public boolean isIndented() {
        return indented;
    }

    /**
     * Sets whether to indent elements.
     * 
     * @param indentElements whether to indent elements
     */
    public synchronized void setIndented(boolean indentElements) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        indented = indentElements;
    }

    /**
     * Gets the number of spaces to use when indenting elements.
     * 
     * @return number of spaces to use when indenting elements
     */
    public int getIndentSize() {
        return indentSize;
    }

    /**
     * Sets the number of spaces to use when indenting elements.
     * 
     * @param size number of spaces to use when indenting elements, must be 0 or greater
     */
    public synchronized void setIndentSize(int size) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        indentSize = (int) Constraint.isGreaterThanOrEqual(0, size, "Indentation size must be 0 or greater");
    }

    /**
     * Gets the list of elements whose content should be wrapped in CDATA sections.
     * 
     * <p>
     * Elements are specified either by their local name, if they are not in a namespace, or via the form '{' +
     * namespace URI + '}' + local name if they are in a namespace.
     * </p>
     * 
     * @return list of elements whose content should be wrapped in CDATA sections
     */
    @Nonnull @NonnullElements @Unmodifiable public List<String> getCdataSectionElements() {
        return cdataSectionElements;
    }

    /**
     * Sets the list of elements whose content should be wrapped in CDATA sections.
     * 
     * @param elements list of elements whose content should be wrapped in CDATA sections
     */
    public synchronized void setCdataSectionElements(@Nullable @NullableElements final List<String> elements) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        final ArrayList<String> checkedElements = new ArrayList<>();
        CollectionSupport.addIf(checkedElements, elements, Predicates.notNull(), new TrimOrNullStringFunction());

        cdataSectionElements = ImmutableList.copyOf(checkedElements);
    }

    /** {@inheritDoc} */
    protected void doExecute(final Collection<Item<Element>> itemCollection) throws StageProcessingException {
        final ArrayList<Item<Element>> transformedItems = Lists.newArrayListWithExpectedSize(itemCollection.size());

        for (Item<Element> item : itemCollection) {
            final DOMSource source = new DOMSource(item.unwrap());
            final DOMResult result = new DOMResult();

            try {
                getTransformer().transform(source, result);
                final Item<Element> transformedItem =
                        new DOMElementItem(((Document) result.getNode()).getDocumentElement());
                ItemMetadataSupport.addAll(transformedItem, item.getItemMetadata().values());
                transformedItems.add(transformedItem);
            } catch (TransformerException e) {
                throw new StageProcessingException("Unable to format Element", e);
            }
        }

        itemCollection.clear();
        itemCollection.addAll(transformedItems);
    }

    /**
     * Builds the {@link Transformer} that will be used to format the element.
     * 
     * @return the {@link Transformer} that will be used to format the element
     * 
     * @throws StageProcessingException thrown if the configuration of the {@link TransformerFactory} has become
     *             corrupted since stage initialization
     */
    protected Transformer getTransformer() throws StageProcessingException {
        try {
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            
            transformer.setOutputProperty("{http://xml.apache.org/xalan}line-separator", lineSeparator);

            if (indented) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount",
                        Integer.toString(indentSize));
            } else {
                transformer.setOutputProperty(OutputKeys.INDENT, "no");
            }

            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                    StringSupport.listToStringValue(cdataSectionElements, ","));

            return transformer;
        } catch (TransformerConfigurationException e) {
            // in theory this shouldn't be possible as we've already checked the config during initialization
            // and nothing else can change the factory, but just in case...
            throw new StageProcessingException("Unable to create a new Transformer", e);
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        transformerFactory =
                TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl", getClass()
                        .getClassLoader());
    }
}