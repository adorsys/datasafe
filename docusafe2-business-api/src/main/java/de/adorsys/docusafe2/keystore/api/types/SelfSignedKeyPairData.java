package de.adorsys.docusafe2.keystore.api.types;

import org.bouncycastle.cert.X509CertificateHolder;

import java.security.KeyPair;

public class SelfSignedKeyPairData {
	
	private final KeyPair keyPair;
	
	private final X509CertificateHolder subjectCert;

	public SelfSignedKeyPairData(KeyPair keyPair, X509CertificateHolder subjectCert) {
		super();
		this.keyPair = keyPair;
		this.subjectCert = subjectCert;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public X509CertificateHolder getSubjectCert() {
		return subjectCert;
	}

}
