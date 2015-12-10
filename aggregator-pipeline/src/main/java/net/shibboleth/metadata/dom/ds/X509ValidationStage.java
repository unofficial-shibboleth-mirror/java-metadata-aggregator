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

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.dom.AbstractDOMValidationStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.w3c.dom.Element;

/**
 * Stage to apply a collection of validators to each X.509 certificate in items.
 * 
 * Each X.509 certificate is processed only once per item, so that duplicate status messages are suppressed.
 */ 
public class X509ValidationStage extends AbstractDOMValidationStage<X509Certificate> {

    /** Certificate factory to use to convert to X.509 certificates. */
    private CertificateFactory factory;
    
    @Override
    protected boolean applicable(@Nonnull final Element e) {
        return XMLDSIGSupport.XML_DSIG_NS.equals(e.getNamespaceURI()) &&
                "X509Certificate".equals(e.getLocalName());
    }

    @Override
    protected void visit(@Nonnull final Element element, @Nonnull final TraversalContext context) 
        throws StageProcessingException {
        final String text = element.getTextContent();
        final byte[] data = Base64Support.decode(text);
        try {
            final X509Certificate cert =
                    (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(data));
            // only process each certificate once per item
            if (!context.getStash().containsValue(cert)) {
                context.getStash().put(cert);
                applyValidators(cert, context);
            }
        } catch (CertificateException e) {
            context.getItem().getItemMetadata().put(new ErrorStatus(getId(), "could not convert X509Certificate data"));
        }
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        try {
            factory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new ComponentInitializationException("can't create X.509 certificate factory", e);
        }
    }

    @Override
    protected void doDestroy() {
        factory = null;
        super.doDestroy();
    }

}
