package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Build a certificate based on information passed to this object.
 * <p>
 * The subjectSampleCertificate can be used as a types. The ca can create some of the fields manually thus
 * modifying suggestions provided by the sample certificate.
 * <p>
 * The object shall not be reused. After the build method is called, this object is not reusable.
 *
 * @author fpo
 */
public class CaSignedCertificateBuilder {

    private boolean createCaCert;

    private X500Name subjectDN;

    private Integer notAfterInDays;
    private Integer notBeforeInDays = 0;

    private int keyUsage = -1;
    private boolean keyUsageSet = false;

    private String signatureAlgo;

    private PublicKey subjectPublicKey;

    boolean dirty = false;

    @SneakyThrows
    public X509CertificateHolder build(PrivateKey issuerPrivatekey) {
        if (dirty) throw new IllegalStateException("Builder can not be reused");
        dirty = true;

        // AUtoselect algorithm
        if (StringUtils.isBlank(signatureAlgo)) {
            String algorithm = issuerPrivatekey.getAlgorithm();
            if (StringUtils.equalsAnyIgnoreCase("DSA", algorithm)) {
                signatureAlgo = "SHA256withDSA";
            } else if (StringUtils.equals("RSA", algorithm)) {
                signatureAlgo = "SHA256WithRSA";
            }
        }

        Date now = new Date();
        Date notAfter = notAfterInDays != null ? DateUtils.addDays(now, notAfterInDays) : null;
        Date notBefore = notBeforeInDays != null ? DateUtils.addDays(now, notBeforeInDays) : null;

        List<KeyValue> notNullCheckList = ListOfKeyValueBuilder.newBuilder()
                .add("X509CertificateBuilder_missing_subject_DN", subjectDN)
                .add("X509CertificateBuilder_missing_subject_publicKey", subjectPublicKey)
                .add("X509CertificateBuilder_missing_validity_date_notBefore", notBefore)
                .add("X509CertificateBuilder_missing_validity_date_notAfter", notAfter)
                .build();

        List<String> errorKeys = BatchValidator.filterNull(notNullCheckList);
        if (errorKeys == null) errorKeys = new ArrayList<>();

        X500Name issuerDN = null;
        BasicConstraints basicConstraints = null;
        // Self signed certificate
        issuerDN = subjectDN;
        if (createCaCert) {
            // self signed ca certificate
            basicConstraints = new BasicConstraints(true);
            // in ca case, subject must subject must be set
            boolean subjectOnlyInAlternativeName = false;
        } else {
            // not a ca certificate
            basicConstraints = new BasicConstraints(false);
        }

        BigInteger serial = SerialNumberGenerator.uniqueSerial();

        X509v3CertificateBuilder v3CertGen = null;

        if (!errorKeys.isEmpty()) {
            throw new IllegalArgumentException("Fields can not be null: " + errorKeys);
        }

        v3CertGen = new JcaX509v3CertificateBuilder(issuerDN, serial, notBefore, notAfter, subjectDN, subjectPublicKey);
        JcaX509ExtensionUtils extUtils = V3CertificateUtils.getJcaX509ExtensionUtils();

        v3CertGen.addExtension(X509Extension.basicConstraints, true, basicConstraints);

        v3CertGen.addExtension(X509Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(subjectPublicKey));

        v3CertGen.addExtension(X509Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(subjectPublicKey));

        if (keyUsageSet) {
            v3CertGen.addExtension(X509Extension.keyUsage,
                    true, new KeyUsage(this.keyUsage));
        }

        ContentSigner signer = V3CertificateUtils.getContentSigner(issuerPrivatekey, signatureAlgo);

        return v3CertGen.build(signer);

    }

    public CaSignedCertificateBuilder withCa(boolean ca) {
        this.createCaCert = ca;
        return this;
    }

    public CaSignedCertificateBuilder withSubjectDN(X500Name subjectDN) {
        this.subjectDN = subjectDN;
        return this;
    }

    public CaSignedCertificateBuilder withSubjectPublicKey(PublicKey subjectPublicKey) {
        this.subjectPublicKey = subjectPublicKey;
        return this;
    }


    public CaSignedCertificateBuilder withNotAfterInDays(Integer notAfterInDays) {
        this.notAfterInDays = notAfterInDays;
        return this;
    }

    public CaSignedCertificateBuilder withNotBeforeInDays(Integer notBeforeInDays) {
        this.notBeforeInDays = notBeforeInDays;
        return this;
    }

    public CaSignedCertificateBuilder withKeyUsage(int keyUsage) {
        if (keyUsageSet) {
            this.keyUsage = this.keyUsage | keyUsage;
        } else {
            this.keyUsage = keyUsage;
            keyUsageSet = true;
        }
        return this;
    }
}
