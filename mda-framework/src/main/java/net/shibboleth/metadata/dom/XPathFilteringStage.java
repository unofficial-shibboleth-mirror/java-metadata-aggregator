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

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.SimpleNamespaceContext;

/**
 * Pipeline stage which allows filtering of @{link DomElementItem}s according to an XPath expression. Each
 * {@link DOMElementItem} is removed if the XPath expression evaluates as {@code true}.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>xpathExpression</code></li>
 * <li><code>namespaceContext</code></li>
 * </ul>
 */
@ThreadSafe
public class XPathFilteringStage extends AbstractStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XPathFilteringStage.class);

    /** The XPath expression to execute on each {@link DOMElementItem}. */
    @NonnullAfterInit @NotEmpty @GuardedBy("this")
    private String xpathExpression;

    /** The {@link NamespaceContext} to use in interpreting the XPath expression. */
    @Nonnull @GuardedBy("this")
    private NamespaceContext namespaceContext = new SimpleNamespaceContext();

    /**
     * Gets the XPath expression to execute on each {@link DOMElementItem}.
     * 
     * @return XPath expression to execute on each {@link DOMElementItem}
     */
    @NonnullAfterInit @NotEmpty public final synchronized String getXPathExpression() {
        return xpathExpression;
    }

    /**
     * Sets the XPath expression to execute on each {@link DOMElementItem}.
     * 
     * @param expression XPath expression to execute on each {@link DOMElementItem}
     */
    public synchronized void setXPathExpression(@Nonnull @NotEmpty final String expression) {
        checkSetterPreconditions();
        xpathExpression =
                Constraint.isNotNull(StringSupport.trimOrNull(expression), "XPath expression can not be null or empty");
    }

    /**
     * Gets the {@link NamespaceContext} to use in interpreting the XPath expression.
     * 
     * @return {@link NamespaceContext} to use in interpreting the XPath expression
     */
    @Nonnull public final synchronized NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    /**
     * Sets the {@link NamespaceContext} to use in interpreting the XPath expression.
     * 
     * @param context {@link NamespaceContext} to use in interpreting the XPath expression
     */
    public synchronized void setNamespaceContext(@Nullable final NamespaceContext context) {
        checkSetterPreconditions();
        if (context == null) {
            namespaceContext = new SimpleNamespaceContext();
        } else {
            namespaceContext = context;
        }
    }

    @Override
    public void doExecute(@Nonnull @NonnullElements final List<Item<Element>> metadataCollection)
            throws StageProcessingException {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(getNamespaceContext());

        final XPathExpression compiledExpression;
        try {
            compiledExpression = xpath.compile(getXPathExpression());
        } catch (final XPathExpressionException e) {
            // This should never occur, as we attempted the same operation at initialization time.
            throw new StageProcessingException("error compiling XPath expression", e);
        }

        final Iterator<Item<Element>> iterator = metadataCollection.iterator();
        while (iterator.hasNext()) {
            final Item<Element> item = iterator.next();
            try {
                if (compiledExpression.evaluateExpression(item.unwrap(), Boolean.class)) {
                    log.debug("removing item matching XPath condition");
                    iterator.remove();
                }
            } catch (final XPathExpressionException e) {
                // Rare in practice; happens, for example, if you use a $variable, as there is
                // no variable resolver attached to our XPath object.
                throw new StageProcessingException("error evaluating XPath expression", e);
            }
        }
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (xpathExpression == null) {
            throw new ComponentInitializationException("XPath expression can not be null or empty");
        }
        
        // Check to see if the expression is valid
        final var factory = XPathFactory.newInstance();
        final var xpath = factory.newXPath();
        xpath.setNamespaceContext(namespaceContext);
        try {
            xpath.compile(xpathExpression);
        } catch (final XPathExpressionException e) {
            throw new ComponentInitializationException("error compiling XPath expression", e);
        }
    }
}
