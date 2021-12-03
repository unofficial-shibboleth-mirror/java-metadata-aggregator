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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import org.apache.xml.security.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.impl.XMLSignatureSigner;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

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
public class XMLSignatureSigningStage extends AbstractStage<Element> {

    /** The variant of SHA to use in the various signature algorithms. */
    public static enum ShaVariant {
        /** 160-bit SHA-1. */
        SHA1,
        /** 256-bit SHA-2. */
        SHA256,
        /** 384-bit SHA-2. */
        SHA384,
        /** 512-bit SHA-2. */
        SHA512
    };

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XMLSignatureSigningStage.class);

    /** SHA algorithm variant used in signature and digest algorithms. Default value: <code>ShaVariant.SHA256</code> */
    @Nonnull @GuardedBy("this")
    private ShaVariant shaVariant = ShaVariant.SHA256;

    /** Private key used to sign data. */
    @NonnullAfterInit @GuardedBy("this")
    private PrivateKey privKey;

    /** Public key associated with the given private key. */
    @Nullable @GuardedBy("this")
    private PublicKey publicKey;

    /**
     * Certificate chain, with end entity certificate as element 0, to be included with the signature. Default value:
     * empty list
     */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<X509Certificate> certificates = List.of();

    /** CRLs to be included with the signature. Default value: empty list */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<X509CRL> crls = List.of();

    /** Whether to use exclusive canonicalization. Default value: <code>true</code> */
    @GuardedBy("this") private boolean c14nExclusive = true;

    /** Whether to include comments in the canonicalized data. Default value: <code>false</code> */
    @GuardedBy("this") private boolean c14nWithComments;

    /** Whether to remove CR characters from generated signatures. Default value: <code>true</code>. */
    @GuardedBy("this") private boolean removingCRsFromSignature = true;

    /** Inclusive prefix list used with exclusive canonicalization. Default value: empty list */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<String> inclusivePrefixList = List.of();

    /**
     * Names of attributes to treat as ID attributes for signature referencing. Default value: list containing the
     * non-namespace-qualified attributes 'ID', 'Id', 'id'
     */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<QName> idAttributeNames = List.of(new QName("ID"), new QName("id"), new QName("Id"));

    /** Explicit names to associate with the given signing key. Default value: empty list */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<String> keyNames = List.of();

    /** Whether key names should be included in the signature's KeyInfo. Default value: <code>true</code> */
    @GuardedBy("this") private boolean includeKeyNames = true;

    /**
     * Whether the public key should be included in the signature's KeyInfo.
     * 
     * The public key can be sourced from either the {@link #publicKey} property or from the
     * first provided certificate.
     * 
     * Default value: <code>false</code>
     */
    @GuardedBy("this") private boolean includeKeyValue;

    /**
     * Whether the end-entity certificate's subject name should be included in the signature's KeyInfo. Default value:
     * <code>false</code>
     */
    @GuardedBy("this") private boolean includeX509SubjectName;

    /**
     * Whether the certificates chain should be included in the signature's KeyInfo. Default value: <code>true</code>
     */
    @GuardedBy("this") private boolean includeX509Certificates = true;

    /** Whether the CRLs should be included in the signature's KeyInfo. Default value: <code>false</code> */
    @GuardedBy("this") private boolean includeX509Crls;

    /**
     * Whether the end-entity certificate's issuer and serial number should be included in the signature's KeyInfo.
     * Default value: <code>false</code>
     */
    @GuardedBy("this") private boolean includeX509IssuerSerial;

    /** Whether to debug digest operations by logging the pre-digest data stream. Default value: <code>false</code> */
    @GuardedBy("this") private boolean debugPreDigest;

    /**
     * Gets the SHA algorithm variant used when computing the signature and digest.
     * 
     * @return SHA algorithm variant used when computing the signature and digest
     */
    @Nonnull public final synchronized ShaVariant getShaVariant() {
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
    @Nullable public final synchronized PrivateKey getPrivateKey() {
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
    @Nullable public final synchronized PublicKey getPublicKey() {
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
    public final synchronized List<X509Certificate> getCertificates() {
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
    public final synchronized List<X509CRL> getCrls() {
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
    public final synchronized boolean isC14nExclusive() {
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
    public final synchronized boolean isC14nWithComments() {
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
    public final synchronized List<String> getInclusivePrefixList() {
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
    public final synchronized List<QName> getIdAttributeNames() {
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
    public final synchronized List<String> getKeyNames() {
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
    public final synchronized boolean isIncludeKeyNames() {
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
    public final synchronized boolean isIncludeKeyValue() {
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
    public final synchronized boolean isIncludeX509SubjectName() {
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
    public final synchronized boolean isIncludeX509Certificates() {
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
    public final synchronized boolean isIncludeX509Crls() {
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
    public final synchronized boolean isIncludeX509IssuerSerial() {
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
    public final synchronized boolean isDebugPreDigest() {
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
     * Gets whether CR characters will be removed from generated signatures.
     *
     * @return <code>true</code> if CR characters will be removed from generated signatures.
     */
    public final synchronized boolean isRemovingCRsFromSignature() {
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

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<Element>> items)
            throws StageProcessingException {
        final var signer = new XMLSignatureSigner(this, log);
        for (final Item<Element> item : items) {
            signer.sign(item);
        }
    }

    @Override
    protected void doDestroy() {
        privKey = null;
        publicKey = null;
        certificates = null;
        crls = null;
        inclusivePrefixList = null;
        idAttributeNames = null;
        keyNames = null;

        super.doDestroy();
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        Init.init();
    }

}
