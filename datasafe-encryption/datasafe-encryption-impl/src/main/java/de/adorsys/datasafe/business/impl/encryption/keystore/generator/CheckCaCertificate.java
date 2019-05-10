package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509CertificateHolder;

@UtilityClass
public class CheckCaCertificate {

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
