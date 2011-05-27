/*
 * Licensed to the University Corporation for Advanced Internet Development, Inc.
 * under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache 
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
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.shibboleth.metadata.pipeline.BaseStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline stage which allows filtering of @{link DomElementItem}s according to
 * an XPath expression.  Each {@link Item} is removed if the XPath expression
 * evaluates as {@code true}.
 */
public class XPathFilteringStage extends BaseStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XPathFilteringStage.class);

    /**
     * Simple implementation of {@link NamespaceContext} based on a map from
     * prefix values to corresponding URIs.  This is not complete implementation,
     * but does have enough functionality for use within XPath evaluation.
     */
    private static class SimpleNamespaceContext implements NamespaceContext {

        /** Mapping from prefix values to the corresponding namespace URIs. */
        private final Map<String, String> prefixMappings;

        /**
         * Constructor.
         *
         * @param mappings  Maps prefix values to the corresponding
         *                  namespace URIs.
         */
        public SimpleNamespaceContext(Map<String, String> mappings) {
            this.prefixMappings = mappings;
        }

        /** {@inheritDoc} */
        public String getNamespaceURI(String prefix) {
            return prefixMappings.get(prefix);
        }

        /** {@inheritDoc} */
        public String getPrefix(String namespaceURI) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        public Iterator<String> getPrefixes(String namespaceURI) {
            throw new UnsupportedOperationException();
        }

    }

    /** The XPath expression to execute on each {@link DomElementItem}. */
    private final String xpathExpression;
    
    /** The {@link NamespaceContext} to use in interpreting the XPath expression. */
    private final NamespaceContext namespaceContext;

    /**
     * Constructor.
     *
     * @param expression    XPath expression to execute.
     * @param context       Namespace context to use for the expression, expressed
     *                      as a {@link NamespaceContext}.
     */
    public XPathFilteringStage(String expression, NamespaceContext context) {
        xpathExpression = expression;
        namespaceContext = context;
    }

    /**
     * Constructor.
     *
     * @param expression        XPath expression to execute.
     * @param prefixMappings    Namespace context to use for the expression, expressed
     *                          as a map from prefix values to namespace URIs.
     */
    public XPathFilteringStage(String expression, Map<String, String> prefixMappings) {
        this(expression, new SimpleNamespaceContext(prefixMappings));
    }

    /** {@inheritDoc} */
    public void doExecute(Collection<DomElementItem> metadataCollection) {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        if (namespaceContext != null) {
            xpath.setNamespaceContext(namespaceContext);
        }

        XPathExpression compiledExpression;
        try {
            compiledExpression = xpath.compile(xpathExpression);
        } catch (XPathExpressionException e) {
            log.error("error compiling XPath expression; no filtering performed", e);
            return;
        }

        Iterator<DomElementItem> iterator = metadataCollection.iterator();
        while (iterator.hasNext()) {
            DomElementItem item = iterator.next();
            try {
                Boolean filterThis = (Boolean)compiledExpression.evaluate(item.unwrap(), XPathConstants.BOOLEAN);
                if (filterThis) {
                    log.info("removing item matching XPath condition");
                    iterator.remove();
                }
            } catch (XPathExpressionException e) {
                log.error("removing item due to XPath expression error", e);
                iterator.remove();
            }
        }
    }

}
