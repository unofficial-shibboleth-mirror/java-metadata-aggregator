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

import java.security.cert.X509Certificate;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class X509RSAKeyLengthValidatorTest extends BaseX509ValidatorTest {
    
    /** Constructor sets class under test. */
    public X509RSAKeyLengthValidatorTest() throws Exception {
        super(X509RSAKeyLengthValidator.class);
    }

    @Test
    public void testDefaults2048() throws Exception {
        final Item<String> item = new MockItem("foo");
        final Validator<X509Certificate> val = new X509RSAKeyLengthValidator();
        val.setId("test");
        val.initialize();

        final X509Certificate cert = getCertificate("2048.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 0, 0);
    }

    @Test
    public void testDefaults1024() throws Exception {
        final Item<String> item = new MockItem("foo");
        final Validator<X509Certificate> val = new X509RSAKeyLengthValidator();
        val.setId("test");
        val.initialize();

        final X509Certificate cert = getCertificate("1024.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 1, 0);
    }

    @Test
    public void testWarningOn1024() throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509RSAKeyLengthValidator val = new X509RSAKeyLengthValidator();
        val.setErrorBoundary(1024);
        val.setWarningBoundary(2048);
        val.setId("test");
        val.initialize();

        final X509Certificate cert = getCertificate("1024.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 0, 1);
    }

    @Test
    public void mda198() throws Exception {
        final X509RSAKeyLengthValidator val = new X509RSAKeyLengthValidator();
        // do not initialize
        Assert.assertNull(val.getId(), "unset ID should be null");
    }

}
