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

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.ItemSelectionStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Item selection strategy which selects items on the basis of a boolean XPath expression.
 */
@ThreadSafe
public class XPathItemSelectionStrategy implements ItemSelectionStrategy<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XPathItemSelectionStrategy.class);

    /**
     * Compiled form of the expression.
     * 
     * {@link XPathExpression} objects are reusable but are not thread-safe, so access to
     * the compiled expression must be protected.
     */
    private final XPathExpression compiledExpression;

    /** The {@link NamespaceContext} to use in interpreting the XPath expression. */
    private final NamespaceContext namespaceContext;

    /**
     * Constructor.
     * 
     * @param expression XPath expression to execute.
     * @param context Namespace context to use for the expression, expressed as a {@link NamespaceContext}.
     * @throws XPathExpressionException if there is a problem compiling the expression
     */
    public XPathItemSelectionStrategy(String expression, NamespaceContext context)
        throws XPathExpressionException {
        namespaceContext = context;

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        if (namespaceContext != null) {
            xpath.setNamespaceContext(namespaceContext);
        }
        compiledExpression = xpath.compile(expression);
    }

    /** {@inheritDoc} */
    public synchronized boolean isSelectedItem(DomElementItem item) {
        try {
            return (Boolean) compiledExpression.evaluate(item.unwrap(), XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            log.warn("Exception thrown during XPath evaluation: " + e);
            return false;
        }
    }

}