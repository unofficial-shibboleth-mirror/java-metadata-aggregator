/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.dom.stage;

import java.security.cert.Certificate;
import java.util.ArrayList;

import net.shibboleth.metadata.AssertSupport;
import net.shibboleth.metadata.dom.BaseDomTest;
import net.shibboleth.metadata.dom.DomMetadata;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import edu.vt.middleware.crypt.util.CryptReader;

/** Unit test for {@link XMLSchemaValidationStage}. */
public class XMLSignatureValidationStageTest extends BaseDomTest {

    /**
     * Tests verifying a file with a valid signature.
     * 
     * @throws Exception thrown if there is a problem verifying the data
     */
    @Test
    public void testValidSignature() throws Exception {
        Element testInput = readXmlData("signedSamlMetadata.xml");

        ArrayList<DomMetadata> mdCol = new ArrayList<DomMetadata>();
        mdCol.add(new DomMetadata(testInput));

        Certificate signingCert = CryptReader.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/signingCert.pem"));

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomMetadata result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XMLSignatureValidationStage.class, "test");
    }

    /**
     * Test that a metadata element with an invalid signature is removed from the collection.
     * 
     * @throws Exception thrown if there is a problem checking the signature
     */
    @Test
    public void testInvalidSignature() throws Exception {
        Element testInput = readXmlData("badSignatureSamlMetadata.xml");

        ArrayList<DomMetadata> mdCol = new ArrayList<DomMetadata>();
        mdCol.add(new DomMetadata(testInput));

        Certificate signingCert = CryptReader.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/signingCert.pem"));

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertTrue(mdCol.isEmpty());
    }

    /**
     * Test that metadata elements that do not contain signature are appropriately filtered out when valid signatures
     * are required.
     * 
     * @throws Exception thrown if there is a problem checking signatures
     */
    @Test
    public void testRequiredSignature() throws Exception {
        Element testInput = readXmlData("samlMetadata/entitiesDescriptor2.xml");

        ArrayList<DomMetadata> mdCol = new ArrayList<DomMetadata>();
        mdCol.add(new DomMetadata(testInput));

        Certificate signingCert = CryptReader.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/signingCert.pem"));

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setSignatureRequired(false);
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomMetadata result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XMLSignatureValidationStage.class, "test");

        mdCol = new ArrayList<DomMetadata>();
        stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setSignatureRequired(true);
        stage.setVerificationCertificate(signingCert);
        stage.initialize();
        stage.execute(mdCol);
        Assert.assertTrue(mdCol.isEmpty());
    }
}
