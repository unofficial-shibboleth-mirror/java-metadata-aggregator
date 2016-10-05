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

import net.shibboleth.metadata.AssertSupport;
import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.cryptacular.util.CertUtil;
import org.cryptacular.util.KeyPairUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** {@link XMLSignatureSigningStage} unit test. */
public class XMLSignatureSigningStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public XMLSignatureSigningStageTest() {
        super(XMLSignatureSigningStage.class);
    }

    /** Test signing with and verifying the result against a known good. */
    @Test
    public void testSigning() throws Exception {
        Element testInput = readXMLData("input.xml");

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(new DOMElementItem(testInput));

        PrivateKey signingKey = KeyPairUtil.readPrivateKey(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingKey.pem")));
        X509Certificate signingCert = (X509Certificate) CertUtil.readCertificate(XMLSignatureSigningStageTest.class
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
        Assert.assertEquals(mdCol.size(), 1);

        final Item<Element> result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XMLSignatureSigningStage.class, "test");

        Element expected = readXMLData("output.xml");
        assertXMLIdentical(expected, result.unwrap());
    }
    
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
    
}