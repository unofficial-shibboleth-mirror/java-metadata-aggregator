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
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
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

import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Assert;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.QNameSupport;
import net.shibboleth.utilities.java.support.xml.XmlConstants;

import org.apache.xml.security.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * A pipeline stage that creates, and adds, an enveloped signature for each element in the given {@link DomElementItem}
 * collection.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>privateKey</code></li>
 * </ul>
 */
@ThreadSafe
public class XMLSignatureSigningStage extends BaseIteratingStage<DomElementItem> {

    /** The variant of SHA to use in the various signature algorithms. */
    public static enum ShaVariant {
        SHA1, SHA256, SHA384, SHA512
    };

    /** XML Signature base URI: {@value} . */
    public static final String XML_SIG_NS_URI = "http://www.w3.org/2000/09/xmldsig#";

    /** QName of the Signature element. */
    public static final QName SIGNATURE_NAME = new QName(XML_SIG_NS_URI, "Signature");

    /** XML Encryption base URI: {@value} . */
    public static final String XML_ENC_NS_URI = "http://www.w3.org/2001/04/xmlenc#";

    /**
     * RFC4501 base URI: {@value} .
     * 
     * @see <a href="http://tools.ietf.org/html/rfc4501">RFC 4501</a>
     */
    public static final String RFC4501_BASE_URI = "http://www.w3.org/2001/04/xmldsig-more";

    /** RSA-SHA1 signature algorithm ID: {@value} . */
    public static final String ALGO_ID_SIGNATURE_RSA_SHA1 = XML_SIG_NS_URI + "rsa-sha1";

    /** RSA-SHA256 signature algorithm ID: {@value} . */
    public static final String ALGO_ID_SIGNATURE_RSA_SHA256 = RFC4501_BASE_URI + "#rsa-sha256";

    /** RSA-SHA384 signature algorithm ID: {@value} . */
    public static final String ALGO_ID_SIGNATURE_RSA_SHA384 = RFC4501_BASE_URI + "#rsa-sha384";

    /** RSA-SHA512 signature algorithm ID: {@value} . */
    public static final String ALGO_ID_SIGNATURE_RSA_SHA512 = RFC4501_BASE_URI + "#rsa-sha512";

    /** SHA1 digest algorithm ID: {@value} . */
    public static final String ALGO_ID_DIGEST_SHA1 = XML_SIG_NS_URI + "sha1";

    /** SHA256 digest algorithm ID: {@value} . */
    public static final String ALGO_ID_DIGEST_SHA256 = XML_ENC_NS_URI + "sha256";

    /** SHA384 digest algorithm ID: {@value} . */
    public static final String ALGO_ID_DIGEST_SHA384 = RFC4501_BASE_URI + "#sha384";

    /** SHA512 digest algorithm ID: {@value} . */
    public static final String ALGO_ID_DIGEST_SHA512 = XML_ENC_NS_URI + "sha512";

    /** Inclusive canonicalization, <strong>WITHOUT</strong> comments, algorithm ID: {@value} . */
    public static final String ALGO_ID_C14N_OMIT_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

    /** Inclusive canonicalization, <strong>WITH</strong> comments, algorithm ID: {@value} . */
    public static final String ALGO_ID_C14N_WITH_COMMENTS = ALGO_ID_C14N_OMIT_COMMENTS + "#WithComments";

    /** Exclusive canonicalization, <strong>WITHOUT</strong> comments, algorithm ID: {@value} . */
    public static final String ALGO_ID_C14N_EXCL_OMIT_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#";

    /** Exclusive canonicalization, <strong>WITH</strong> comments, algorithm ID: {@value} . */
    public static final String ALGO_ID_C14N_EXCL_WITH_COMMENTS = ALGO_ID_C14N_EXCL_OMIT_COMMENTS + "WithComments";

    /** Enveloped signature transform ID: {@value} . */
    public static final String TRANSFORM_ENVELOPED_SIGNATURE = XML_SIG_NS_URI + "enveloped-signature";

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
    private PublicKey pubKey;

    /**
     * Certificate chain, with end entity certificate as element 0, to be included with the signature. Default value:
     * empty list
     */
    private List<X509Certificate> certificates = Collections.emptyList();

    /** CRLs to be included with the signature. Default value: empty list */
    private List<X509CRL> crls = Collections.emptyList();

    /** Signature algorithm used. */
    private String sigAlgo;

    /** Digest algorithm used. */
    private String digestAlgo;

    /** Whether to use exclusive canonicalization. Default value: <code>true</code> */
    private boolean c14nExclusive = true;

    /** Whether to include comments in the canonicalized data. Default value: <code>false</code> */
    private boolean c14nWithComments;

