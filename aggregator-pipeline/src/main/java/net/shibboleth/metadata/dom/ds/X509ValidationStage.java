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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.crypto.dsig.XMLSignature;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.AbstractDOMValidationStage;
import net.shibboleth.metadata.dom.SimpleDOMTraversalContext;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.codec.DecodingException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Stage to apply a collection of validators to each X.509 certificate in items.
 * 
 * Each X.509 certificate is processed only once per item, so that duplicate status messages are suppressed.
 *
 * @since 0.9.0
 */ 
public class X509ValidationStage extends AbstractDOMValidationStage<X509Certificate, X509ValidationStage.Context> {

    /** Context class for this kind of traversal. */
    protected static class Context extends SimpleDOMTraversalContext {

        /**
         * Collection of certificates we have already seen.
         *
         * This is used to ensure that we only issue one error for each different certificate;
         * duplicates are ignored.
         *
         * This is achieved by using a {@link Map} from certificates to themselves and
         * detecting presence using {@link Map#containsKey}.
         */
        private Map<X509Certificate, X509Certificate> certMap = new HashMap<>();

        /**
         * Constructor.
         *
         * @param contextItem the {@link Item} this traversal is being performed on.
         */
        public Context(@Nonnull final Item<Element> contextItem) {
            super(contextItem);
        }

        /**
         * Returns whether we have seen this certificate before.
         *
         * @param cert {@link X509Certificate} to check for.
         * @return <code>true</code> if we have seen this certificate before.
         */
        protected boolean haveSeen(@Nonnull final X509Certificate cert) {
            return certMap.containsKey(cert);
        }

        /**
         * Add a certificate to the list of certificates we have seen and processed already.
         *
         * @param cert {@link X509Certificate} to add to the list of already seen certificates.
         */
        protected void add(@Nonnull final X509Certificate cert) {
            certMap.put(cert, cert);
        }

    }

    /** Certificate factory to use to convert to X.509 certificates. */
    private CertificateFactory factory;

    @Override
    protected Context buildContext(@Nonnull final Item<Element> item) {
        return new Context(item);
    }

    @Override
    protected boolean applicable(@Nonnull final Element e) {
        return XMLSignature.XMLNS.equals(e.getNamespaceURI()) &&
                "X509Certificate".equals(e.getLocalName());
    }

    @Override
    protected void visit(@Nonnull final Element element, @Nonnull final Context context) 
        throws StageProcessingException {
        final String text = element.getTextContent();        
        try {
            final byte[] data = Base64Support.decode(text);
            final X509Certificate cert =
                    (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(data));
            // only process each certificate once per item
            if (!context.haveSeen(cert)) {
                context.add(cert);
                applyValidators(cert, context);
            }
        } catch (final CertificateException | DecodingException e) {
            addError(context.getItem(), element, "could not convert X509Certficate data");
        }
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        try {
            factory = CertificateFactory.getInstance("X.509");
        } catch (final CertificateException e) {
            throw new ComponentInitializationException("can't create X.509 certificate factory", e);
        }
    }

    @Override
    protected void doDestroy() {
        factory = null;
        super.doDestroy();
    }

}
