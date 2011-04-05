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

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.ErrorStatusInfo;
import net.shibboleth.metadata.WarningStatusInfo;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.apache.xml.security.Init;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.opensaml.util.xml.ElementSupport;
import org.opensaml.util.xml.SerializeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A pipeline stage which validates the XML digital signature found on metadata elements.
 * 
 * If metadata element signatures are required, per {@link #signatureRequired}, and an element does not contain a
 * signature than an {@link ErrorStatusInfo} object is set on the element.
 * 
 * If metadata element signatures are required to be valid, per {@link #isValidSignatureRequired()}, and an element
 * signature is found to be invalid than an {@link ErrorStatusInfo} object is set on the element. If signatures are not
 * required to be valid and an element signature is found to be invalid than an {@link WarningStatusInfo} is set on the
 * element.
 */
@ThreadSafe
public class XMLSignatureValidationStage extends BaseIteratingStage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XMLSignatureValidationStage.class);

    /** Whether metadata is required to be signed. */
    private boolean signatureRequired = true;

    /** Whether the signature on a metadata elements is required to be valid. Default value: {@value} */
    private boolean validSignatureRequired = true;

    /** Certificate whose public key is used to verify the metadata signature. */
    private Certificate verificationCertificate;

    /** Public key used to verify the metadata signature. */
    private PublicKey verificationKey;

    /**
     * Gets whether the metadata is required to be signed.
     * 
     * @return whether the metadata is required to be signed
     */
    public boolean isSignatureRequired() {
        return signatureRequired;
    }

    /**
     * Sets whether the metadata is required to be signed.
     * 
     * @param required whether the metadata is required to be signed
     */
    public synchronized void setSignatureRequired(final boolean required) {
        if (isInitialized()) {
            return;
        }
        signatureRequired = required;
    }

    /**
     * Gets whether the signature on a metadata element is required to be valid.
     * 
     * @return whether the signature on a metadata element is required to be valid
     */
    public boolean isValidSignatureRequired() {
        return validSignatureRequired;
    }

    /**
     * Sets whether the signature on a metadata element is required to be valid.
     * 
     * @param isRequired whether the signature on a metadata element is required to be valid
     */
    public synchronized void setValidSignatureRequired(boolean isRequired) {
        if (isInitialized()) {
            return;
        }
        validSignatureRequired = isRequired;
    }

    /**
     * Gets the key used to verify the signature.
     * 
     * @return key used to verify the signature
     */
    public PublicKey getVerificationKey() {
        return verificationKey;
    }

    /**
     * Sets the key used to verify the signature.
     * 
     * @param key key used to verify the signature
     */
    public synchronized void setVerificationKey(final PublicKey key) {
        if (isInitialized()) {
            return;
        }
        verificationKey = key;
    }

    /**
     * Gets the certificate whose public key is used to verify the signed metadata.
     * 
     * @return certificate whose public key is used to verify the signed metadata
     */
    public Certificate getVerificationCertificate() {
        return verificationCertificate;
    }

    /**
     * Set the key, included in a certificate, used to verify the signature. This method will also set
     * {@link #verificationKey} with the public key of the certificate.
     * 
     * @param certificate certificate containing the key used to verify the signature
     */

    public synchronized void setVerificationCertificate(final Certificate certificate) {
        if (isInitialized()) {
            return;
        }
        if (certificate != null) {
            verificationCertificate = certificate;
            verificationKey = certificate.getPublicKey();
        }
    }

    /** {@inheritDoc} */
    protected boolean doExecute(DomMetadata metadata) throws StageProcessingException {
        final Element signatureElement = getSignatureElement(metadata.getMetadata());
        if (signatureElement == null) {
            if (signatureRequired) {
                log.debug("Metadata was not signed and signature is required");
                metadata.getMetadataInfo().put(
                        new ErrorStatusInfo(getId(), "element was not signed but signatures are required"));
            } else {
                log.debug("XML document is not signed, no verification performed");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("XML document contained Signature element\n{}", SerializeSupport.prettyPrintXML(signatureElement));
        }

        if (!signatureVerified(signatureElement)) {
            if (validSignatureRequired) {
                metadata.getMetadataInfo().put(new ErrorStatusInfo(getId(), "element signature is invalid"));
            } else {
                metadata.getMetadataInfo().put(new WarningStatusInfo(getId(), "element signature is invalid"));
            }
        }

        return true;
    }

    /**
     * Verifies the enclosed signature on the root of the metadata.
     * 
     * @param signatureElement the Signature element
     * 
     * @return true if the signature is verified successfully, false otherwise
     * 
     * @throws StageProcessingException thrown if the given root element contains more than on signature
     */
    protected boolean signatureVerified(final Element signatureElement) throws StageProcessingException {
        log.debug("Creating XML security library XMLSignature object");
        final XMLSignature signature;
        try {
            signature = new XMLSignature(signatureElement, "");
        } catch (XMLSecurityException e) {
            log.debug("Unable to read XML signature", e);
            return false;
        }

        try {
            if (signature.checkSignatureValue(verificationKey)) {
                log.debug("XML document signature verified.");
                return true;
            } else {
                log.debug("XML document signature did not verify.");
                return false;
            }
        } catch (XMLSignatureException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to validate signature", e);
            }
            return false;
        }
    }

    /**
     * Gets the signature element from the document. The signature must be a child of the document root.
     * 
     * @param root root from which to start searching for the signature
     * 
     * @return the signature element, or null
     * 
     * @throws StageProcessingException thrown if there is more than one signature present
     */
    protected Element getSignatureElement(final Element root) throws StageProcessingException {
        final List<Element> sigElements = ElementSupport.getChildElementsByTagNameNS(root,
                XMLSignatureSigningStage.XML_SIG_NS_URI, "Signature");

        if (sigElements.isEmpty()) {
            return null;
        }

        if (sigElements.size() > 1) {
            throw new StageProcessingException("XML document contained more than one signature");
        }

        return sigElements.get(0);
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        if (verificationKey == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", VerificationKey must not be null");
        }

        if (!Init.isInitialized()) {
            Init.init();
        }
    }
}