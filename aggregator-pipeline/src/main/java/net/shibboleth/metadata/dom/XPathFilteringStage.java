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
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.SimpleNamespaceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

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
public class XPathFilteringStage extends BaseStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XPathFilteringStage.class);

    /** The XPath expression to execute on each {@link DOMElementItem}. */
    private String xpathExpression;

    /** The {@link NamespaceContext} to use in interpreting the XPath expression. */
    private NamespaceContext namespaceContext = new SimpleNamespaceContext();

    /**
     * Gets the XPath expression to execute on each {@link DOMElementItem}.
     * 
     * @return XPath expression to execute on each {@link DOMElementItem}
     */
    @Nullable public String getXPathExpression() {
        return xpathExpression;
    }

    /**
     * Sets the XPath expression to execute on each {@link DOMElementItem}.
     * 
     * @param expression XPath expression to execute on each {@link DOMElementItem}
     */
    public synchronized void setXPathExpression(@Nonnull @NotEmpty final String expression) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        xpathExpression =
                Constraint.isNotNull(StringSupport.trimOrNull(expression), "XPath expression can not be null or empty");
    }

    /**
     * Gets the {@link NamespaceContext} to use in interpreting the XPath expression.
     * 
     * @return {@link NamespaceContext} to use in interpreting the XPath expression
     */
    @Nonnull public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    /**
     * Sets the {@link NamespaceContext} to use in interpreting the XPath expression.
     * 
     * @param context {@link NamespaceContext} to use in interpreting the XPath expression
     */
    public synchronized void setNamespaceContext(@Nullable final NamespaceContext context) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (context == null) {
            namespaceContext = new SimpleNamespaceContext();
        } else {
            namespaceContext = context;
        }
    }

    /** {@inheritDoc} */
    @Override public void doExecute(@Nonnull @NonnullElements final Collection<Item<Element>> metadataCollection) {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        if (namespaceContext != null) {
            xpath.setNamespaceContext(namespaceContext);
        }

        final XPathExpression compiledExpression;
        try {
            compiledExpression = xpath.compile(xpathExpression);
        } catch (final XPathExpressionException e) {
            log.error("error compiling XPath expression; no filtering performed", e);
            return;
        }

        final Iterator<Item<Element>> iterator = metadataCollection.iterator();
        while (iterator.hasNext()) {
            final Item<Element> item = iterator.next();
            try {
                final Boolean filterThis = (Boolean) compiledExpression.evaluate(item.unwrap(), XPathConstants.BOOLEAN);
                if (filterThis) {
                    log.debug("removing item matching XPath condition");
                    iterator.remove();
                }
            } catch (final XPathExpressionException e) {
                log.error("removing item due to XPath expression error", e);
                iterator.remove();
            }
        }
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        xpathExpression = null;
        namespaceContext = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (xpathExpression == null) {
            throw new ComponentInitializationException("XPath expression can not be null or empty");
        }
    }
}