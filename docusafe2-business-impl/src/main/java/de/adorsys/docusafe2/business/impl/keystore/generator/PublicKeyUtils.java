package de.adorsys.docusafe2.business.impl.keystore.generator;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class PublicKeyUtils {

	public static PublicKey getPublicKey(X509CertificateHolder certificateHolder, Provider provider) throws InvalidKeySpecException {
		if(certificateHolder==null) return null;
    	SubjectPublicKeyInfo subjectPublicKeyInfo = certificateHolder.getSubjectPublicKeyInfo();
		X509EncodedKeySpec x509EncodedKeySpec;
		try {
			x509EncodedKeySpec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
        try {
			return KeyFactory.getInstance(subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId(), provider).generatePublic(x509EncodedKeySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	public static PublicKey getPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo, Provider provider) throws InvalidKeySpecException {
		if(subjectPublicKeyInfo==null) return null;
		X509EncodedKeySpec x509EncodedKeySpec;
		try {
			x509EncodedKeySpec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
        try {
			return KeyFactory.getInstance(subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId(), provider).generatePublic(x509EncodedKeySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	public static PublicKey getPublicKeySilent(X509CertificateHolder certificateHolder, Provider provider) {
		if(certificateHolder==null) return null;
    	SubjectPublicKeyInfo subjectPublicKeyInfo = certificateHolder.getSubjectPublicKeyInfo();
		X509EncodedKeySpec x509EncodedKeySpec;
		try {
			x509EncodedKeySpec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
        try {
			return KeyFactory.getInstance(subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId(), provider).generatePublic(x509EncodedKeySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (InvalidKeySpecException e) {
			throw new IllegalStateException(e);
		}
	}
}
