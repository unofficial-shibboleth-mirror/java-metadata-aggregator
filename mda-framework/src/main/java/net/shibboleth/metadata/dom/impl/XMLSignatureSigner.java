/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.dom.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.security.auth.x500.X500Principal;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.XMLSignatureSigningStage;
import net.shibboleth.metadata.dom.ds.XMLDSIGSupport;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.shared.annotation.constraint.Live;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.StringSupport;
import net.shibboleth.shared.xml.ElementSupport;
import net.shibboleth.shared.xml.QNameSupport;

/**
 * A class that encapsulates the various stages in signing an XML document.
 * 
 * <p>
 * This implementation is not thread safe, but can be used sequentially to sign
 * a number of different documents.
 * </p>
 * 
 * <p>
 * As an adjunct to an {@link XMLSignatureSigningStage} instance, it snapshots its
 * parameters during construction so that they can be used without synchronization
 * during operation.
 * </p>
 */
@NotThreadSafe
public class XMLSignatureSigner {

    /* Fields passed from the calling class. */

    /** Logger provided by the calling class. */
    @Nonnull private final Logger log;

    /** Private key used to sign data. */
    @Nonnull private final PrivateKey privKey;

    /** Public key associated with the given private key. */
    @Nullable private final PublicKey publicKey;

    /** Inclusive prefix list used with exclusive canonicalization. */
    @Nonnull @NonnullElements @Unmodifiable
    private final List<String> inclusivePrefixList;

    /**
     * Names of attributes to treat as ID attributes for signature referencing. Default value: list containing the
     * non-namespace-qualified attributes 'ID', 'Id', 'id'
     */
    @Nonnull @NonnullElements @Unmodifiable
    private final List<QName> idAttributeNames;

    /** Explicit names to associate with the given signing key. */
    @Nonnull @NonnullElements @Unmodifiable
    private final List<String> keyNames;

    /** Certificate chain, with end entity certificate as element 0, to be included with the signature. */
    @Nonnull @NonnullElements @Unmodifiable
    private final List<X509Certificate> certificates;

    /** CRLs to be included with the signature. */
    @Nonnull @NonnullElements @Unmodifiable
    private final List<X509CRL> crls;

    /** Whether key names should be included in the signature's KeyInfo. */
    private final boolean includeKeyNames;

    /** Whether the public key should be included in the signature's KeyInfo. */
    private final boolean includeKeyValue;

    /** Whether the end-entity certificate's subject name should be included in the signature's KeyInfo. */
    private final boolean includeX509SubjectName;

    /** Whether the certificates chain should be included in the signature's KeyInfo. */
    private final boolean includeX509Certificates;

    /** Whether the CRLs should be included in the signature's KeyInfo. */
    private final boolean includeX509Crls;

    /** Whether the end-entity certificate's issuer and serial number should be included in the signature's KeyInfo. */
    private final boolean includeX509IssuerSerial;

    /** Whether to debug digest operations by logging the pre-digest data stream. */
    private final boolean debugPreDigest;

    /** Whether to remove CR characters from generated signatures. */
    private final boolean removingCRsFromSignature;

    /* Fields derived from the calling class. */

    /**
     * Canonicalization algorithm to use. This is determined from the {@code c14nExclusive} and
     * {@code c14nWithComments} properties.
     */
    @Nonnull private final String c14nAlgo;

    /** Signature algorithm used. */
    @Nonnull private final String sigAlgo;

    /** Digest algorithm used. */
    @Nonnull private final String digestAlgo;

    /* Fields created during construction. */

    /** Factory used to create XML signature objects. */
    @SuppressWarnings("null")
    private final @Nonnull XMLSignatureFactory xmlSigFactory = XMLSignatureFactory.getInstance();

    /** Factory used to create KeyInfo objects. */
    @SuppressWarnings("null")
    private final @Nonnull KeyInfoFactory keyInfoFactory = xmlSigFactory.getKeyInfoFactory();

