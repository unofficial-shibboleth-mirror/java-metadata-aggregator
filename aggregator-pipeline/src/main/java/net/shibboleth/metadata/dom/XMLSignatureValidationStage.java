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

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.dom.XMLSignatureValidator.ValidationException;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.apache.xml.security.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableSet;

/**
 * A pipeline stage which validates the XML digital signature found on DOM Elements.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>verificationKey</code></li>
 * </ul>
 * 
 * If Element signatures are required, per {@link #signatureRequired}, and an Element does not contain a signature than
 * an {@link ErrorStatus} object is set on the Element.
 * 
 * If Element signatures are required to be valid, per {@link #isValidSignatureRequired()}, and an Element signature is
 * found to be invalid than an {@link ErrorStatus} object is set on the element. If signatures are not required to be
 * valid and an Element signature is found to be invalid than an {@link WarningStatus} is set on the Element.
 */
@ThreadSafe
public class XMLSignatureValidationStage extends BaseIteratingStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XMLSignatureValidationStage.class);

    /** Whether Elements are required to be signed. */
    private boolean signatureRequired = true;

    /** Whether the signature on a Elements is required to be valid. Default value: <code>true</code> */
    private boolean validSignatureRequired = true;

    /** Certificate whose public key is used to verify the Element signature. */
    private Certificate verificationCertificate;

    /** Public key used to verify the Element signature. */
    private PublicKey verificationKey;
    
    /** Set of blacklisted digest URIs. Default value: empty set. */
    @Nonnull private Set<String> blacklistedDigests = Collections.emptySet();
    
    /** Set of blacklisted signature method URIs. Default value: empty set. */
    @Nonnull private Set<String> blacklistedSignatureMethods = Collections.emptySet();
    
    /** Option to determine whether empty references are to be permitted.  Default value: <code>true</code>. */
    private boolean permittingEmptyReferences = true;
    
    /** Validator used for all signatures validated by this stage instance. */
    private XMLSignatureValidator validator;

    /**
     * Gets whether the Element is required to be signed.
     * 
     * @return whether the Element is required to be signed
     */
    public boolean isSignatureRequired() {
        return signatureRequired;
    }

    /**
     * Sets whether the Element is required to be signed.
     * 
     * @param required whether the Element is required to be signed
     */
    public synchronized void setSignatureRequired(final boolean required) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        signatureRequired = required;
    }

    /**
     * Gets whether the signature on a Element element is required to be valid.
     * 
     * @return whether the signature on a Element element is required to be valid
     */
    public boolean isValidSignatureRequired() {
        return validSignatureRequired;
    }

    /**
     * Sets whether the signature on a Element element is required to be valid.
     * 
     * @param isRequired whether the signature on a Element element is required to be valid
     */
    public synchronized void setValidSignatureRequired(final boolean isRequired) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        validSignatureRequired = isRequired;
    }

    /**
     * Gets the key used to verify the signature.
     * 
     * @return key used to verify the signature
     */
    @Nullable public PublicKey getVerificationKey() {
        return verificationKey;
    }

    /**
     * Sets the key used to verify the signature.
     * 
     * @param key key used to verify the signature
     */
    public synchronized void setVerificationKey(@Nonnull final PublicKey key) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        verificationKey = Constraint.isNotNull(key, "Public key can not be null");
    }

    /**
     * Gets the certificate whose public key is used to verify the signed Element.
     * 
     * @return certificate whose public key is used to verify the signed Element
     */
    @Nullable public Certificate getVerificationCertificate() {
        return verificationCertificate;
    }

    /**
     * Set the key, included in a certificate, used to verify the signature. This method will also set
     * {@link #verificationKey} with the public key of the certificate.
     * 
     * @param certificate certificate containing the key used to verify the signature
     */
    public synchronized void setVerificationCertificate(@Nonnull final Certificate certificate) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        verificationCertificate = Constraint.isNotNull(certificate, "Certificate can not be null");
        verificationKey = verificationCertificate.getPublicKey();
    }
    
    /**
     * Set the collection of identifiers to be blacklisted as digest algorithms.
     * 
     * @param identifiers collection of identifiers to be blacklisted
     */
    public void setBlacklistedDigests(@Nonnull @NonnullElements final Collection<String> identifiers) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        blacklistedDigests = ImmutableSet.copyOf(Constraint.isNotNull(identifiers,
                "identifier collection may not be null"));
    }
    
    /**
     * Gets the set of blacklisted digest algorithm identifiers.
     * 
     * @return the set of blacklisted digest algorithm identifiers
     */
    @Nonnull @NonnullElements public Set<String> getBlacklistedDigests() {
        return Collections.unmodifiableSet(blacklistedDigests);
    }

    /**
     * Set the collection of identifiers to be blacklisted as signature methods.
     * 
     * @param identifiers collection of identifiers to be blacklisted
     */
    public void setBlacklistedSignatureMethods(@Nonnull @NonnullElements final Collection<String> identifiers) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        blacklistedSignatureMethods = ImmutableSet.copyOf(Constraint.isNotNull(identifiers,
                "identifier collection may not be null"));
    }
    
    /**
     * Gets the set of blacklisted signature method identifiers.
     * 
     * @return the set of blacklisted signature method identifiers
     */
    @Nonnull @NonnullElements public Set<String> getBlacklistedSignatureMethods() {
        return Collections.unmodifiableSet(blacklistedSignatureMethods);
    }

    /**
     * Gets whether empty references are permitted.
     * 
     * @return whether empty references are permitted
     */
    public boolean isPermittingEmptyReferences() {
        return permittingEmptyReferences;
    }

    /**
     * Sets whether empty references are permitted.
     * 
     * @param permit whether empty references are permitted
     */
    public synchronized void setPermittingEmptyReferences(final boolean permit) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        permittingEmptyReferences = permit;
    }
    
    /** {@inheritDoc} */
    @Override protected boolean doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        
        final Element docElement = item.unwrap();
        
        // Step 1: locate the signature element within the document.
        final Element signatureElement;
        try {
            signatureElement = validator.getSignatureElement(docElement);
            if (signatureElement == null) {
                if (signatureRequired) {
                    log.debug("DOM Element was not signed and signature is required");
                    item.getItemMetadata().put(
                            new ErrorStatus(getId(), "DOM Element was not signed but signatures are required"));
                } else {
                    log.debug("DOM Element is not signed, no verification performed");
                }
                return true;
            }
        } catch (final ValidationException e) {
            // pass on an error from signature location (e.g., multiple signatures)
            log.debug("setting status: ", e.getMessage());
            item.getItemMetadata().put(new ErrorStatus(getId(), e.getMessage()));
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("DOM Element contained Signature element\n{}", SerializeSupport.prettyPrintXML(signatureElement));
        }

        try {
            validator.verifySignature(docElement, signatureElement);
        } catch (final ValidationException e) {
            final String message = "element signature is invalid: " + e.getMessage();
            log.debug("setting status: ", message);
            if (validSignatureRequired) {
                item.getItemMetadata().put(new ErrorStatus(getId(), message));
            } else {
                item.getItemMetadata().put(new WarningStatus(getId(), message));
            }            
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        verificationCertificate = null;
        verificationKey = null;
        validator = null;
        blacklistedDigests = null;
        blacklistedSignatureMethods = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (verificationKey == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", no verification key was specified");
        }

        validator = new XMLSignatureValidator(verificationKey,
                blacklistedDigests, blacklistedSignatureMethods, permittingEmptyReferences);

        if (!Init.isInitialized()) {
            Init.init();
        }
    }
}