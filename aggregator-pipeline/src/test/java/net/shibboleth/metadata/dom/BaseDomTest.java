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
import java.net.URL;
import java.security.Security;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resource.ClasspathResource;
import net.shibboleth.utilities.java.support.resource.Resource;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
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
     * @throws ComponentInitializationException thrown if there is a problem initializing the parser pool
     */
    @BeforeClass public void setUp() throws ComponentInitializationException {
        XMLUnit.setIgnoreWhitespace(true);

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
     * Variant of ClasspathResource that patches round the problem described
     * in JSPT-21.
     */
    private class FixedClasspathResource extends ClasspathResource {
    
        /**
         * Constructor.
         *
         * @param resourcePath classpath path to the resource
         */
        public FixedClasspathResource(final String resourcePath) {
            super(resourcePath);
            // Work around the fact that ClasspathResource doesn't handle location correctly
            final URL resourceURL = this.getClass().getClassLoader().getResource(resourcePath);
            setLocation(resourceURL.toExternalForm());
        }
        
    }
    
    /**
     * Helper method to acquire a ClasspathResource based on the given resource path.
     * 
     * @param resourcePath classpath path to the resource
     * @return the data file as a resource
     */
    public Resource getClasspathResource(final String resourcePath) {
        return new FixedClasspathResource(resourcePath);
    }
    
    /**
     * Acquires a Resource encapsulating a data file located in the resource directory named for the class that is being tested. For example,
     * for the class <code>net.shibboleth.metadata.dom.saml.RemoveContactPersonStage</code>, the data file will be
     * looked for in <code>src/test/resources/net/shibboleth/metadata/dom/saml/RemoveContactPersonStage/</code>.
     * 
     * @param classBeingTested the class that is being unit tested
     * @param dataFile the data file to be encapsulated as a resource
     * 
     * @return the data file as a Resource
     */
    public Resource getTestRelativeClasspathResource(final Class classBeingTested, final String dataFile) {
        StringBuilder absoluteDataPath = new StringBuilder();
        absoluteDataPath.append("/").append(classBeingTested.getName().replace('.', '/'));
        absoluteDataPath.append("/").append(dataFile);
        return new FixedClasspathResource(absoluteDataPath.toString());
    }
    
    /**
     * Parses and XML data file located in the resource directory named for the class that is being tested. For example,
     * for the class <code>net.shibboleth.metadata.dom.saml.RemoveContactPersonStage</code>, the data file will be
     * looked for in <code>src/test/resources/net/shibboleth/metadata/dom/saml/RemoveContactPersonStage/</code>.
     * 
     * @param classBeingTested the class that is being unit tested
     * @param dataFile the data file to be loaded in and read
     * 
     * @return the document root element
     * 
     * @throws XMLParserException thrown if the data file does not exist or is not valid XML
     */
    public Element readTestRelativeXmlData(final Class classBeingTested, final String dataFile)
            throws XMLParserException {
        StringBuilder absoluteDataPath = new StringBuilder();
        absoluteDataPath.append("/").append(classBeingTested.getName().replace('.', '/'));
        absoluteDataPath.append("/").append(dataFile);

        InputStream input = BaseDomTest.class.getResourceAsStream(absoluteDataPath.toString());
        if (input == null) {
            throw new XMLParserException(absoluteDataPath + " does not exist or is not readable");
        }

        return parserPool.parse(input).getDocumentElement();
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
        Constraint.isNotNull(trimmedPath, "Path may not be null or empty");

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
     * Checks whether two nodes are identical based on {@link Diff#identical()}.
     * 
     * @param expected the expected node against which the actual node will be tested, never null
     * @param actual the actual node tested against the expected node, never null
     */
    public void assertXmlIdentical(Node expected, Node actual) {
        Constraint.isNotNull(expected, "Expected Node may not be null");
        Constraint.isNotNull(actual, "Actual Node may not be null");

        Diff diff = new Diff(expected.getOwnerDocument(), actual.getOwnerDocument());
        if (!diff.identical()) {
            org.testng.Assert.fail(diff.toString());
        }
    }
}