package de.adorsys.docusafe2.keystore.impl;

import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;

import java.util.Arrays;

public class CheckCaCertificate {

	public static boolean isSigingCertificate(X509CertificateHolder signed, X509CertificateHolder signer) {
		// The issuer name of the signed certificate matches the subject name of
		// the signer certificate
		if (!signed.getIssuer().equals(signer.getSubject()))
			return false;

		// authority key identifier of signed certificate is not null
		AuthorityKeyIdentifier authorityKeyIdentifier = KeyIdUtils.readAuthorityKeyIdentifier(signed);
		if (authorityKeyIdentifier == null)
			return false;

		// subject identifier of signer certificate is not null
		SubjectKeyIdentifier subjectKeyIdentifier = KeyIdUtils.readSubjectKeyIdentifier(signer);

		// both match
		if (!Arrays.equals(subjectKeyIdentifier.getKeyIdentifier(), authorityKeyIdentifier.getKeyIdentifier()))
			return false;

		// including serial
		if (!signer.getSerialNumber().equals(authorityKeyIdentifier.getAuthorityCertSerialNumber()))
			return false;

		return CertVerifier.verify(signed, signer);
	}

	public static boolean isCaCertificate(X509CertificateHolder issuerCertificate) {
		// check is issuerCertificate is ca certificate
		Extension basicConstraintsExtension = issuerCertificate.getExtension(X509Extension.basicConstraints);
		BasicConstraints issuerBasicConstraints = BasicConstraints
				.getInstance(basicConstraintsExtension.getParsedValue());
		if (!issuerBasicConstraints.isCA())
			return false;

		// Check if correct key usage
		int ku = KeyUsageUtils.getKeyUsage(issuerCertificate);
		return (KeyUsage.keyCertSign & ku) > 0;
	}
}
