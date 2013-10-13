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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.signature.reference.ReferenceData;
import org.apache.xml.security.signature.reference.ReferenceSubTreeData;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.common.base.Strings;

/**
 * A class that encapsulates the various stages in validation of XML signatures as methods.
 * 
 * Failures, and the reason for the failure, are represented by exceptions.
 * 
 * This code largely derives from XmlSecTool V1.2, with the abrupt program termination
 * method of reporting issues replaced by throwing an exception. The intention is
 * that this code might be reintegrated with XmlSecTool in a later release.
 */
final class XMLSignatureValidator {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XMLSignatureValidator.class);

    /** Public key used to verify signatures. */
    private final PublicKey verificationKey;

    /** Set of blacklisted digest algorithms. */
    private final Set<String> blacklistedDigests;
    
    /** Set of blacklisted signature methods. */
    private final Set<String> blacklistedSignatureMethods;
    
    /** Whether an empty reference is permitted. */
    private final boolean emptyReferencePermitted;
    
    /**
     *  Constructor.
     *  
     *  @param key public key with which to verify signatures
     *  @param blacklistDigests set of blacklisted digest algorithm URIs, or <code>null</code>
     *  @param blacklistSignatureMethods set of blacklisted signature method URIs, or <code>null</code>
     *  @param emptyRefPermitted true if empty references are permitted
     */
    XMLSignatureValidator(@Nonnull final PublicKey key, @Nullable final Set<String> blacklistDigests,
            @Nullable final Set<String> blacklistSignatureMethods,
            final boolean emptyRefPermitted) {
        Constraint.isNotNull(key, "public key can not be null");
        verificationKey = key;
        
        if (blacklistDigests != null) {
            blacklistedDigests = new HashSet<String>(blacklistDigests);
        } else {
            blacklistedDigests = Collections.emptySet();
        }
        
        if (blacklistSignatureMethods != null) {
            blacklistedSignatureMethods = new HashSet<String>(blacklistSignatureMethods);
        } else {
            blacklistedSignatureMethods = Collections.emptySet();
        }
        
        emptyReferencePermitted = emptyRefPermitted;
    }
    
    /**
     * Exception class representing a failure to validate.
     */
    public static class ValidationException extends Exception {

        /** Serial version UID. */
        private static final long serialVersionUID = -6649552572123849961L;

        /** Constructor. */
        public ValidationException() {

        }

        /**
         * Constructor.
         * 
         * @param message exception message
         */
        public ValidationException(@Nullable final String message) {
            super(message);
        }

        /**
         * Constructor.
         * 
         * @param wrappedException exception to be wrapped by this one
         */
        public ValidationException(@Nullable final Exception wrappedException) {
            super(wrappedException);
        }

        /**
         * Constructor.
         * 
         * @param message exception message
         * @param wrappedException exception to be wrapped by this one
         */
        public ValidationException(@Nullable final String message, @Nullable final Exception wrappedException) {
            super(message, wrappedException);
        }
        
    }
    

    /**
     * Reconcile the given reference with the document element, by making sure that
     * the appropriate attribute is marked as an ID attribute.
     * 
     * @param docElement document element whose appropriate attribute should be marked
     * @param reference reference which references the document element
     * @throws ValidationException if the reference is neither empty nor to a fragment
     */
    private void markIdAttribute(@Nonnull final Element docElement, @Nonnull final Reference reference)
            throws ValidationException {
        
        assert docElement != null;
        assert reference != null;
        
        final String referenceURI = reference.getURI();
        
        /*
         * If the reference is empty, it implicitly references the document element
         * and no attribute is being referenced.
         */
        if (Strings.isNullOrEmpty(referenceURI)) {
            log.debug("reference was empty; no ID marking required");
            return;
        }
        
        /*
         * If something has already identified an ID element, don't interfere
         */
        if (AttributeSupport.getIdAttribute(docElement) != null ) {
            log.debug("document element already has an ID attribute");
            return;
        }

        /*
         * The reference must be a fragment reference, from which we extract the
         * ID value.
         */
        if (!referenceURI.startsWith("#")) {
            throw new ValidationException("Signature Reference URI was not a document fragment reference: " +
                    referenceURI);
        }
        final String id = referenceURI.substring(1);

        /*
         * Now look for the attribute which holds the ID value, and mark it as the ID attribute.
         */
        NamedNodeMap attributes = docElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attribute = (Attr) attributes.item(i);
            if (id.equals(attribute.getValue())) {
                log.debug("marking ID attribute {}", attribute.getName());
                docElement.setIdAttributeNode(attribute, true);
                return;
            }
        }
        
        /*
         * No attribute on the document element has the referenced ID value.
         * Signature validation will fail later, but let's give a warning here
         * as well to help people debug their signature code.
         */
        log.debug("did not find a document element attribute with value '{}'", id);
    }

    /**
     * Verifies that the signature on a document is valid.
     * 
     * @param docElement document element whose signature will be validated
     * @param signatureElement element containing the signature to be validated
     * @throws ValidationException if any of a number of invalid conditions are detected
     */
    public void verifySignature(@Nonnull final Element docElement, @Nonnull final Element signatureElement)
            throws ValidationException {
        
        assert docElement != null;
        assert signatureElement != null;
        
        log.debug("Creating XML security library XMLSignature object");
        XMLSignature signature = null;
        try {
            signature = new XMLSignature(signatureElement, "");
        } catch (XMLSecurityException e) {
            throw new ValidationException("Unable to read XML signature", e);
        }

        if (signature.getObjectLength() != 0) {
            throw new ValidationException("Signature contained an Object element, this is not allowed");
        }

        final Reference ref = extractReference(signature);
        markIdAttribute(docElement, ref);
        
        // check reference digest algorithm against blacklist
        try {
            String alg = ref.getMessageDigestAlgorithm().getAlgorithmURI();
            log.debug("blacklist checking digest {}", alg);
            if (blacklistedDigests.contains(alg)) {
                log.error("Digest algorithm {} is blacklisted", alg);
                throw new ValidationException("Digest algorithm " + alg + " is blacklisted");
            }
        } catch (XMLSignatureException e) {
            throw new ValidationException("unable to retrieve signature digest algorithm");
        }
        
        // check signature algorithm against blacklist
        String alg = signature.getSignedInfo().getSignatureMethodURI();
        log.debug("blacklist checking signature method {}", alg);
        if (blacklistedSignatureMethods.contains(alg)) {
            throw new ValidationException("Signature algorithm " + alg + " is blacklisted");
        }        

        log.debug("Verifying XML signature with key\n{}",
                Base64Support.encode(verificationKey.getEncoded(), false));
        
        try {
            if (signature.checkSignatureValue(verificationKey)) {
                /*
                 * Now that the signature has been verified, we need to check that the
                 * XML signature layer resolved the reference to the correct element
                 * (always the document element) and that only appropriate transforms have
                 * been applied.
                 * 
                 * Note that we need to re-extract the reference from the signature at
                 * this point, we can't use one from before the signature validation.
                 */
                validateSignatureReference(docElement, extractReference(signature));
                log.debug("XML document signature verified.");
            } else {
                throw new ValidationException("XML document signature verification failed");
            }
        } catch (XMLSignatureException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to validate signature", e);
            }
            throw new ValidationException("XML document signature verification failed with an error: " +
                    e.getMessage());
        }
    }

    /**
     * Extract the reference within the provided XML signature while ensuring that there
     * is only one such reference, and that (if appropriate) it is not empty.
     * 
     * @param signature signature to extract the reference from
     * @return the extracted reference
     * @throws ValidationException if a reference could not be extracted
     */
    private Reference extractReference(@Nonnull final XMLSignature signature) throws ValidationException {
        int numReferences = signature.getSignedInfo().getLength();
        if (numReferences != 1) {
            throw new ValidationException("Signature SignedInfo had invalid number of References: " + numReferences);
        }

        try {
            final Reference ref = signature.getSignedInfo().item(0);
            if (ref == null) {
                throw new ValidationException("Signature Reference was null");
            }
            if (!emptyReferencePermitted) {
                if (Strings.isNullOrEmpty(ref.getURI())) {
                    throw new ValidationException("empty references are not permitted");
                }
            }
            return ref;
        } catch (XMLSecurityException e) {
            throw new ValidationException("Apache XML Security exception obtaining Reference: " + e.getMessage());
        }
    }

    /**
     * Validates the reference within the XML signature by performing the following checks.
     * <ul>
     * <li>check that the XML signature layer resolves that reference to the same element as the DOM layer does</li>
     * <li>check that only enveloped and, optionally, exclusive canonicalization transforms are used</li>
     * </ul>
     * 
     * @param docElement document element
     * @param ref reference to be verified
     * @throws ValidationException if any of the checks fail
     */
    private void validateSignatureReference(@Nonnull final Element docElement, @Nonnull final Reference ref)
            throws ValidationException {
        validateSignatureReferenceUri(docElement, ref);
        validateSignatureTransforms(ref);
    }

    /**
     * Validates that the element resolved by the signature validation layer is the same as the
     * element resolved by the DOM layer.
     * 
     * @param expectedSignedNode the node expected as the result of the reference
     * @param reference the reference to be validated
     * @throws ValidationException if validation fails
     */
    private void validateSignatureReferenceUri(@Nonnull final Element expectedSignedNode,
            @Nonnull final Reference reference) throws ValidationException {
        final ReferenceData refData = reference.getReferenceData();
        if (refData instanceof ReferenceSubTreeData) {
            final ReferenceSubTreeData subTree = (ReferenceSubTreeData) refData;
            final Node root = subTree.getRoot();
            Node resolvedSignedNode = root;
            if (root.getNodeType() == Node.DOCUMENT_NODE) {
                resolvedSignedNode = ((Document)root).getDocumentElement();
            }

            if (!expectedSignedNode.isSameNode(resolvedSignedNode)) {
                throw new ValidationException("Signature Reference URI \"" + reference.getURI()
                        + "\" was resolved to a node other than the document element");
            }
        } else {
            throw new ValidationException("Signature Reference URI did not resolve to a subtree");
        }
    }

    /**
     * Validate the transforms included in the Signature Reference.
     * 
     * The Reference may contain at most 2 transforms. One of them must be the Enveloped signature transform. An
     * Exclusive Canonicalization transform (with or without comments) may also be present. No other transforms are
     * allowed.
     * 
     * @param reference the Signature reference containing the transforms to evaluate
     * @throws ValidationException if the transforms are incorrect
     */
    private void validateSignatureTransforms(@Nonnull final Reference reference) throws ValidationException {
        Transforms transforms = null;
        try {
            transforms = reference.getTransforms();
        } catch (XMLSecurityException e) {
            throw new ValidationException("Apache XML Security error obtaining Transforms instance: " + e.getMessage());
        }

        if (transforms == null) {
            throw new ValidationException("Error obtaining Transforms instance, null was returned");
        }

        int numTransforms = transforms.getLength();
        if (numTransforms > 2) {
            throw new ValidationException("Invalid number of Transforms was present: " + numTransforms);
        }

        boolean sawEnveloped = false;
        for (int i = 0; i < numTransforms; i++) {
            Transform transform = null;
            try {
                transform = transforms.item(i);
            } catch (TransformationException e) {
                throw new ValidationException("Error obtaining transform instance: " + e.getMessage());
            }
            String uri = transform.getURI();
            if (Transforms.TRANSFORM_ENVELOPED_SIGNATURE.equals(uri)) {
                log.debug("Saw Enveloped signature transform");
                sawEnveloped = true;
            } else if (Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS.equals(uri)
                    || Transforms.TRANSFORM_C14N_EXCL_WITH_COMMENTS.equals(uri)) {
                log.debug("Saw Exclusive C14N signature transform");
            } else {
                throw new ValidationException("Saw invalid signature transform: " + uri);
            }
        }

        if (!sawEnveloped) {
            throw new ValidationException("Signature was missing the required Enveloped signature transform");
        }
    }

    /**
     * Gets the signature element from the document. The signature must be a child of the document root.
     * 
     * @param docElement document element from which to pull the signature
     * 
     * @return the signature element, or null
     * @throws ValidationException if more than one signature element is present
     */
    @Nullable public Element getSignatureElement(@Nonnull final Element docElement) throws ValidationException {
        final List<Element> sigElements =
                ElementSupport.getChildElementsByTagNameNS(docElement,
                        XMLSignatureSigningStage.XML_SIG_NS_URI, "Signature");

        if (sigElements.isEmpty()) {
            return null;
        }

        if (sigElements.size() > 1) {
            throw new ValidationException("XML document contained more than one signature, unable to process");
        }

        return sigElements.get(0);
    }

}
