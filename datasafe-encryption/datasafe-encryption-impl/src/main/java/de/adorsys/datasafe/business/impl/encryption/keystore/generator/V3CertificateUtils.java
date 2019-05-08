package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import lombok.experimental.UtilityClass;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

@UtilityClass
public class V3CertificateUtils {

	public static X509Certificate getX509JavaCertificate(X509CertificateHolder holder) {
		try {
			return new JcaX509CertificateConverter().setProvider(ProviderUtils.bcProvider).getCertificate(holder);
		} catch (CertificateException e) {
			throw new IllegalStateException(e);
		}
	}

	public static X509Certificate getX509JavaCertificate(org.bouncycastle.asn1.x509.Certificate certificate) {
		return getX509JavaCertificate(new X509CertificateHolder(certificate));
	}

	public static PublicKey extractPublicKey(X509CertificateHolder subjectCertificate) {
		try {
			return PublicKeyUtils.getPublicKey(subjectCertificate, ProviderUtils.bcProvider);
		} catch (InvalidKeySpecException e) {
			throw new IllegalStateException(e);
		}
	}

	public static JcaX509ExtensionUtils getJcaX509ExtensionUtils() {
		try {
			return new JcaX509ExtensionUtils();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	public static ContentSigner getContentSigner(PrivateKey privatekey, String algo) {
		try {
			return new JcaContentSignerBuilder(algo).setProvider(ProviderUtils.bcProvider).build(privatekey);
		} catch (OperatorCreationException e) {
			throw new IllegalStateException(e);
		}
	}
}
