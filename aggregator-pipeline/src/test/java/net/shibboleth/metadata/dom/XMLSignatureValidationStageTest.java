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

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.shibboleth.metadata.AssertSupport;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import edu.vt.middleware.crypt.CryptException;
import edu.vt.middleware.crypt.util.CryptReader;

/** Unit test for {@link XMLSchemaValidationStage}. */
public class XMLSignatureValidationStageTest extends BaseDOMTest {
    
    private Certificate signingCert;
    
    @BeforeClass
    private void init() throws CryptException, IOException {
        setTestingClass(XMLSignatureValidationStage.class);
        signingCert = CryptReader.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingCert.pem")));
    }
    
    private DOMElementItem makeItem(final String name) throws XMLParserException {
        final Element input = readXmlData(name);
        return new DOMElementItem(input);
    }
    
    /**
     * Tests verifying a file with a valid signature.
     */
    @Test
    public void testValidSignature() throws Exception {
        final DOMElementItem item = makeItem("signed.xml");

        final List<DOMElementItem> mdCol = new ArrayList<>();
        mdCol.add(item);

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DOMElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XMLSignatureValidationStage.class, "test");
        
        // There should not have been any errors.
        final List<ErrorStatus> errors = result.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);

        // There should not have been any warnings either.
        final List<WarningStatus> warnings = result.getItemMetadata().get(WarningStatus.class);
        Assert.assertEquals(warnings.size(), 0);
    }

    /**
     * Test that a metadata element with an invalid signature is labelled with an error.
     */
    @Test
    public void testInvalidSignature() throws Exception {
        final DOMElementItem item = makeItem("badSignature.xml");
        final List<DOMElementItem> mdCol = new ArrayList<>();
        mdCol.add(item);

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertTrue(item.getItemMetadata().containsKey(ErrorStatus.class));
    }

    /**
     * Test that metadata elements that do not contain signature are appropriately labelled when valid signatures
     * are required.
     */
    @Test
    public void testRequiredSignature() throws Exception {
        final DOMElementItem item = makeItem("entities2.xml");
        
        final List<DOMElementItem> mdCol = new ArrayList<>();
        mdCol.add(item);

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setSignatureRequired(false);
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DOMElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XMLSignatureValidationStage.class, "test");

        final DOMElementItem item2 = makeItem("entities2.xml");
        
        mdCol.clear();
        mdCol.add(item2);
        
        stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setSignatureRequired(true);
        stage.setVerificationCertificate(signingCert);
        stage.initialize();
        stage.execute(mdCol);
        
        Assert.assertTrue(item2.getItemMetadata().containsKey(ErrorStatus.class));
    }
    
    /**
     * Test digest algorithm blacklist.
     */
    @Test
    public void testDigestBlacklist() throws Exception {
        final DOMElementItem item = makeItem("signed.xml");

        final List<DOMElementItem> mdCol = new ArrayList<>();
        mdCol.add(item);

        final Set<String> blacklist = new HashSet<>();
        blacklist.add("http://www.w3.org/2001/04/xmlenc#sha256");
        
        final XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.setBlacklistedDigests(blacklist);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DOMElementItem result = mdCol.iterator().next();
        
        // There should not have been one error, mentioning blacklisting.
        final List<ErrorStatus> errors = result.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        final String message = errors.get(0).getStatusMessage();
        Assert.assertTrue(message.contains("blacklist"));
    }

    /**
     * Test signature method blacklist.
     */
    @Test
    public void testSignatureMethodBlacklist() throws Exception {
        final DOMElementItem item = makeItem("signed.xml");

        final List<DOMElementItem> mdCol = new ArrayList<>();
        mdCol.add(item);

        final Set<String> blacklist = new HashSet<>();
        blacklist.add("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        
        final XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.setBlacklistedSignatureMethods(blacklist);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DOMElementItem result = mdCol.iterator().next();
        
        // There should not have been one error, mentioning blacklisting.
        final List<ErrorStatus> errors = result.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        final String message = errors.get(0).getStatusMessage();
        Assert.assertTrue(message.contains("blacklist"));
    }
    
    /**
     * Test that a signature with an empty reference is permitted by default.
     */
    @Test
    public void testEmptyRefPermitted() throws Exception {
        final DOMElementItem item = makeItem("emptyref.xml");

        final List<DOMElementItem> mdCol = new ArrayList<>();
        mdCol.add(item);

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DOMElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XMLSignatureValidationStage.class, "test");
        
        // There should not have been any errors.
        final List<ErrorStatus> errors = result.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);

        // There should not have been any warnings either.
        final List<WarningStatus> warnings = result.getItemMetadata().get(WarningStatus.class);
        Assert.assertEquals(warnings.size(), 0);
    }

    /**
     * Test that a signature with an empty reference is not permitted if disallowed.
     */
    @Test
    public void testEmptyRefNotPermitted() throws Exception {
        final DOMElementItem item = makeItem("emptyref.xml");

        final List<DOMElementItem> mdCol = new ArrayList<>();
        mdCol.add(item);

        final XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.setPermittingEmptyReferences(false);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        final DOMElementItem result = mdCol.iterator().next();
        
        // There should have been an error.
        final List<ErrorStatus> errors = result.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        final String message = errors.get(0).getStatusMessage();
        Assert.assertTrue(message.contains("reference"));
    }
}
