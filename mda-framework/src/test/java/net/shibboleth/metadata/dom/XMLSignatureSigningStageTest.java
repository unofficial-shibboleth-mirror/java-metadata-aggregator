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

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.cryptacular.util.CertUtil;
import org.cryptacular.util.KeyPairUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.input.NormalizedSource;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.ds.XMLDSIGSupport;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.logic.ConstraintViolationException;
import net.shibboleth.shared.xml.ElementSupport;
import net.shibboleth.shared.xml.SerializeSupport;
import net.shibboleth.shared.xml.XMLParserException;

/** {@link XMLSignatureSigningStage} unit test. */
public class XMLSignatureSigningStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public XMLSignatureSigningStageTest() {
        super(XMLSignatureSigningStage.class);
    }

    /**
     * Utility to return an item collection made from a single named resource.
     *
     * @param fileName name of the resource
     * @return collection made from the resource
     * @throws XMLParserException if the resource can't be parsed
     */
    private @Nonnull List<Item<Element>> getInput(@Nonnull final String fileName) throws XMLParserException {
        final Element testInput = readXMLData(fileName);
        final List<Item<Element>> list = new ArrayList<>();
        list.add(new DOMElementItem(testInput));
        return list;
    }

    /**
     * Returns whether an element or its children contain a literal CR.
     *
     * This operates by serializing the element and seeing whether the result
     * contains an entity-encoded CR.
     *
     * @param element {@link Element} to examine
     * @return <code>true</code> if the serialized element contains an encoded CR
     */
    private boolean containsCRs(@Nonnull final Element element) {
        final String string = SerializeSupport.nodeToString(element);
        return string.contains("&#13;");
    }

    /**
     * Test signing with and verifying the result against a known good.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testSigning() throws Exception {
        Element testInput = readXMLData("input.xml");

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(new DOMElementItem(testInput));

        PrivateKey signingKey = KeyPairUtil.readPrivateKey(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingKey.pem")));
        assert signingKey != null;
        X509Certificate signingCert = CertUtil.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingCert.pem")));
        final List<X509Certificate> certs = new ArrayList<>();
        certs.add(signingCert);

        final XMLSignatureSigningStage stage = new XMLSignatureSigningStage();
        stage.setId("test");
        stage.setIncludeKeyValue(false);
        stage.setIncludeX509IssuerSerial(true);
        stage.setPrivateKey(signingKey);
        stage.setCertificates(certs);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);

        final Item<Element> result = mdCol.iterator().next();

        Element expected = readXMLData("output.xml");
        assertXMLIdentical(expected, result.unwrap());
    }

    /**
     * MDA-196: XMLSignatureSigningStage's includeX509SubjectName property causes exception.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testMDA196() throws Exception {
        final Element testInput = readXMLData("input.xml");

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(new DOMElementItem(testInput));

        final PrivateKey signingKey = KeyPairUtil.readPrivateKey(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingKey.pem")));
        assert signingKey != null;
        final X509Certificate signingCert = CertUtil.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingCert.pem")));
        final List<X509Certificate> certs = new ArrayList<>();
        certs.add(signingCert);

        final XMLSignatureSigningStage stage = new XMLSignatureSigningStage();
        stage.setId("test");
        stage.setIncludeKeyValue(false);
        stage.setIncludeX509IssuerSerial(true);
        stage.setIncludeX509SubjectName(true);
        stage.setPrivateKey(signingKey);
        stage.setCertificates(certs);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);

        final Item<Element> result = mdCol.iterator().next();

        final Element expected = readXMLData("mda196.xml");
        assertXMLIdentical(expected, result.unwrap());
    }

    @SuppressWarnings("null")
    @Test
    public void testSetIdAttributeNamesNull() throws Exception {
        final XMLSignatureSigningStage stage = new XMLSignatureSigningStage();
        stage.setId("test");
        try {
            stage.setIdAttributeNames(null);
            Assert.fail("expected a constraint exception");
        } catch (ConstraintViolationException e) {
            // expected
        }
    }

    @Test
    public void mda216Default() throws Exception {
        final XMLSignatureSigningStage stage = new XMLSignatureSigningStage();
        Assert.assertTrue(stage.isRemovingCRsFromSignature());
    }

    /**
     * Test the functionality of the MDA-216 fix.
     *
     * This test is way over-specified. It <em>will</em> fail if the Santuario
     * library ever stops putting CRs into its output, but it will also fail
     * if Santuario starts putting CRs into its output in other places.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void setRemovingCRsFromSignature() throws Exception {
        PrivateKey signingKey = KeyPairUtil.readPrivateKey(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingKey.pem")));
        assert signingKey != null;
        X509Certificate signingCert = CertUtil.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingCert.pem")));
        final List<X509Certificate> certs = new ArrayList<>();
        certs.add(signingCert);

        /*
         * The first result uses the default value.
         */
        final @Nonnull List<Item<Element>> mdCol1 = getInput("input.xml");

        final XMLSignatureSigningStage stage1 = new XMLSignatureSigningStage();
        stage1.setId("test");
        stage1.setIncludeKeyValue(false);
        stage1.setIncludeX509IssuerSerial(true);
        stage1.setPrivateKey(signingKey);
        stage1.setCertificates(certs);
        stage1.initialize();

        stage1.execute(mdCol1);
        stage1.destroy();
        Assert.assertEquals(mdCol1.size(), 1);

        final Item<Element> result1 = mdCol1.iterator().next();
        Assert.assertFalse(containsCRs(result1.unwrap()));

        /*
         * The second result disables CR stripping.
         */
        final List<Item<Element>> mdCol2 = getInput("input.xml");

        final XMLSignatureSigningStage stage2 = new XMLSignatureSigningStage();
        stage2.setId("test");
        stage2.setIncludeKeyValue(false);
        stage2.setIncludeX509IssuerSerial(true);
        stage2.setPrivateKey(signingKey);
        stage2.setCertificates(certs);
        stage2.setRemovingCRsFromSignature(false);
        stage2.initialize();

        stage2.execute(mdCol2);
        stage2.destroy();
        Assert.assertEquals(mdCol2.size(), 1);

        final Item<Element> result2 = mdCol2.iterator().next();

        /*
         * Compare the two results.
         */
        final Source source1 = new NormalizedSource(Input.fromNode(result1.unwrap()).build());
        final Source source2 = new NormalizedSource(Input.fromNode(result2.unwrap()).build());
        final Diff diff = DiffBuilder.compare(source1).withTest(source2)
                .checkForIdentical()
                .build();

        /*
         * As we are running under Java 11 or later, with the latest Santuario, we expect the two
         * results to be different. Under previous versions of Java, we expected them to be the same.
         *
         * We ascertained in other tests that they are both valid.
         */
        Assert.assertTrue(containsCRs(result2.unwrap()), "expected CRs in result");
        Assert.assertTrue(diff.hasDifferences(), "results were same, expected different");
    }
    
    @Test
    public void mda224defaultingPublicKeyFromCertificate() throws Exception {
        final var signingKey = KeyPairUtil.readPrivateKey(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingKey.pem")));
        assert signingKey != null;
        final var md = getInput("input.xml");
        final var stage = new XMLSignatureSigningStage();
        
        final var signingCert = CertUtil.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingCert.pem")));
        assert signingCert != null;
        final var certs = CollectionSupport.listOf(signingCert);

        stage.setId("test");
        stage.setIncludeKeyValue(true);
        stage.setPrivateKey(signingKey);
        stage.setCertificates(certs);
        stage.initialize();

        stage.execute(md);
        stage.destroy();
        Assert.assertEquals(md.size(), 1);
        
        final var entitiesDescriptor = md.get(0).unwrap(); // document element
        final var keyInfo = extractKeyInfo(entitiesDescriptor);
        assert keyInfo != null;

        // If we had a certificate, expect to see that as an X509Data, and expect to see
        // its public key as well as a KeyValue
        Assert.assertTrue(hasChildNamed(keyInfo, new QName(XMLSignature.XMLNS, "X509Data")));
        Assert.assertTrue(hasChildNamed(keyInfo, new QName(XMLSignature.XMLNS, "KeyValue")));
    }

    @Test
    public void mda224defaultingPublicKeyFromAbsentCertificate() throws Exception {
        final var signingKey = KeyPairUtil.readPrivateKey(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingKey.pem")));
        assert signingKey != null;
        final var md = getInput("input.xml");
        final var stage = new XMLSignatureSigningStage();
        
        stage.setId("test");
        stage.setIncludeKeyValue(true);
        stage.setPrivateKey(signingKey);
        stage.initialize();

        stage.execute(md);
        stage.destroy();
        Assert.assertEquals(md.size(), 1);
        
        final var entitiesDescriptor = md.get(0).unwrap(); // document element
        final var keyInfo = extractKeyInfo(entitiesDescriptor);

        // If we didn't have a public key or a certificate, we don't expect to see a KeyInfo at all.
        Assert.assertNull(keyInfo);
    }

    @Test
    public final void testDefaultHash() {
        final var stage = new XMLSignatureSigningStage();
        Assert.assertEquals(stage.getShaVariant(), XMLSignatureSigningStage.ShaVariant.SHA256);
    }

    private boolean hasChildNamed(@Nonnull final Element element, @Nonnull final QName name) {
        return !ElementSupport.getChildElements(element, name).isEmpty();
    }

    private Element extractKeyInfo(@Nonnull final Element root) {
        final var signature = ElementSupport.getFirstChildElement(root, XMLDSIGSupport.SIGNATURE_NAME);
        assert signature != null;
        final var keyInfos = signature.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo");
        Assert.assertNotNull(keyInfos);
        if (keyInfos.getLength() != 0) {
            return (Element)keyInfos.item(0);            
        }
        return null;
    }
}
