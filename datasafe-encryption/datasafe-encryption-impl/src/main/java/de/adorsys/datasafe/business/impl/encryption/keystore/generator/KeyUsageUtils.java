package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import org.bouncycastle.asn1.crmf.CertTemplate;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509CertificateHolder;

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

	private static final boolean hasAnyKeyUsage(Extension extension, int... keyUsageBits){
        if (extension != null){
        	KeyUsage ku = KeyUsage.getInstance(extension.getParsedValue());
            int bits = ku.getBytes()[0] & 0xff;
            // check all bits. Assume true.
            for (int keyUsageBit : keyUsageBits) {
            	if((bits & keyUsageBit) == keyUsageBit) return true;
			}
        } 
        // else false
    	return false;
	}

	public static final int[] getCaKeyUsages(){
		return new int[]{KeyUsage.keyCertSign};
	}
}
