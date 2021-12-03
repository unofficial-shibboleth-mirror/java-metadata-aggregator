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

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

public class X509RSAOpenSSLBlacklistValidatorTest extends BaseX509ValidatorTest {
    
    public X509RSAOpenSSLBlacklistValidatorTest() throws Exception {
        super(X509RSAOpenSSLBlacklistValidator.class);
    }

    @Test
    public void testNotBlacklisted() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.setId("test");
        val.initialize();
        Assert.assertEquals(val.getKeySize(), 0); // no key size restriction
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("ok.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 0, 0);
        val.destroy();
    }

    @Test
    public void test1024on1024noRestriction() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.setId("test");
        val.initialize();
        Assert.assertEquals(val.getKeySize(), 0); // no key size restriction
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("1024.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 1, 0);
        val.destroy();
    }

    @Test
    public void test1024on1024Restricted() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.setKeySize(1024);
        val.setId("test");
        val.initialize();
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("1024.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 1, 0);
        val.destroy();
    }

    @Test
    public void test1024on1024Restricted2() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.setKeySize(2048); // untrue, but should prevent any matches
        val.setId("test");
        val.initialize();
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("1024.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 0, 0);
        val.destroy();
    }

    @Test
    public void test2048on1024noRestriction() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.setId("test");
        val.initialize();
        Assert.assertEquals(val.getKeySize(), 0); // no key size restriction
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("2048.pem");
        val.validate(cert, item, "stage");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        val.destroy();
    }

    @Test
    public void test2048on2048noRestriction() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("2048.txt"));
        val.setId("test");
        val.initialize();
        Assert.assertEquals(val.getKeySize(), 0); // no key size restriction
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("2048.pem");
        val.validate(cert, item, "stage");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        val.destroy();
    }

    @Test
    public void test2048on2048Restricted() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("2048.txt"));
        val.setKeySize(2048);
        val.setId("test");
        val.initialize();
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("2048.pem");
        val.validate(cert, item, "stage");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        val.destroy();
    }

    @Test
    public void test2048on2048Restricted2() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("2048.txt"));
        val.setKeySize(1024); // untrue, but should prevent any matches
        val.setId("test");
        val.initialize();
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("2048.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 0, 0);
        val.destroy();
    }

    @Test
    public void testBlankLineIssue9() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("issue9.txt"));
        val.setId("test");
        val.initialize();
        val.destroy();
    }

    @Test
    public void mda198() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        // do not initialize
        Assert.assertNull(val.getId(), "unset ID should be null");
    }

    @Test
    public void mda219() throws Exception {
        final var val = new X509RSAOpenSSLBlacklistValidator();
        final var resource = getClasspathResource("does-not-exist.txt");
        val.setId("test");
        val.setBlacklistResource(resource);
        try {
            val.initialize();
            Assert.fail("expected exception");
        } catch (final ComponentInitializationException e) {
            // After MDA-219, we expect to see a cause which is an IOException.
            final var cause = e.getCause();
            // System.out.println("Message: " + e.getMessage());
            // System.out.println("Cause: " + cause);
            Assert.assertNotNull(cause, "exception had no cause");
            Assert.assertTrue(cause instanceof IOException, "cause should have been an IOException");
        }
    }
    
    @Test
    public void testGetResource() throws Exception {
        final var val = new X509RSAOpenSSLBlacklistValidator();
        val.setId("test");
        Assert.assertNull(val.getBlacklistResource());
        final var resource = getClasspathResource("1024.txt");
        Assert.assertNotNull(resource);
        val.setBlacklistResource(resource);
        Assert.assertSame(val.getBlacklistResource(), resource);
        val.initialize();
        val.destroy();
    }

    @Test(expectedExceptions = ComponentInitializationException.class)
    public void testNoResource() throws Exception {
        final var val = new X509RSAOpenSSLBlacklistValidator();
        val.setId("test");
        val.initialize();
    }

}