    /**
     * Constructor.
     *
     * @param stage the {@link XMLSignatureSigningStage} we are acting as an adjunct to
     * @param logger the logger for the calling stage
     */
    public XMLSignatureSigner(@Nonnull final XMLSignatureSigningStage stage, @Nonnull final Logger logger) {
        log = logger;

        /*
         * Snapshot parameters while holding the lock on the stage, reducing the
         * cost of the synchronization on the individual getters.
         */
        synchronized (stage) {
            /* Whether to use exclusive canonicalization */
            final boolean c14nExclusive = stage.isC14nExclusive();

            /* Whether to include comments in the canonicalized data. */
            final boolean c14nWithComments = stage.isC14nWithComments();

            if (c14nExclusive) {
                if (c14nWithComments) {
                    c14nAlgo = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;
                } else {
                    c14nAlgo = CanonicalizationMethod.EXCLUSIVE;
                }
            } else {
                if (c14nWithComments) {
                    c14nAlgo = CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS;
                } else {
                    c14nAlgo = CanonicalizationMethod.INCLUSIVE;
                }
            }

            switch (stage.getSHAVariant()) {
                case SHA1:
                    sigAlgo = SignatureMethod.RSA_SHA1;
                    digestAlgo = DigestMethod.SHA1;
                    break;

                case SHA384:
                    sigAlgo = SignatureMethod.RSA_SHA384;
                    digestAlgo = DigestMethod.SHA384;
                    break;

                case SHA512:
                    sigAlgo = SignatureMethod.RSA_SHA512;
                    digestAlgo = DigestMethod.SHA512;
                    break;

                case SHA256:
                default:
                    sigAlgo = SignatureMethod.RSA_SHA256;
                    digestAlgo = DigestMethod.SHA256;
                    break;
            }

            privKey = Constraint.isNotNull(stage.getPrivateKey(), "privateKey may not be null");
            publicKey = stage.getPublicKey();
            inclusivePrefixList = stage.getInclusivePrefixList();
            idAttributeNames = stage.getIdAttributeNames();
            keyNames = stage.getKeyNames();
            certificates = stage.getCertificates();
            crls = stage.getCrls();
            includeKeyNames = stage.isIncludeKeyNames();
            includeKeyValue = stage.isIncludeKeyValue();
            includeX509SubjectName = stage.isIncludeX509SubjectName();
            includeX509Certificates = stage.isIncludeX509Certificates();
            includeX509Crls = stage.isIncludeX509Crls();
            includeX509IssuerSerial = stage.isIncludeX509IssuerSerial();
            debugPreDigest = stage.isDebugPreDigest() && log.isDebugEnabled();
            removingCRsFromSignature = stage.isRemovingCRsFromSignature();
        }

    }
    
    /**
     * Sign an individual item.
     * 
     * @param item the {@link Item} to sign
     * @throws StageProcessingException if an error occurs preventing the signature from being made
     */
    public void sign(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element element = item.unwrap();
        final var signature = xmlSigFactory.newXMLSignature(buildSignedInfo(element), buildKeyInfo());
        
        final XMLSignContext context = new DOMSignContext(privKey, element, element.getFirstChild());

        // Enable caching reference values if required for debugging.
        if (debugPreDigest) {
            context.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        }

        // Perform the signature operation
        try {
            signature.sign(context);
        } catch (final Exception e) {
            throw new StageProcessingException("Unable to create signature for element", e);
        }

        // Log the pre-digest data for debugging
        try {
            if (debugPreDigest) {
                final Reference ref = signature.getSignedInfo().getReferences().get(0);
                final String preDigest = new String(ref.getDigestInputStream().readAllBytes(), StandardCharsets.UTF_8);
                log.debug("pre digest: {}", preDigest);
            }
        } catch (final IOException e) {
            throw new StageProcessingException("Unable to log pre-digest data", e);
        }

        // Remove any CRs from selected signature elements.
        if (removingCRsFromSignature) {
            final Element signatureElement = ElementSupport.getFirstChildElement(element,
                    XMLDSIGSupport.SIGNATURE_NAME);
            // Must be present, by construction
            assert signatureElement != null;
            removeCRsFromNamedChildren(signatureElement, "SignatureValue");
            removeCRsFromNamedChildren(signatureElement, "X509Certificate");
        }
    }

