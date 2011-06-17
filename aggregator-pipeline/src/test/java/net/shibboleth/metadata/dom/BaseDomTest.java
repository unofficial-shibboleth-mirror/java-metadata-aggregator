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

import java.io.InputStream;
import java.io.StringReader;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.util.Assert;
import org.opensaml.util.StringSupport;
import org.opensaml.util.xml.BasicParserPool;
import org.opensaml.util.xml.ParserPool;
import org.opensaml.util.xml.SerializeSupport;
import org.opensaml.util.xml.XMLParserException;
import org.testng.annotations.BeforeClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** A base class for DOM related tests. */
public abstract class BaseDomTest {

    /** Initialized parser pool used to parser data. */
    private BasicParserPool parserPool;

    /**
     * Setup test class. Creates and initializes the parser pool. Set BouncyCastle as a JCE provider.
     * 
     * @throws XMLParserException thrown if there is a problem initializing the parser pool
     */
    @BeforeClass
    public void setUp() throws XMLParserException {
        parserPool = new BasicParserPool();
        parserPool.initialize();

        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Gets an initialized parser pool.
     * 
     * @return initialized parser pool, never null
     */
    public ParserPool getParserPool() {
        return parserPool;
    }

    /**
     * Reads in an XML file, parses it, and returns the document element. If the given path is relative (i.e., does not
     * start with a '/') it is assumed to be relative to /data.
     * 
     * @param path classpath path to the data file, never null
     * 
     * @return the document root of the data file, never null
     * 
     * @throws XMLParserException thrown if the file does not exists or there is a problem parsing it
     */
    public Element readXmlData(String path) throws XMLParserException {
        String trimmedPath = StringSupport.trimOrNull(path);
        Assert.isNotNull(trimmedPath, "Path may not be null or empty");

        if (!trimmedPath.startsWith("/")) {
            trimmedPath = "/data/" + trimmedPath;
        }

        InputStream input = BaseDomTest.class.getResourceAsStream(trimmedPath);
        if (input == null) {
            throw new XMLParserException(trimmedPath + " does not exist or is not readable");
        }

        return parserPool.parse(input).getDocumentElement();
    }

    /**
     * Checks whether two nodes are equal based on {@link Node#isEqualNode(Node)}. Both nodes are serialized, re-parsed,
     * and then compared for equality. This forces any changes made to the document that haven't yet been represented in
     * the DOM (e.g., declaration of used namespaces) to be flushed to the DOM.
     * 
     * @param expected the expected node against which the actual node will be tested, never null
     * @param actual the actual node tested against the expected node, never null
     * 
     * @throws XMLParserException thrown if there is a problem serializing and re-parsing the nodes
     */
    public void assertXmlEqual(Node expected, Node actual) throws XMLParserException {
        Assert.isNotNull(actual, "Actual Node may not be null");
        String serializedForm = SerializeSupport.nodeToString(expected);
        Element deserializedExpected = parserPool.parse(new StringReader(serializedForm)).getDocumentElement();

        Assert.isNotNull(expected, "Expected Node may not be null");
        serializedForm = SerializeSupport.nodeToString(actual);
        Element deserializedActual = parserPool.parse(new StringReader(serializedForm)).getDocumentElement();

        org.testng.Assert.assertTrue(deserializedExpected.isEqualNode(deserializedActual),
                "Actual Node does not equal expected Node");
    }
}