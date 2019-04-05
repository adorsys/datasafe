package de.adorsys.docusafe2.business.impl.keystore.generator;

import de.adorsys.docusafe2.business.impl.keystore.generator.V3CertificateUtils;
import org.bouncycastle.cert.X509CertificateHolder;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertVerifier {

	public static boolean verify(X509CertificateHolder signed, X509CertificateHolder signer) {
		X509Certificate signedJavaCertificate = V3CertificateUtils.getX509JavaCertificate(signed);
		X509Certificate signerJavaCertificate = V3CertificateUtils.getX509JavaCertificate(signer);
		try {
			signedJavaCertificate.verify(signerJavaCertificate.getPublicKey());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isValid(X509CertificateHolder certificateHolder) {
		Date notBefore = certificateHolder.getNotBefore();
		Date notAfter = certificateHolder.getNotAfter();
		Date now = new Date();
		return now.after(notBefore) && now.before(notAfter);
	}


	public static boolean isValid(X509CertificateHolder certificateHolder, Date notBefore, Date notAfter) {
		Date certNotBefore = certificateHolder.getNotBefore();
		Date certNotAfter = certificateHolder.getNotAfter();
		boolean before = true;
		boolean after = true;
		if (notBefore != null)
			before = (certNotBefore != null) && (notBefore.equals(certNotBefore) || notBefore.after(certNotBefore));
		if (notAfter != null)
			after = (certNotAfter != null) && (notAfter.equals(certNotAfter) || notAfter.before(certNotAfter));

		return before && after;
	}

	public static boolean isSelfSigned(X509CertificateHolder certHolder) {
		return isSelfSigned(V3CertificateUtils.getX509JavaCertificate(certHolder));
	}

	public static boolean isSelfSigned(java.security.cert.Certificate certificate) {
		try {
			certificate.verify(certificate.getPublicKey());
			return true;
		} catch (InvalidKeyException e) {
			return false;
		} catch (CertificateException e) {
			return false;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (NoSuchProviderException e) {
			throw new IllegalStateException(e);
		} catch (SignatureException e) {
			return false;
		}
	}
}
