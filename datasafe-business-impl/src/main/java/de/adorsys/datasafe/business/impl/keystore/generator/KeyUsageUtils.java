package de.adorsys.datasafe.business.impl.keystore.generator;

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

	public static final boolean hasAllKeyUsage(X509CertificateHolder holder, int... keyUsageBits){
    	Extension extension = holder.getExtension(X509Extension.keyUsage);
    	return hasAllKeyUsage(extension, keyUsageBits);
    }
	public static final boolean hasAllKeyUsage(CertTemplate certTemplate, int... keyUsageBits){
		Extensions extensions = certTemplate.getExtensions();
    	Extension extension = extensions.getExtension(X509Extension.keyUsage);
    	return hasAllKeyUsage(extension, keyUsageBits);
    }
	private static final boolean hasAllKeyUsage(Extension extension, int... keyUsageBits){
        if (extension != null){
            KeyUsage ku = KeyUsage.getInstance(extension.getParsedValue());
            int bits = ku.getBytes()[0] & 0xff;
            // no bit, false
            if(keyUsageBits.length<=0) return false;
            
            // check all bits. Assume true.
            for (int keyUsageBit : keyUsageBits) {
            	if((bits & keyUsageBit) != keyUsageBit) return false;
			}
            return true;
        } else {
        	// no extensions, no key usage, fine
            if(keyUsageBits.length<=0) return true;
            
            // else false
        	return false;
        }
	}
	public static final boolean hasAnyKeyUsage(X509CertificateHolder holder, int... keyUsageBits){
        // no bit, true
        if(keyUsageBits.length<=0) return true;
        Extension extension = holder.getExtension(X509Extension.keyUsage);
        return hasAnyKeyUsage(extension, keyUsageBits);
    }
	public static final boolean hasAnyKeyUsage(CertTemplate certTemplate, int... keyUsageBits){
        // no bit, true
        if(keyUsageBits.length<=0) return true;
        Extensions extensions = certTemplate.getExtensions();
        Extension extension = extensions.getExtension(X509Extension.keyUsage);
        return hasAnyKeyUsage(extension, keyUsageBits);
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
	public static final int[] getStdKeyUsages(){
		return new int[]{KeyUsage.digitalSignature, KeyUsage.nonRepudiation,KeyUsage.keyEncipherment};
	}

	public static final int[] getCaKeyUsages(){
		return new int[]{KeyUsage.keyCertSign};
	}
}
