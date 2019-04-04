package de.adorsys.docusafe2.keystore.impl;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.crmf.CertTemplate;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class KeyIdUtils {
    
    public static byte[] createPublicKeyIdentifierAsByteString(X509CertificateHolder certHldr){
    	return createPublicKeyIdentifierAsByteString(certHldr.getSubjectPublicKeyInfo());
    }    
    public static String createPublicKeyIdentifierAsString(X509CertificateHolder certHldr){
    	return hexEncode(createPublicKeyIdentifierAsByteString(certHldr));
    }
    public static SubjectKeyIdentifier createPublicKeyIdentifier(PublicKey subjectPublicKey){
		JcaX509ExtensionUtils extUtils;
		try {
			extUtils = new JcaX509ExtensionUtils();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
		return extUtils.createSubjectKeyIdentifier(subjectPublicKey);
    }
    public static SubjectKeyIdentifier createPublicKeyIdentifier(SubjectPublicKeyInfo publicKeyInfo){
    	if(publicKeyInfo==null) return null;
		JcaX509ExtensionUtils extUtils;
		try {
			extUtils = new JcaX509ExtensionUtils();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
		return extUtils.createSubjectKeyIdentifier(publicKeyInfo);
    }
    public static byte[] createPublicKeyIdentifierAsByteString(PublicKey subjectPublicKey){
    	SubjectKeyIdentifier subjectKeyIdentifier = createPublicKeyIdentifier(subjectPublicKey);
		return subjectKeyIdentifier.getKeyIdentifier();
    }
    public static byte[] createPublicKeyIdentifierAsByteString(SubjectPublicKeyInfo publicKeyInfo){
    	SubjectKeyIdentifier subjectKeyIdentifier = createPublicKeyIdentifier(publicKeyInfo);
    	return subjectKeyIdentifier.getKeyIdentifier();
    }

    public static String createPublicKeyIdentifierAsString(PublicKey subjectPublicKey){
    	return hexEncode(createPublicKeyIdentifierAsByteString(subjectPublicKey));
    }

    public static String createPublicKeyIdentifierAsString(SubjectPublicKeyInfo publicKeyInfo){
    	return hexEncode(createPublicKeyIdentifierAsByteString(publicKeyInfo));
    }
    

    public static AuthorityKeyIdentifier readAuthorityKeyIdentifier(X509CertificateHolder certHldr){
    	if(certHldr==null)return null;
        Extension ext = certHldr.getExtension(Extension.authorityKeyIdentifier);
        if (ext == null)return null;
        ASN1Encodable value = ext.getParsedValue();
        return AuthorityKeyIdentifier.getInstance(value);
    }
    public static byte[] readAuthorityKeyIdentifierAsByteString(AuthorityKeyIdentifier authorityKeyIdentifier){
    	return authorityKeyIdentifier==null?null:authorityKeyIdentifier.getKeyIdentifier();
    }    
    public static byte[] readAuthorityKeyIdentifierAsByteString(X509CertificateHolder certHldr){
    	return readAuthorityKeyIdentifierAsByteString(readAuthorityKeyIdentifier(certHldr));
    }
    public static String readAuthorityKeyIdentifierAsString(X509CertificateHolder certHldr){
    	return hexEncode(readAuthorityKeyIdentifierAsByteString(certHldr));
    }
	public static AuthorityKeyIdentifier readAuthorityKeyIdentifier(CertTemplate certTemplate) {
		if(certTemplate==null)return null;
    	Extensions extensions = certTemplate.getExtensions();
    	if(extensions==null)return null;
    	Extension ext = extensions.getExtension(Extension.authorityKeyIdentifier);
    	if(ext==null) return null;
        return AuthorityKeyIdentifier.getInstance(ext.getParsedValue());
	}
	public static String authorityKeyIdentifierToString(AuthorityKeyIdentifier authorityKeyIdentifier){
    	return authorityKeyIdentifier==null?null:hexEncode(authorityKeyIdentifier.getKeyIdentifier());
	}
    public static String authoritySerialNumberToString(AuthorityKeyIdentifier authorityKeyIdentifier){
    	if(authorityKeyIdentifier==null) return null;
    	BigInteger authorityCertSerialNumber = authorityKeyIdentifier.getAuthorityCertSerialNumber();
    	if(authorityCertSerialNumber==null) return null;
    	return authorityCertSerialNumber.toString(16).toUpperCase();
    }
    
    public static SubjectKeyIdentifier readSubjectKeyIdentifier(X509CertificateHolder certHldr) {
    	if(certHldr==null)return null;
        Extension ext = certHldr.getExtension(Extension.subjectKeyIdentifier);
        if(ext==null) return null;
        return SubjectKeyIdentifier.getInstance(ext.getParsedValue());
	}
    public static byte[] readSubjectKeyIdentifierAsByteString(X509CertificateHolder certHldr){
        SubjectKeyIdentifier subjectKeyIdentifier = readSubjectKeyIdentifier(certHldr);
    	return subjectKeyIdentifier==null?null:subjectKeyIdentifier.getKeyIdentifier();
    }
    public static String readSubjectKeyIdentifierAsString(X509CertificateHolder certHldr){
    	return KeyIdUtils.hexEncode(readSubjectKeyIdentifierAsByteString(certHldr));
    }
    public static SubjectKeyIdentifier readSubjectKeyIdentifier(CertTemplate certTemplate) {
    	if(certTemplate==null)return null;
    	Extensions extensions = certTemplate.getExtensions();
    	if(extensions==null)return null;
    	Extension ext = extensions.getExtension(Extension.subjectKeyIdentifier);
    	if(ext==null) return null;
        return SubjectKeyIdentifier.getInstance(ext.getParsedValue());
	}
    
	public static String subjectKeyIdentifierToString(SubjectKeyIdentifier subjectKeyIdentifier) {
		return subjectKeyIdentifier==null?null:KeyIdUtils.hexEncode(subjectKeyIdentifier.getKeyIdentifier());
	}
    
	public static String hexEncode(byte[] keyIdentifier){
		if(keyIdentifier==null) return null;
    	return new String(Hex.encode(keyIdentifier)).toUpperCase();
    }

	public static String hexEncode(ASN1OctetString octetString){
		if(octetString==null) return null;
    	return new String(Hex.encode(octetString.getOctets())).toUpperCase();
    }
	
    public static String readSerialNumberAsString(X509CertificateHolder certHldr){
    	if(certHldr==null) return null;
    	BigInteger serialNumber = certHldr.getSerialNumber();
    	return serialNumber.toString(16).toUpperCase();
    }

    public static String hexEncode(ASN1Integer value){
    	if(value==null) return null;
    	return value.getPositiveValue().toString(16);
    }
//    
//    public static String readEndEntityIdentifier(X509CertificateHolder certHldr){
//    	X500Name subjectDN = X500NameHelper.readSubjectDN(certHldr);
//    	if(subjectDN==null) return null;
//    	return X500NameHelper.readUniqueIdentifier(subjectDN);
//    }
}
