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
import javax.annotation.concurrent.ThreadSafe;
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

import org.apache.xml.security.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.ds.XMLDSIGSupport;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import net.shibboleth.utilities.java.support.xml.QNameSupport;

/**
 * A pipeline stage that creates, and adds, an enveloped signature for each element in the given {@link DOMElementItem}
 * collection.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>privateKey</code></li>
 * </ul>
 */
@ThreadSafe
public class XMLSignatureSigningStage extends AbstractIteratingStage<Element> {

    /** The variant of SHA to use in the various signature algorithms. */
    public static enum ShaVariant {
        SHA1, SHA256, SHA384, SHA512
    };

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XMLSignatureSigningStage.class);

    /** Factory used to create XML signature objects. */
    private XMLSignatureFactory xmlSigFactory;

    /** Factory used to create KeyInfo objects. */
    private KeyInfoFactory keyInfoFactory;

    /** SHA algorithm variant used in signature and digest algorithms. Default value: <code>ShaVariant.SHA256</code> */
    private ShaVariant shaVariant = ShaVariant.SHA256;

    /** Private key used to sign data. */
    private PrivateKey privKey;

    /** Public key associated with the given private key. */
    private PublicKey publicKey;

    /**
     * Certificate chain, with end entity certificate as element 0, to be included with the signature. Default value:
     * empty list
     */
    @Nonnull @NonnullElements @Unmodifiable
    private List<X509Certificate> certificates = List.of();

    /** CRLs to be included with the signature. Default value: empty list */
    @Nonnull @NonnullElements @Unmodifiable
    private List<X509CRL> crls = List.of();

    /** Signature algorithm used. */
    private String sigAlgo;

    /** Digest algorithm used. */
    private String digestAlgo;

    /** Whether to use exclusive canonicalization. Default value: <code>true</code> */
    private boolean c14nExclusive = true;

    /** Whether to include comments in the canonicalized data. Default value: <code>false</code> */
    private boolean c14nWithComments;

    /** Whether to remove CR characters from generated signatures. Default value: <code>true</code>. */
    private boolean removingCRsFromSignature = true;

    /**
     * Canonicalization algorithm to use. This is determined from the {@link #c14nExclusive} and
     * {@link #c14nWithComments} properties.
     */
    private String c14nAlgo;

    /** Inclusive prefix list used with exclusive canonicalization. Default value: empty list */
    @Nonnull @NonnullElements @Unmodifiable
    private List<String> inclusivePrefixList = List.of();

    /**
     * Names of attributes to treat as ID attributes for signature referencing. Default value: list containing the
     * non-namespace-qualified attributes 'ID', 'Id', 'id'
     */
    @Nonnull @NonnullElements @Unmodifiable
    private List<QName> idAttributeNames = List.of(new QName("ID"), new QName("id"), new QName("Id"));

    /** Explicit names to associate with the given signing key. Default value: empty list */
    @Nonnull @NonnullElements @Unmodifiable
    private List<String> keyNames = List.of();

    /** Whether key names should be included in the signature's KeyInfo. Default value: <code>true</code> */
    private boolean includeKeyNames = true;

    /**
     * Whether the public key should be included in the signature's KeyInfo.
     * 
     * The public key can be sourced from either the {@link #publicKey} property or from the
     * first provided certificate.
     * 
     * Default value: <code>false</code>
     */
    private boolean includeKeyValue;

    /**
     * Whether the end-entity certificate's subject name should be included in the signature's KeyInfo. Default value:
     * <code>false</code>
     */
    private boolean includeX509SubjectName;

    /**
     * Whether the certificates chain should be included in the signature's KeyInfo. Default value: <code>true</code>
     */
    private boolean includeX509Certificates = true;

    /** Whether the CRLs should be included in the signature's KeyInfo. Default value: <code>false</code> */
    private boolean includeX509Crls;

    /**
     * Whether the end-entity certificate's issuer and serial number should be included in the signature's KeyInfo.
     * Default value: <code>false</code>
     */
    private boolean includeX509IssuerSerial;

    /** Whether to debug digest operations by logging the pre-digest data stream. Default value: <code>false</code> */
    private boolean debugPreDigest;
    
    /**
     * Constructor.
     */
    public XMLSignatureSigningStage() {
        shaVariant = ShaVariant.SHA256;
    }

    /**
     * Gets the SHA algorithm variant used when computing the signature and digest.
     * 
     * @return SHA algorithm variant used when computing the signature and digest
     */
    @Nonnull public ShaVariant getShaVariant() {
        return shaVariant;
    }

    /**
     * Sets the SHA algorithm variant used when computing the signature and digest.
     * 
     * @param variant SHA algorithm variant used when computing the signature and digest
     */
    public synchronized void setShaVariant(@Nonnull final ShaVariant variant) {
        throwSetterPreconditionExceptions();
        shaVariant = Constraint.isNotNull(variant, "SHA variant can not be null");
    }

    /**
     * Gets the private key used to sign the content.
     * 
     * @return the privKey private key used to sign the content
     */
    @Nullable public PrivateKey getPrivateKey() {
        return privKey;
    }

    /**
     * Sets the private key used to sign the content.
     * 
     * @param key private key used to sign the content
     */
    public synchronized void setPrivateKey(@Nonnull final PrivateKey key) {
        throwSetterPreconditionExceptions();
        privKey = Constraint.isNotNull(key, "Private key can not be null");
    }

    /**
     * Gets the public key associated with private key used to sign the content.
     * 
     * @return public key associated with private key used to sign the content
     */
    @Nullable public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Sets public key associated with private key used to sign the content.
     * 
     * @param key public key associated with private key used to sign the content
     */
    public synchronized void setPublicKey(@Nullable final PublicKey key) {
        throwSetterPreconditionExceptions();
        publicKey = key;
    }

    /**
     * Gets the certificates associated with the key used to sign the content. The end-entity certificate is the first
     * element in the list.
     * 
     * @return certificates associated with the key used to sign the content
     */
    @Nonnull @NonnullElements @Unmodifiable
    public List<X509Certificate> getCertificates() {
        return certificates;
    }

    /**
     * Sets the certificates associated with the key used to sign the content. The end-entity certificate must be the
     * first element in the list.
     * 
     * @param certs certificates associated with the key used to sign the content
     */
    public synchronized void setCertificates(
            @Nonnull @NonnullElements @Unmodifiable final List<X509Certificate> certs) {
        throwSetterPreconditionExceptions();
        certificates = List.copyOf(certs);
    }

    /**
     * Gets the CRLs associated with certificates.
     * 
     * @return CRLs associated with certificates
     */
    @Nonnull @NonnullElements @Unmodifiable
    public List<X509CRL> getCrls() {
        return crls;
    }

    /**
     * Sets the CRLs associated with certificates.
     * 
     * @param revocationLists CRLs associated with certificates
     */
    public synchronized void setCrls(
            @Nonnull @NonnullElements @Unmodifiable final List<X509CRL> revocationLists) {
        throwSetterPreconditionExceptions();
        crls = List.copyOf(revocationLists);
    }

    /**
     * Gets whether exclusive canonicalization will be used.
     * 
     * @return whether exclusive canonicalization will be used
     */
    public boolean isC14nExclusive() {
        return c14nExclusive;
    }

    /**
     * Sets whether exclusive canonicalization will be used.
     * 
     * @param isExclusive whether exclusive canonicalization will be used
     */
    public synchronized void setC14nExclusive(final boolean isExclusive) {
        throwSetterPreconditionExceptions();
        c14nExclusive = isExclusive;
    }

    /**
     * Gets whether comments are canonicalized.
     * 
     * @return whether comments are canonicalized
     */
    public boolean isC14nWithComments() {
        return c14nWithComments;
    }

    /**
     * Sets whether comments are canonicalized.
     * 
     * @param withComments whether comments are canonicalized
     */
    public synchronized void setC14nWithComments(final boolean withComments) {
        throwSetterPreconditionExceptions();
        c14nWithComments = withComments;
    }

    /**
     * Gets the inclusive prefix list used during exclusive canonicalization.
     * 
     * @return inclusive prefix list used during exclusive canonicalization
     */
    @Nonnull @NonnullElements @Unmodifiable
    public List<String> getInclusivePrefixList() {
        return inclusivePrefixList;
    }

    /**
     * Sets the inclusive prefix list used during exclusive canonicalization.
     * 
     * @param prefixList inclusive prefix list used during exclusive canonicalization
     */
    public synchronized void setInclusivePrefixList(
            @Nonnull @NonnullElements @Unmodifiable final List<String> prefixList) {
        throwSetterPreconditionExceptions();
        inclusivePrefixList = List.copyOf(prefixList);
    }

    /**
     * Gets the names of the attributes treated as reference IDs.
     * 
     * @return names of the attributes treated as reference IDs
     */
    @Nonnull @NonnullElements @Unmodifiable
    public List<QName> getIdAttributeNames() {
        return idAttributeNames;
    }

    /**
     * Sets the names of the attributes treated as reference IDs.
     * 
     * @param names names of the attributes treated as reference IDs
     */
    public synchronized void setIdAttributeNames(
            @Nonnull @NonnullElements @Unmodifiable final List<QName> names) {
        throwSetterPreconditionExceptions();
        Constraint.isNotNull(names, "names property may not be null");

        idAttributeNames = List.copyOf(names);
    }

    /**
     * Gets the explicit key names added to the KeyInfo.
     * 
     * @return explicit key names added to the KeyInfo
     */
    @Nonnull @NonnullElements @Unmodifiable
    public List<String> getKeyNames() {
        return keyNames;
    }

    /**
     * Sets the explicit key names added to the KeyInfo.
     * 
     * @param names explicit key names added to the KeyInfo
     */
    public synchronized void setKeyNames(
            @Nonnull @NonnullElements @Unmodifiable final List<String> names) {
        throwSetterPreconditionExceptions();
        keyNames = List.copyOf(names);
    }

    /**
     * Gets whether key names are included in the KeyInfo.
     * 
     * @return whether key names are included in the KeyInfo
     */
    public boolean isIncludeKeyNames() {
        return includeKeyNames;
    }

    /**
     * Sets whether key names are included in the KeyInfo.
     * 
     * @param include whether key names are included in the KeyInfo
     */
    public synchronized void setIncludeKeyNames(final boolean include) {
        throwSetterPreconditionExceptions();
        includeKeyNames = include;
    }

    /**
     * Gets whether key values are included in the KeyInfo.
     * 
     * @return whether key values are included in the KeyInfo
     */
    public boolean isIncludeKeyValue() {
        return includeKeyValue;
    }

    /**
     * Sets whether key values are included in the KeyInfo.
     * 
     * @param included whether key values are included in the KeyInfo
     */
    public synchronized void setIncludeKeyValue(final boolean included) {
        throwSetterPreconditionExceptions();
        includeKeyValue = included;
    }

    /**
     * Gets whether end-entity certifcate's subject name is included in the KeyInfo.
     * 
     * @return whether end-entity certifcate's subject name is included in the KeyInfo
     */
    public boolean isIncludeX509SubjectName() {
        return includeX509SubjectName;
    }

    /**
     * Sets whether end-entity certifcate's subject name is included in the KeyInfo.
     * 
     * @param include whether end-entity certifcate's subject name is included in the KeyInfo
     */
    public synchronized void setIncludeX509SubjectName(final boolean include) {
        throwSetterPreconditionExceptions();
        includeX509SubjectName = include;
    }

    /**
     * Gets whether X509 certificates are included in the KeyInfo.
     * 
     * @return whether X509 certificates are included in the KeyInfo
     */
    public boolean isIncludeX509Certificates() {
        return includeX509Certificates;
    }

    /**
     * Sets whether X509 certificates are included in the KeyInfo.
     * 
     * @param include whether X509 certificates are included in the KeyInfo
     */
    public synchronized void setIncludeX509Certificates(final boolean include) {
        throwSetterPreconditionExceptions();
        includeX509Certificates = include;
    }

    /**
     * Gets whether CRLs are included in the KeyInfo.
     * 
     * @return whether CRLs are included in the KeyInfo
     */
    public boolean isIncludeX509Crls() {
        return includeX509Crls;
    }

    /**
     * Sets whether CRLs are included in the KeyInfo.
     * 
     * @param include whether CRLs are included in the KeyInfo
     */
    public synchronized void setIncludeX509Crls(final boolean include) {
        throwSetterPreconditionExceptions();
        includeX509Crls = include;
    }

    /**
     * Gets whether the end-entity certificate's issuer and serial number are included in the KeyInfo.
     * 
     * @return whether the end-entity certificate's issuer and serial number are included in the KeyInfo
     */
    public boolean isIncludeX509IssuerSerial() {
        return includeX509IssuerSerial;
    }

    /**
     * Sets whether the end-entity certificate's issuer and serial number are included in the KeyInfo.
     * 
     * @param include whether the end-entity certificate's issuer and serial number are included in the KeyInfo
     */
    public synchronized void setIncludeX509IssuerSerial(final boolean include) {
        throwSetterPreconditionExceptions();
        includeX509IssuerSerial = include;
    }

    /**
     * Gets whether logging of the pre-digest data stream is enabled.
     * 
     * @return whether logging of the pre-digest data stream is enabled
     */
    public boolean isDebugPreDigest() {
        return debugPreDigest;
    }
    
    /**
     * Sets whether logging of the pre-digest data stream is enabled.
     * 
     * @param debug whether logging of the pre-digest data stream is enabled
     */
    public synchronized void setDebugPreDigest(final boolean debug) {
        throwSetterPreconditionExceptions();
        debugPreDigest = debug;
    }
    
    /**
     * Gets the signature algorithm used when signing.
     * 
     * @return signature algorithm used when signing
     */
    public String getSigAlgo() {
        return sigAlgo;
    }

    /**
     * Gets the digest algorithm used when signing.
     * 
     * @return digest algorithm used when signing
     */
    public String getDigestAlgo() {
        return digestAlgo;
    }

    /**
     * Gets whether CR characters will be removed from generated signatures.
     *
     * @return <code>true</code> if CR characters will be removed from generated signatures.
     */
    public boolean isRemovingCRsFromSignature() {
        return removingCRsFromSignature;
    }

    /**
     * Sets whether to remove CR characters from generated signatures.
     *
     * @param newValue whether to remove CR characters from generated signatures.
     */
    public void setRemovingCRsFromSignature(final boolean newValue) {
        throwSetterPreconditionExceptions();
        removingCRsFromSignature = newValue;
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

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element element = item.unwrap();
        final XMLSignature signature = xmlSigFactory.newXMLSignature(buildSignedInfo(element), buildKeyInfo());
        try {
            final XMLSignContext context = new DOMSignContext(privKey, element, element.getFirstChild());
            
            // Enable caching reference values if required for debugging.
            if (isDebugPreDigest() && log.isDebugEnabled()) {
                context.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
            }
            
            // Perform the signature operation
            signature.sign(context);
            
            // Remove any CRs from selected signature elements.
            if (isRemovingCRsFromSignature()) {
                final Element signatureElement = ElementSupport.getFirstChildElement(element,
                        XMLDSIGSupport.SIGNATURE_NAME);
                removeCRsFromNamedChildren(signatureElement, "SignatureValue");
                removeCRsFromNamedChildren(signatureElement, "X509Certificate");
            }

            // Log the pre-digest data for debugging
            if (isDebugPreDigest() && log.isDebugEnabled()) {
                final Reference ref = signature.getSignedInfo().getReferences().get(0);
                final String preDigest = new String(ref.getDigestInputStream().readAllBytes(), StandardCharsets.UTF_8);
                log.debug("pre digest: {}", preDigest);
            }
        } catch (final Exception e) {
            log.error("Unable to create signature for element", e);
            throw new StageProcessingException("Unable to create signature for element", e);
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
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        }

        final SignatureMethod sigMethod;
        try {
            sigMethod = xmlSigFactory.newSignatureMethod(sigAlgo, null);
        } catch (final Exception e) {
            final String errMsg = "Unable to create signature method " + sigAlgo;
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        }

        final List<Reference> refs = Collections.singletonList(buildSignatureReference(target));

        return xmlSigFactory.newSignedInfo(c14nMethod, sigMethod, refs);
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
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        }

        TransformParameterSpec transformSpec;
        final ArrayList<Transform> transforms = new ArrayList<>();

        try {
            transformSpec = null;
            transforms.add(xmlSigFactory.newTransform(Transform.ENVELOPED, transformSpec));
        } catch (final Exception e) {
            final String errMsg = "Unable to create transform " + Transform.ENVELOPED;
            log.error(errMsg, e);
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
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        }

        return xmlSigFactory.newReference(refUri, digestMethod, transforms, null, null);
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
    @Nullable protected String getElementId(@Nonnull final Element target) {
        final NamedNodeMap attributes = target.getAttributes();
        if (attributes == null || attributes.getLength() < 1) {
            return null;
        }

        if (idAttributeNames != null && !idAttributeNames.isEmpty()) {
            for (int i = 0; i < attributes.getLength(); i++) {
                final Attr attribute = (Attr) attributes.item(i);
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
                log.error("Unable to create KeyValue", e);
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

    @Override
    protected void doDestroy() {
        xmlSigFactory = null;
        keyInfoFactory = null;
        privKey = null;
        publicKey = null;
        certificates = null;
        crls = null;
        sigAlgo = null;
        digestAlgo = null;
        c14nAlgo = null;
        inclusivePrefixList = null;
        idAttributeNames = null;
        keyNames = null;

        super.doDestroy();
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (!Init.isInitialized()) {
            Init.init();
        }

        xmlSigFactory = XMLSignatureFactory.getInstance();
        keyInfoFactory = xmlSigFactory.getKeyInfoFactory();

        switch (shaVariant) {
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

    }
}