    /**
     * Canonicalization algorithm to use. This is determined from the {@link #c14nExclusive} and
     * {@link #c14nWithComments} properties.
     */
    private String c14nAlgo;

    /** Inclusive prefix list used with exclusive canonicalization. Default value: empty list */
    private List<String> inclusivePrefixList = Collections.emptyList();

    /** Names of attributes to treat as ID attributes for signature referencing. Default value: empty list */
    private List<QName> idAttributeNames = Collections.emptyList();

    /** Explicit names to associate with the given signing key. Default value: empty list */
    private List<String> keyNames = Collections.emptyList();

    /**
     * Whether additional key names should be derived from the end-entity certificate, if present. Default value:
     * <code>true</code>
     */
    private boolean deriveKeyNames = true;

    /** Whether key names should be included in the signature's KeyInfo. Default value: <code>true</code> */
    private boolean includeKeyNames = true;

    /** Whether the public key should be included in the signature's KeyInfo. Default value: <code>false</code> */
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        shaVariant = Assert.isNotNull(variant, "SHA variant can not be null");
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        privKey = Assert.isNotNull(key, "Private key can not be null");
    }

    /**
     * Gets the public key associated with private key used to sign the content.
     * 
     * @return public key associated with private key used to sign the content
     */
    @Nullable public PublicKey getPublicKey() {
        return pubKey;
    }

    /**
     * Sets public key associated with private key used to sign the content.
     * 
     * @param key public key associated with private key used to sign the content
     */
    public synchronized void setPublicKey(@Nullable final PublicKey key) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        pubKey = key;
    }

    /**
     * Gets the certificates associated with the key used to sign the content. The end-entity certificate is the 0th
     * element in the list.
     * 
     * @return certificates associated with the key used to sign the content
     */
    @Nonnull @NonnullElements @Unmodifiable public List<X509Certificate> getCertificates() {
        return certificates;
    }

    /**
     * Sets the certificates associated with the key used to sign the content. The end-entity certificate must be the
     * 0th element in the list.
     * 
     * @param certs certificates associated with the key used to sign the content
     */
    public synchronized void setCertificates(@Nullable @NullableElements final List<X509Certificate> certs) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        if(certs == null || certs.isEmpty()){
            certificates = Collections.emptyList();
        }else{
            certificates = ImmutableList.copyOf(Iterables.filter(certs, Predicates.notNull()));
        }
    }

    /**
     * Gets the CRLs associated with certificates.
     * 
     * @return CRLs associated with certificates
     */
    @Nonnull @NonnullElements @Unmodifiable public List<X509CRL> getCrls() {
        return crls;
    }

    /**
     * Sets the CRLs associated with certificates.
     * 
     * @param revocationLists CRLs associated with certificates
     */
    public synchronized void setCrls(@Nullable @NullableElements final List<X509CRL> revocationLists) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        if(revocationLists == null | revocationLists.isEmpty()){
            crls = Collections.emptyList();
        }else{
            crls = ImmutableList.copyOf(Iterables.filter(revocationLists, Predicates.notNull()));
        }
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        c14nWithComments = withComments;
    }

    /**
     * Gets the inclusive prefix list used during exclusive canonicalization.
     * 
     * @return inclusive prefix list used during exclusive canonicalization
     */
    @Nonnull @NonnullElements @Unmodifiable public List<String> getInclusivePrefixList() {
        return inclusivePrefixList;
    }

    /**
     * Sets the inclusive prefix list used during exclusive canonicalization.
     * 
     * @param prefixList inclusive prefix list used during exclusive canonicalization
     */
    public synchronized void setInclusivePrefixList(@Nullable @NullableElements final List<String> prefixList) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        if(prefixList == null || prefixList.isEmpty()){
            inclusivePrefixList = Collections.emptyList();
        }else{
            inclusivePrefixList = ImmutableList.copyOf(Iterables.filter(prefixList, Predicates.notNull()));
        }
    }

    /**
     * Gets the names of the attributes treated as reference IDs.
     * 
     * @return names of the attributes treated as reference IDs
     */
    @Nonnull @NonnullElements @Unmodifiable public List<QName> getIdAttributeNames() {
        return idAttributeNames;
    }

    /**
     * Sets the names of the attributes treated as reference IDs.
     * 
     * @param names names of the attributes treated as reference IDs
     */
    public synchronized void setIdAttributeNames(@Nullable @NullableElements final List<QName> names) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        idAttributeNames = ImmutableList.copyOf(Iterables.filter(names, Predicates.notNull()));
    }

    /**
     * Gets the explicit key names added to the KeyInfo.
     * 
     * @return explicit key names added to the KeyInfo
     */
    @Nonnull @NonnullElements public List<String> getKeyNames() {
        return keyNames;
    }

    /**
     * Sets the explicit key names added to the KeyInfo.
     * 
     * @param names explicit key names added to the KeyInfo
     */
    public synchronized void setKeyNames(@Nullable @NullableElements final List<String> names) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        keyNames = ImmutableList.copyOf(Iterables.filter(names, Predicates.notNull()));
    }

    /**
     * Gets whether key names are derived from the end-entity certificate, if present. TODO describe was is derived
     * 
     * @return whether key names are derived from the end-entity certificate
     */
    public boolean isDeriveKeyNames() {
        return deriveKeyNames;
    }

    /**
     * Sets whether key names are derived from the end-entity certificate.
     * 
     * @param deriveNames whether key names are derived from the end-entity certificate
     */
    public synchronized void setDeriveKeyNames(final boolean deriveNames) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        deriveKeyNames = deriveNames;
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        includeX509IssuerSerial = include;
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

    /** {@inheritDoc} */
    protected boolean doExecute(@Nonnull final DomElementItem item) throws StageProcessingException {
        Element element = item.unwrap();
        XMLSignature signature = xmlSigFactory.newXMLSignature(buildSignedInfo(element), buildKeyInfo());
        try {
            signature.sign(new DOMSignContext(privKey, element, element.getFirstChild()));
        } catch (Exception e) {
            log.error("Unable to create signature for element", e);
            throw new StageProcessingException("Unable to create signature for element", e);
        }

        return true;
    }

    /**
     * Gets the descriptor of signed content.
     * 
     * @param target the element that will be signed
     * 
     * @return signed content descriptor
     * 
     * @throws StageProcessingException thrown if there is a problem create the signed content descriptor
     */
    @Nonnull protected SignedInfo buildSignedInfo(@Nonnull final Element target) throws StageProcessingException {
        C14NMethodParameterSpec c14nMethodSpec = null;
        if (c14nAlgo.startsWith(ALGO_ID_C14N_EXCL_OMIT_COMMENTS) && inclusivePrefixList != null
                && !inclusivePrefixList.isEmpty()) {
            c14nMethodSpec = new ExcC14NParameterSpec(inclusivePrefixList);
        }

        CanonicalizationMethod c14nMethod;
        try {
            c14nMethod = xmlSigFactory.newCanonicalizationMethod(c14nAlgo, c14nMethodSpec);
        } catch (Exception e) {
            String errMsg = "Unable to create transform " + c14nAlgo;
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        }

        SignatureMethod sigMethod;
        try {
            sigMethod = xmlSigFactory.newSignatureMethod(sigAlgo, null);
        } catch (Exception e) {
            String errMsg = "Unable to create signature method " + sigAlgo;
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
    @Nonnull protected Reference buildSignatureReference(@Nonnull final Element target) throws StageProcessingException {
        final String id = getElementId(target);
        final String refUri;
        if (id == null) {
            refUri = "";
        } else {
            refUri = "#" + id;
        }

        DigestMethod digestMethod = null;
        try {
            DigestMethodParameterSpec digestMethodSpec = null;
            digestMethod = xmlSigFactory.newDigestMethod(digestAlgo, digestMethodSpec);
        } catch (Exception e) {
            String errMsg = "Unable to create digest method " + digestAlgo;
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        }

        TransformParameterSpec transformSpec;
        final ArrayList<Transform> transforms = new ArrayList<Transform>();

        try {
            transformSpec = null;
            transforms.add(xmlSigFactory.newTransform(TRANSFORM_ENVELOPED_SIGNATURE, transformSpec));
        } catch (Exception e) {
            String errMsg = "Unable to create transform " + TRANSFORM_ENVELOPED_SIGNATURE;
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        }

        try {
            if (c14nAlgo.startsWith(ALGO_ID_C14N_EXCL_OMIT_COMMENTS) && inclusivePrefixList != null
                    && !inclusivePrefixList.isEmpty()) {
                transformSpec = new ExcC14NParameterSpec(inclusivePrefixList);
            }
            transforms.add(xmlSigFactory.newTransform(c14nAlgo, transformSpec));
        } catch (Exception e) {
            String errMsg = "Unable to create transform " + c14nAlgo;
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

        Attr attribute;
        String value;
        if (idAttributeNames != null && !idAttributeNames.isEmpty()) {
            for (int i = 0; i < attributes.getLength(); i++) {
                attribute = (Attr) attributes.item(i);
                if (idAttributeNames.contains(QNameSupport.getNodeQName(attribute))) {
                    value = StringSupport.trimOrNull(attribute.getValue());
                    if (value != null) {
                        return value;
                    }
                }
            }
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            attribute = (Attr) attributes.item(i);
            if (attribute.isId()) {
                value = StringSupport.trimOrNull(attribute.getValue());
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
    @Nonnull protected KeyInfo buildKeyInfo() throws StageProcessingException {
        final ArrayList<Object> keyInfoItems = new ArrayList<Object>();

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
    protected void addKeyNames(@Nonnull @NonnullElements @Live final ArrayList<Object> keyInfoItems) throws StageProcessingException {
        if (!includeKeyNames) {
            return;
        }

        if (keyNames != null && !keyNames.isEmpty()) {
            for (String name : keyNames) {
                keyInfoItems.add(keyInfoFactory.newKeyName(name));
            }
        }

        if (deriveKeyNames) {
            // TODO derived key names
        }
    }

    /**
     * Adds raw key values to the KeyInfo if key values are to be included.
     * 
     * @param keyInfoItems collector for KeyInfo children
     * 
     * @throws StageProcessingException thrown if there is a problem creating the KeyValue content
     */
    protected void addKeyValue(@Nonnull @NonnullElements @Live final ArrayList<Object> keyInfoItems) throws StageProcessingException {
        if (!includeKeyValue) {
            return;
        }

        PublicKey key = pubKey;
        if (key == null && certificates != null) {
            X509Certificate cert = certificates.get(0);
            if (cert != null) {
                key = cert.getPublicKey();
            }
        }
        if (key != null) {
            try {
                keyInfoItems.add(keyInfoFactory.newKeyValue(key));
            } catch (Exception e) {
                log.error("Unable to create KeyValue", e);
                throw new StageProcessingException("Unable to create KeyValue", e);
            }
        }
    }

    /**
     * Adds X509 data (subject names, certificates, CRLs, and Issuer/Serial) set to be included, into the key info.
     * 
     * @param keyInfoItems collector for KeyInfo children
     * 
     * @throws StageProcessingException thrown if there is a problem creating the X509Data content
     */
    protected void addX509Data(@Nonnull @NonnullElements @Live final ArrayList<Object> keyInfoItems) throws StageProcessingException {
        final ArrayList<Object> x509Data = new ArrayList<Object>();

        if (certificates != null && !certificates.isEmpty()) {
            X509Certificate endEntityCert = certificates.get(0);

            if (includeX509SubjectName) {
                X500Principal subjectDn = endEntityCert.getSubjectX500Principal();
                keyInfoItems.add(subjectDn.getName(X500Principal.RFC2253));
            }

            if (includeX509Certificates) {
                x509Data.addAll(certificates);
            }

            if (includeX509IssuerSerial) {
                X500Principal issuerDn = endEntityCert.getIssuerX500Principal();
                BigInteger serialNumber = endEntityCert.getSerialNumber();
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

    /** {@inheritDoc} */
    protected void doDestroy() {
        xmlSigFactory = null;
        keyInfoFactory = null;
        privKey = null;
        pubKey = null;
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
    
    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (!Init.isInitialized()) {
            Init.isInitialized();
        }

        xmlSigFactory = XMLSignatureFactory.getInstance();
        keyInfoFactory = xmlSigFactory.getKeyInfoFactory();

        switch (shaVariant) {
            case SHA1:
                sigAlgo = ALGO_ID_SIGNATURE_RSA_SHA1;
                digestAlgo = ALGO_ID_DIGEST_SHA1;
                break;
            case SHA384:
                sigAlgo = ALGO_ID_SIGNATURE_RSA_SHA384;
                digestAlgo = ALGO_ID_DIGEST_SHA384;
                break;

            case SHA512:
                sigAlgo = ALGO_ID_SIGNATURE_RSA_SHA512;
                digestAlgo = ALGO_ID_DIGEST_SHA512;
                break;

            case SHA256:
            default:
                sigAlgo = ALGO_ID_SIGNATURE_RSA_SHA256;
                digestAlgo = ALGO_ID_DIGEST_SHA256;
                break;
        }

        if (c14nExclusive) {
            if (c14nWithComments) {
                c14nAlgo = ALGO_ID_C14N_EXCL_WITH_COMMENTS;
            } else {
                c14nAlgo = ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
            }
        } else {
            if (c14nWithComments) {
                c14nAlgo = ALGO_ID_C14N_WITH_COMMENTS;
            } else {
                c14nAlgo = ALGO_ID_C14N_OMIT_COMMENTS;
            }
        }

        if (idAttributeNames == null) {
            idAttributeNames = new ArrayList<QName>();
            idAttributeNames.add(new QName("id"));
            idAttributeNames.add(new QName("Id"));
            idAttributeNames.add(new QName("ID"));
            idAttributeNames.add(XmlConstants.XML_ID_ATTRIB_NAME);
        }
    }
}