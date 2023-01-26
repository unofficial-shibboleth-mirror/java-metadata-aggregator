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

import java.util.function.Predicate;

import javax.annotation.Nonnull;
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
import net.shibboleth.shared.annotation.constraint.NotEmpty;

/**
 * Item selection strategy which selects items on the basis of a boolean XPath expression.
 */
@ThreadSafe
public class XPathItemSelectionStrategy implements Predicate<Item<Element>> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XPathItemSelectionStrategy.class);

    /**
     * Compiled form of the expression.
     * 
     * {@link XPathExpression} objects are reusable but are not thread-safe, so access to the compiled expression must
     * be protected.
     */
    @Nonnull @GuardedBy("this")
    private final XPathExpression compiledExpression;

    /**
     * Constructor.
     * 
     * @param expression XPath expression to execute.
     * @param context Namespace context to use for the expression, expressed as a {@link NamespaceContext}.
     * @throws XPathExpressionException if there is a problem compiling the expression
     */
    public XPathItemSelectionStrategy(@Nonnull @NotEmpty final String expression,
            @Nonnull final NamespaceContext context) throws XPathExpressionException {
        final NamespaceContext namespaceContext;
        if (context == null) {
            namespaceContext = new SimpleNamespaceContext();
        } else {
            namespaceContext = context;
        }

        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(namespaceContext);

        compiledExpression = xpath.compile(expression);
    }

    @Override
    public synchronized boolean test(@Nonnull final Item<Element> item) {
        try {
            return compiledExpression.evaluateExpression(item.unwrap(), Boolean.class);
        } catch (final XPathExpressionException e) {
            log.warn("Exception thrown during XPath evaluation: " + e);
            return false;
        }
    }
}