    /**
     * Gets the descriptor of signed content.
     * 
     * @param target the element that will be signed
     * 
     * @return signed content descriptor
     * 
     * @throws StageProcessingException thrown if there is a problem creating the signed content descriptor
     */
    @Nonnull protected SignedInfo buildSignedInfo(@Nonnull final Element target) throws StageProcessingException {
        C14NMethodParameterSpec c14nMethodSpec = null;
        if (c14nAlgo.startsWith(CanonicalizationMethod.EXCLUSIVE) && inclusivePrefixList != null
                && !inclusivePrefixList.isEmpty()) {
            c14nMethodSpec = new ExcC14NParameterSpec(inclusivePrefixList);
        }

        final CanonicalizationMethod c14nMethod;
        try {
            c14nMethod = xmlSigFactory.newCanonicalizationMethod(c14nAlgo, c14nMethodSpec);
        } catch (final Exception e) {
            final String errMsg = "Unable to create transform " + c14nAlgo;
            throw new StageProcessingException(errMsg, e);
        }

        final SignatureMethod sigMethod;
        try {
            sigMethod = xmlSigFactory.newSignatureMethod(sigAlgo, null);
        } catch (final Exception e) {
            final String errMsg = "Unable to create signature method " + sigAlgo;
            throw new StageProcessingException(errMsg, e);
        }

        final List<Reference> refs = Collections.singletonList(buildSignatureReference(target));

        final var info = xmlSigFactory.newSignedInfo(c14nMethod, sigMethod, refs);
        assert info != null;
        return info;
    }

    /**
     * Builds the references to the signed content.
     * 
     * @param target the element to be signed
     * 
     * @return reference to signed content
     * 
     * @throws StageProcessingException thrown if there is a problem creating the reference to the element
     */
    @Nonnull protected Reference buildSignatureReference(@Nonnull final Element target)
            throws StageProcessingException {
        final String id = getElementId(target);
        final String refUri;
        if (id == null) {
            refUri = "";
        } else {
            refUri = "#" + id;
        }

        DigestMethod digestMethod = null;
        try {
            final DigestMethodParameterSpec digestMethodSpec = null;
            digestMethod = xmlSigFactory.newDigestMethod(digestAlgo, digestMethodSpec);
        } catch (final Exception e) {
            final String errMsg = "Unable to create digest method " + digestAlgo;
            throw new StageProcessingException(errMsg, e);
        }

        TransformParameterSpec transformSpec;
        final ArrayList<Transform> transforms = new ArrayList<>();

        try {
            transformSpec = null;
            transforms.add(xmlSigFactory.newTransform(Transform.ENVELOPED, transformSpec));
        } catch (final Exception e) {
            final String errMsg = "Unable to create transform " + Transform.ENVELOPED;
            throw new StageProcessingException(errMsg, e);
        }

        try {
            if (c14nAlgo.startsWith(CanonicalizationMethod.EXCLUSIVE) && inclusivePrefixList != null
                    && !inclusivePrefixList.isEmpty()) {
                transformSpec = new ExcC14NParameterSpec(inclusivePrefixList);
            }
            transforms.add(xmlSigFactory.newTransform(c14nAlgo, transformSpec));
        } catch (final Exception e) {
            final String errMsg = "Unable to create transform " + c14nAlgo;
            throw new StageProcessingException(errMsg, e);
        }

        final var ref = xmlSigFactory.newReference(refUri, digestMethod, transforms, null, null);
        assert ref != null;
        return ref;
    }

    /**
     * Determines the ID for the element to be signed. To determine the ID first, all the element attributes are
     * inspected, if one matches the provided {@link #idAttributeNames} then the value of the attribute is used as the
     * ID value. If no ID attribute names are given, or none of the given ones match, and one or more of the attributes
     * is marked as an ID attribute (i.e. {@link Attr#isId()} is true), then the value of one of those attributes is
     * used.
     * 
     * @param target an element to be referenced by the signature
     * 
     * @return the ID value for the element, or null
     */
    // Checkstyle: CyclomaticComplexity OFF
    protected @Nullable String getElementId(@Nonnull final Element target) {
        final NamedNodeMap attributes = target.getAttributes();
        if (attributes == null || attributes.getLength() < 1) {
            return null;
        }

        if (idAttributeNames != null && !idAttributeNames.isEmpty()) {
            for (int i = 0; i < attributes.getLength(); i++) {
                final Attr attribute = (Attr) attributes.item(i);
                assert attribute != null;
                if (idAttributeNames.contains(QNameSupport.getNodeQName(attribute))) {
                    // mark the attribute as an ID attribute so that it can be referenced by the signature
                    target.setIdAttributeNode(attribute, true);
                    final String value = StringSupport.trimOrNull(attribute.getValue());
                    if (value != null) {
                        return value;
                    }
                }
            }
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            final Attr attribute = (Attr) attributes.item(i);
            if (attribute.isId()) {
                final String value = StringSupport.trimOrNull(attribute.getValue());
                if (value != null) {
                    return value;
                }
            }
        }

        return null;
    }
    // Checkstyle: CyclomaticComplexity ON

