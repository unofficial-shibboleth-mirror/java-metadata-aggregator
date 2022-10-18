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


package net.shibboleth.metadata.dom.ds;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.x509.X509RSAKeyLengthValidator;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class X509ValidationStageTest extends BaseDOMTest {

    public X509ValidationStageTest() throws Exception {
        super(X509ValidationStage.class);
    }
    
    private X509ValidationStage makeStage() throws ComponentInitializationException {
        final X509ValidationStage stage = new X509ValidationStage();
        stage.setId("test");
        return stage; 
    }
    
    private DOMElementItem makeItem(final String which) throws XMLParserException {
        final Element doc = readXMLData(classRelativeResource(which));
        return new DOMElementItem(doc);
    }
    
    private void errorsAndWarnings(final Item<Element> item,
            final int expectedErrors, final int expectedWarnings) {
        final Collection<ErrorStatus> errors = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), expectedErrors);
        final Collection<WarningStatus> warnings = item.getItemMetadata().get(WarningStatus.class);
        Assert.assertEquals(warnings.size(), expectedWarnings);
        //for (ErrorStatus err: errors) {
        //    System.out.println("Error: " + err.getComponentId() + ": " + err.getStatusMessage());
        //}
        //for (WarningStatus warn: warnings) {
        //    System.out.println("Warning: " + warn.getComponentId() + ": " + warn.getStatusMessage());
        //}
    }

    @Test
    public void testNothing() throws Exception {
        final DOMElementItem item = makeItem("in.xml");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final X509ValidationStage stage = makeStage();
        // not setting any validators to run
        stage.initialize();
        
        stage.execute(items);
        stage.destroy();

        errorsAndWarnings(item, 0, 0);
    }
    
    @Test
    public void testError() throws Exception {
        final DOMElementItem item = makeItem("in.xml");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final X509RSAKeyLengthValidator val =
                new X509RSAKeyLengthValidator();
        val.setErrorBoundary(2049);
        val.setId("test");
        val.initialize();
        
        final List<Validator<X509Certificate>> vals = new ArrayList<>();
        vals.add(val);
        
        final X509ValidationStage stage = makeStage();
        stage.setValidators(vals);
        stage.initialize();
        
        stage.execute(items);
        stage.destroy();

        errorsAndWarnings(item, 1, 0);
    }
    
    @Test
    public void testWarning() throws Exception {
        final DOMElementItem item = makeItem("in.xml");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final X509RSAKeyLengthValidator val =
                new X509RSAKeyLengthValidator();
        val.setWarningBoundary(2049);
        val.setErrorBoundary(2048);
        val.setId("test");
        val.initialize();
        
        final List<Validator<X509Certificate>> vals = new ArrayList<>();
        vals.add(val);
        
        final X509ValidationStage stage = makeStage();
        stage.setValidators(vals);
        stage.initialize();
        
        stage.execute(items);
        stage.destroy();
        
        errorsAndWarnings(item, 0, 1);
    }

    @Test
    public void badCertificateNullIssuerMDA270() throws Exception {
        final DOMElementItem item = makeItem("mda270.xml");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final X509ValidationStage stage = makeStage();
        stage.initialize();
        
        stage.execute(items);
        stage.destroy();
        
        // We expect two errors, one for each occurrence of the certificate
        errorsAndWarnings(item, 2, 0);
        
        // Peek at the first of those
        var error = item.getItemMetadata().get(ErrorStatus.class).get(0);
        var message = error.getStatusMessage();
        
        // We do NOT want to see the generic message, but the specific one
        // thrown by CertificateFactory. Allow some latitude in verifying this.
        Assert.assertTrue(message.toLowerCase().contains("empty issuer dn"));
    }
}
