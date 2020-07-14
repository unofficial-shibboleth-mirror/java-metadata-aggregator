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


package net.shibboleth.metadata.validate.x509;

import java.math.BigInteger;
import java.security.cert.X509Certificate;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class X509RSAExponentValidatorTest extends BaseX509ValidatorTest {
    
    public X509RSAExponentValidatorTest() throws Exception {
        super(X509RSAExponentValidator.class);
    }

    private void testCert(final String certName,
            final Validator<X509Certificate> val,
            final int expectedErrors, final int expectedWarnings) throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate(certName);
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, expectedErrors, expectedWarnings);
    }

    private void testThreeCerts(final Validator<X509Certificate> val,
            final int expectedErrors3, final int expectedWarnings3,
            final int expectedErrors35, final int expectedWarnings35,
            final int expectedErrors65537, final int expectedWarnings65537) throws Exception {
        testCert("3.pem", val, expectedErrors3, expectedWarnings3); // exponent == 3
        testCert("35.pem", val, expectedErrors35, expectedWarnings35); // exponent == 35
        testCert("65537.pem", val, expectedErrors65537, expectedWarnings65537); // exponent == 65537
    }
    
    @Test
    public void testDefaults() throws Exception {
        final X509RSAExponentValidator val = new X509RSAExponentValidator();
        val.setId("test");
        val.initialize();
        testThreeCerts(val, 1, 0, 0, 0, 0, 0);
    }

    @Test
    public void testNISTWarning() throws Exception {
        final X509RSAExponentValidator val = new X509RSAExponentValidator();
        val.setWarningBoundary(65537);
        val.setId("test");
        val.initialize();
        testThreeCerts(val, 1, 0, 0, 1, 0, 0);
    }

    @Test
    public void testNISTError() throws Exception {
        final X509RSAExponentValidator val = new X509RSAExponentValidator();
        val.setErrorBoundary(65537);
        val.setId("test");
        val.initialize();
        testThreeCerts(val, 1, 0, 1, 0, 0, 0);
    }

    @Test
    public void testWarningOnly() throws Exception {
        final X509RSAExponentValidator val = new X509RSAExponentValidator();
        val.setErrorBoundary(0);
        val.setWarningBoundary(65537);
        val.setId("test");
        val.initialize();
        testThreeCerts(val, 0, 1, 0, 1, 0, 0);
    }

    @Test
    public void mda198() throws Exception {
        final X509RSAExponentValidator val = new X509RSAExponentValidator();
        // do not initialize
        Assert.assertNull(val.getId(), "unset ID should be null");
    }
    
    public void testErrorBoundaryLongZero() throws Exception {
        final var stage = new X509RSAExponentValidator();
        stage.setErrorBoundary(0L);
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testErrorBoundaryLongNegative() throws Exception {
        final var stage = new X509RSAExponentValidator();
        stage.setErrorBoundary(-1L);
    }

    @Test
    public void testWarningBoundaryLongZero() throws Exception {
        final var stage = new X509RSAExponentValidator();
        stage.setWarningBoundary(0L);
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testWarningBoundaryLongNegative() throws Exception {
        final var stage = new X509RSAExponentValidator();
        stage.setWarningBoundary(-1L);
    }

    public void testErrorBoundaryBigZero() throws Exception {
        final var stage = new X509RSAExponentValidator();
        stage.setErrorBoundary(BigInteger.ZERO);
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testErrorBoundaryBigNegative() throws Exception {
        final var stage = new X509RSAExponentValidator();
        stage.setErrorBoundary(BigInteger.valueOf(-1));
    }

    @Test
    public void testWarningBoundaryBigZero() throws Exception {
        final var stage = new X509RSAExponentValidator();
        stage.setWarningBoundary(BigInteger.ZERO);
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void testWarningBoundaryBigNegative() throws Exception {
        final var stage = new X509RSAExponentValidator();
        stage.setWarningBoundary(BigInteger.valueOf(-1));
    }

}
