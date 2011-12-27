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

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline stage which allows filtering of @{link DomElementItem}s according to an XPath expression.
 * Each {@link DomElementItem} is removed if the XPath expression evaluates as {@code true}.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>xpathExpression</code></li>
 * <li><code>namespaceContext</code></li>
 * </ul>
 */
@ThreadSafe
public class XPathFilteringStage extends BaseStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XPathFilteringStage.class);

    /** The XPath expression to execute on each {@link DomElementItem}. */
    private String xpathExpression;

    /** The {@link NamespaceContext} to use in interpreting the XPath expression. */
    private NamespaceContext namespaceContext;

    /**
     * Gets the XPath expression to execute on each {@link DomElementItem}.
     * 
     * @return XPath expression to execute on each {@link DomElementItem}
     */
    public String getXpathExpression() {
        return xpathExpression;
    }

    /**
     * Sets the XPath expression to execute on each {@link DomElementItem}.
     * 
     * @param expression XPath expression to execute on each {@link DomElementItem}
     */
    public synchronized void setXpathExpression(String expression) {
        if (isInitialized()) {
            return;
        }

        xpathExpression = StringSupport.trimOrNull(expression);
    }

    /**
     * Gets the {@link NamespaceContext} to use in interpreting the XPath expression.
     * 
     * @return {@link NamespaceContext} to use in interpreting the XPath expression
     */
    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    /**
     * Sets the {@link NamespaceContext} to use in interpreting the XPath expression.
     * 
     * @param context {@link NamespaceContext} to use in interpreting the XPath expression
     */
    public synchronized void setNamespaceContext(NamespaceContext context) {
        if (isInitialized()) {
            return;
        }

        namespaceContext = context;
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
                Boolean filterThis = (Boolean) compiledExpression.evaluate(item.unwrap(), XPathConstants.BOOLEAN);
                if (filterThis) {
                    log.debug("removing item matching XPath condition");
                    iterator.remove();
                }
            } catch (XPathExpressionException e) {
                log.error("removing item due to XPath expression error", e);
                iterator.remove();
            }
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if(xpathExpression == null){
            throw new ComponentInitializationException("XPath expression can not be null or empty");
        }
        
        if(namespaceContext == null){
            namespaceContext = new SimpleNamespaceContext();
        }
    }
}