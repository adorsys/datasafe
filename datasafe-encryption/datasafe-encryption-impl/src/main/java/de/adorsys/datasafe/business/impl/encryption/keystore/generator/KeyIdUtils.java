package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;

public class KeyIdUtils {

    public static AuthorityKeyIdentifier readAuthorityKeyIdentifier(X509CertificateHolder certHldr){
    	if(certHldr==null)return null;
        Extension ext = certHldr.getExtension(Extension.authorityKeyIdentifier);
        if (ext == null)return null;
        ASN1Encodable value = ext.getParsedValue();
        return AuthorityKeyIdentifier.getInstance(value);
    }
    
    public static SubjectKeyIdentifier readSubjectKeyIdentifier(X509CertificateHolder certHldr) {
    	if(certHldr==null)return null;
        Extension ext = certHldr.getExtension(Extension.subjectKeyIdentifier);
        if(ext==null) return null;
        return SubjectKeyIdentifier.getInstance(ext.getParsedValue());
	}
}