    /**
     * Builds the KeyInfo element to be included in the signature.
     * 
     * @return KeyInfo element to be included in the signature
     * 
     * @throws StageProcessingException thrown if there is a problem creating the KeyInfo descriptor
     */
    @Nullable protected KeyInfo buildKeyInfo() throws StageProcessingException {
        final List<XMLStructure> keyInfoItems = new ArrayList<>();

        addKeyNames(keyInfoItems);
        addKeyValue(keyInfoItems);
        addX509Data(keyInfoItems);

        if (keyInfoItems.isEmpty()) {
            return null;
        }
        return keyInfoFactory.newKeyInfo(keyInfoItems);
    }

    /**
     * Adds key names to the KeyInfo, if key names are to be included.
     * 
     * @param keyInfoItems collector for KeyInfo children
     * 
     * @throws StageProcessingException thrown if there is a problem creating the KeyName content
     */
    protected void addKeyNames(@Nonnull @NonnullElements @Live final List<XMLStructure> keyInfoItems)
            throws StageProcessingException {
        if (!includeKeyNames) {
            return;
        }

        if (keyNames != null && !keyNames.isEmpty()) {
            for (final String name : keyNames) {
                keyInfoItems.add(keyInfoFactory.newKeyName(name));
            }
        }
    }

    /**
     * Adds raw key values to the KeyInfo if key values are to be included.
     * 
     * @param keyInfoItems collector for KeyInfo children
     * 
     * @throws StageProcessingException thrown if there is a problem creating the KeyValue content
     */
    protected void addKeyValue(@Nonnull @NonnullElements @Live final List<XMLStructure> keyInfoItems)
            throws StageProcessingException {
        if (!includeKeyValue) {
            return;
        }

        PublicKey key = publicKey;
        
        // If we have no explicit public key, we can extract one from a certificate, if we have one.
        if (key == null && !certificates.isEmpty()) {
            key = certificates.get(0).getPublicKey();
        }

        if (key != null) {
            try {
                keyInfoItems.add(keyInfoFactory.newKeyValue(key));
            } catch (final Exception e) {
                throw new StageProcessingException("Unable to create KeyValue", e);
            }
        }
    }

    /**
     * Adds X509 data (subject names, certificates, CRLs, and Issuer/Serial) set to be included, into the key info.
     * 
     * @param keyInfoItems collector for KeyInfo children
     */
    protected void addX509Data(@Nonnull @NonnullElements @Live final List<XMLStructure> keyInfoItems) {
        final List<Object> x509Data = new ArrayList<>();

        if (certificates != null && !certificates.isEmpty()) {
            final X509Certificate endEntityCert = certificates.get(0);

            if (includeX509SubjectName) {
                final X500Principal subjectDn = endEntityCert.getSubjectX500Principal();
                x509Data.add(subjectDn.getName(X500Principal.RFC2253));
            }

            if (includeX509Certificates) {
                x509Data.addAll(certificates);
            }

            if (includeX509IssuerSerial) {
                final X500Principal issuerDn = endEntityCert.getIssuerX500Principal();
                final BigInteger serialNumber = endEntityCert.getSerialNumber();
                x509Data.add(keyInfoFactory.newX509IssuerSerial(issuerDn.getName(X500Principal.RFC2253), serialNumber));
            }
        }

        if (includeX509Crls && crls != null && !crls.isEmpty()) {
            x509Data.add(crls);
        }

        if (!x509Data.isEmpty()) {
            keyInfoItems.add(keyInfoFactory.newX509Data(x509Data));
        }
    }
    
    /**
     * Remove any CRs from the text content of named child elements.
     *
     * @param signature The <code>Signature</code> element to process.
     * @param elementName The element name within the XML DSIG namespace to look for.
     */
    private void removeCRsFromNamedChildren(@Nonnull final Element signature, @Nonnull final String elementName) {
        final NodeList nodes = signature.getElementsByTagNameNS(XMLSignature.XMLNS, elementName);
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            final String text = node.getTextContent();
            if (text.indexOf('\r') >= 0) {
                node.setTextContent(text.replaceAll("\\r", ""));
            }
        }
    }

}
