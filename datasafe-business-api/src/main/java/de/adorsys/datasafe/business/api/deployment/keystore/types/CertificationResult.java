package de.adorsys.datasafe.business.api.deployment.keystore.types;

import org.bouncycastle.cert.X509CertificateHolder;

import java.util.List;

/**
 * Hold key certificates.
 * 
 * @author fpo
 *
 */
public class CertificationResult {

	private final X509CertificateHolder subjectCert;
	
	private final List<X509CertificateHolder> issuerChain;

	public CertificationResult(X509CertificateHolder subjectCert, List<X509CertificateHolder> issuerChain) {
		super();
		this.subjectCert = subjectCert;
		this.issuerChain = issuerChain;
	}

	public X509CertificateHolder getSubjectCert() {
		return subjectCert;
	}

	public List<X509CertificateHolder> getIssuerChain() {
		return issuerChain;
	}

}
