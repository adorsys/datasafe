package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509CertificateHolder;

@UtilityClass
public class KeyUsageUtils {

	public static int getKeyUsage(X509CertificateHolder issuerCertificate) {
		Extension keyUsageExtension = issuerCertificate.getExtension(X509Extension.keyUsage);
		return extractKeyUsage(keyUsageExtension);
	}
	
	private static int extractKeyUsage(Extension keyUsageExtension){
		if(keyUsageExtension!=null){
			KeyUsage ku = KeyUsage.getInstance(keyUsageExtension.getParsedValue().toASN1Primitive());
            return ku.getBytes()[0] & 0xff;
		}
		return -1;
	}

	public static int[] getCaKeyUsages(){
		return new int[] {KeyUsage.keyCertSign};
	}
}
