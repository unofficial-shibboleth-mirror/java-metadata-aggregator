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

import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;

public class X509RSAOpenSSLBlacklistValidatorTest extends BaseX509ValidatorTest {
    
    /** Constructor sets class under test. */
    public X509RSAOpenSSLBlacklistValidatorTest() throws Exception {
        super(X509RSAOpenSSLBlacklistValidator.class);
    }

    @Test
    public void testNotBlacklisted() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.initialize();
        Assert.assertEquals(val.getKeySize(), 0); // no key size restriction
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("ok.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 0, 0);
    }

    @Test
    public void test1024on1024noRestriction() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.initialize();
        Assert.assertEquals(val.getKeySize(), 0); // no key size restriction
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("1024.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 1, 0);
    }

    @Test
    public void test1024on1024Restricted() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.setKeySize(1024);
        val.initialize();
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("1024.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 1, 0);
    }

    @Test
    public void test1024on1024Restricted2() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.setKeySize(2048); // untrue, but should prevent any matches
        val.initialize();
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("1024.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 0, 0);
    }

    @Test
    public void test2048on1024noRestriction() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("1024.txt"));
        val.initialize();
        Assert.assertEquals(val.getKeySize(), 0); // no key size restriction
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("2048.pem");
        val.validate(cert, item, "stage");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
    }

    @Test
    public void test2048on2048noRestriction() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("2048.txt"));
        val.initialize();
        Assert.assertEquals(val.getKeySize(), 0); // no key size restriction
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("2048.pem");
        val.validate(cert, item, "stage");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
    }

    @Test
    public void test2048on2048Restricted() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("2048.txt"));
        val.setKeySize(2048);
        val.initialize();
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("2048.pem");
        val.validate(cert, item, "stage");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
    }

    @Test
    public void test2048on2048Restricted2() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("2048.txt"));
        val.setKeySize(1024); // untrue, but should prevent any matches
        val.initialize();
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("2048.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 0, 0);
    }

    @Test
    public void testBlankLineIssue9() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("issue9.txt"));
        val.initialize();
    }
    
    @Test
    public void classPathResource() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(new ClassPathResource("net/shibboleth/metadata/validate/x509/debian-2048.txt"));
        val.setKeySize(2048);
        val.initialize();
        
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate("2048.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, 1, 0);
    }
    
}
