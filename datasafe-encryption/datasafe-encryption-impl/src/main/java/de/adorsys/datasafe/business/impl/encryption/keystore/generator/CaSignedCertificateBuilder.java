package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Build a certificate based on information passed to this object.
 * 
 * The subjectSampleCertificate can be used as a types. The ca can create some of the fields manually thus
 * modifying suggestions provided by the sample certificate.
 * 
 * The object shall not be reused. After the build method is called, this object is not reusable.
 * 
 * @author fpo
 *
 */
public class CaSignedCertificateBuilder {

	private boolean createCaCert;

	private X500Name subjectDN;
	
	private boolean subjectOnlyInAlternativeName;

	private Integer notAfterInDays;
	private Integer notBeforeInDays = 0;

	private X509CertificateHolder issuerCertificate;

	private int keyUsage=-1;
	private boolean keyUsageSet = false;

	private GeneralNames subjectAltNames;
	
	private String signatureAlgo;
	
	private PublicKey subjectPublicKey;

	boolean dirty = false;

	public X509CertificateHolder build(PrivateKey issuerPrivatekey) {
		if(dirty)throw new IllegalStateException("Builder can not be reused");
		dirty=true;
		
		// AUtoselect algorithm
		if(StringUtils.isBlank(signatureAlgo)) {
			String algorithm = issuerPrivatekey.getAlgorithm();
			if(StringUtils.equalsAnyIgnoreCase("DSA", algorithm)){
				signatureAlgo = "SHA256withDSA";
			} else if (StringUtils.equals("RSA", algorithm)){
				signatureAlgo = "SHA256WithRSA";
			}
		}
		
		Date now = new Date();
		Date notAfter = notAfterInDays!=null?DateUtils.addDays(now, notAfterInDays):null;
		Date notBefore = notBeforeInDays!=null?DateUtils.addDays(now, notBeforeInDays):null;;

		List<KeyValue> notNullCheckList = ListOfKeyValueBuilder.newBuilder()
				.add("X509CertificateBuilder_missing_subject_DN", subjectDN)
				.add("X509CertificateBuilder_missing_subject_publicKey", subjectPublicKey)
				.add("X509CertificateBuilder_missing_validity_date_notBefore", notBefore)
				.add("X509CertificateBuilder_missing_validity_date_notAfter", notAfter)
				.build();
			
		List<String> errorKeys = BatchValidator.filterNull(notNullCheckList);
		if(errorKeys==null)errorKeys=new ArrayList<>();
		
		X500Name issuerDN = null;
		BasicConstraints basicConstraints = null;
		if(issuerCertificate==null){
			// Self signed certificate
			issuerDN = subjectDN;
			if(createCaCert){
				// self signed ca certificate
				basicConstraints = new BasicConstraints(true);
				// in ca case, subject must subject must be set
				subjectOnlyInAlternativeName = false;
			} else {
				// not a ca certificate
				basicConstraints = new BasicConstraints(false);
			}
		} else {			
			// check is issuerCertificate is ca certificate
			if(!CheckCaCertificate.isCaCertificate(issuerCertificate))errorKeys.add("X509CertificateBuilder_issuerCert_notCaCert");

			// prepare inputs
			issuerDN = issuerCertificate.getSubject();

			// ca signing another ca certificate
			if(createCaCert){
				// in ca case, subject must subject must be set
				subjectOnlyInAlternativeName = false;
				
				// ca certificate must carry a subject
				Extension basicConstraintsExtension = issuerCertificate.getExtension(X509Extension.basicConstraints);
				BasicConstraints issuerBasicConstraints = BasicConstraints.getInstance(basicConstraintsExtension.getParsedValue());
				BigInteger pathLenConstraint = issuerBasicConstraints.getPathLenConstraint();
				if(pathLenConstraint==null){
					pathLenConstraint = BigInteger.ONE;
				} else {
					pathLenConstraint = pathLenConstraint.add(BigInteger.ONE);
				}
				basicConstraints = new BasicConstraints(pathLenConstraint.intValue());
				
				resetKeyUsage();
				for (int keyUsage : KeyUsageUtils.getCaKeyUsages()) withKeyUsage(keyUsage);
			} else {
				// ca issuing a simple certificate
				basicConstraints = new BasicConstraints(false);
			}
		}
		
		BigInteger serial = SerialNumberGenerator.uniqueSerial();
		
		X509v3CertificateBuilder v3CertGen = null;
		if(subjectOnlyInAlternativeName && subjectAltNames!=null){
			// Remove missing sobject error.
			errorKeys.remove("X509CertificateBuilder_missing_subject_DN");
			subjectDN = new X500Name("cn=");
		}
		
		if(!errorKeys.isEmpty()){
			throw new IllegalArgumentException("Fields can not be null: " + errorKeys);
		}
		
		v3CertGen = new JcaX509v3CertificateBuilder(issuerDN, serial, notBefore, notAfter, subjectDN,subjectPublicKey);
		JcaX509ExtensionUtils extUtils = V3CertificateUtils.getJcaX509ExtensionUtils();
		
		try {
			v3CertGen.addExtension(X509Extension.basicConstraints,true, basicConstraints);
			
			v3CertGen.addExtension(X509Extension.subjectKeyIdentifier,false, 
					extUtils.createSubjectKeyIdentifier(subjectPublicKey));
			
			if(issuerCertificate==null){
				v3CertGen.addExtension(X509Extension.authorityKeyIdentifier,false,
						extUtils.createAuthorityKeyIdentifier(subjectPublicKey));
			} else {
				v3CertGen.addExtension(X509Extension.authorityKeyIdentifier,false,
						extUtils.createAuthorityKeyIdentifier(issuerCertificate));
			}
			
			if(keyUsageSet){
				v3CertGen.addExtension(X509Extension.keyUsage,
						true, new KeyUsage(this.keyUsage));
			}

			// complex rules for subject alternative name. See rfc5280
			if(subjectAltNames!=null){
				if(subjectOnlyInAlternativeName){
					v3CertGen.addExtension(X509Extension.subjectAlternativeName, true, subjectAltNames);
				} else {
					v3CertGen.addExtension(X509Extension.subjectAlternativeName, false, subjectAltNames);
				}
			}
		} catch (CertIOException e) {
			throw new IllegalStateException(e);
		}

		ContentSigner signer = V3CertificateUtils.getContentSigner(issuerPrivatekey,signatureAlgo);

		return v3CertGen.build(signer);

	}
	
	private void copyKeyUsage(X509CertificateHolder issuerCertificate) {
		int ku = KeyUsageUtils.getKeyUsage(issuerCertificate);
		if(ku!=-1)withKeyUsage(ku);
	}

	public CaSignedCertificateBuilder withSignatureAlgo(String signatureAlgo) {
		this.signatureAlgo = signatureAlgo;
		return this;
	}

	public CaSignedCertificateBuilder withCa(boolean ca) {
		this.createCaCert = ca;
		return this;
	}

	public CaSignedCertificateBuilder withSubjectDN(X500Name subjectDN) {
		this.subjectDN = subjectDN;
		return this;
	}

	public CaSignedCertificateBuilder withSubjectPublicKey(PublicKey subjectPublicKey) {
		this.subjectPublicKey = subjectPublicKey;
		return this;
	}

	
	public CaSignedCertificateBuilder withNotAfterInDays(Integer notAfterInDays) {
		this.notAfterInDays = notAfterInDays;
		return this;
	}

	public CaSignedCertificateBuilder withNotBeforeInDays(Integer notBeforeInDays) {
		this.notBeforeInDays = notBeforeInDays;
		return this;
	}

	public CaSignedCertificateBuilder withIssuerCertificate(
			X509CertificateHolder issuerCertificate) {
		// Validate Issuer Certificate
		if(!CheckCaCertificate.isCaCertificate(issuerCertificate)) throw new IllegalArgumentException("Invalid issuer certificate");
		this.issuerCertificate = issuerCertificate;
		return this;
	}
	
	public CaSignedCertificateBuilder resetKeyUsage() {
		keyUsageSet = false;
		this.keyUsage = -1;
		return this;
	}

	public CaSignedCertificateBuilder withKeyUsage(int keyUsage) {
		if(keyUsageSet){
			this.keyUsage=this.keyUsage|keyUsage;
		} else {
			this.keyUsage=keyUsage;
			keyUsageSet=true;
		}
		return this;
	}

	public CaSignedCertificateBuilder withSubjectAltNames(GeneralNames subjectAltNames) {
		if(this.subjectAltNames==null){
			this.subjectAltNames = new GeneralNames(subjectAltNames.getNames());
		} else {
			ArrayList<GeneralName> nameList = new ArrayList<GeneralName>();
			GeneralName[] names1 = this.subjectAltNames.getNames();
			for (GeneralName generalName : names1) {
				if(!nameList.contains(generalName))
					nameList.add(generalName);
			}
			GeneralName[] names2 = subjectAltNames.getNames();
			for (GeneralName generalName : names2) {
				if(!nameList.contains(generalName))
					nameList.add(generalName);
			}
			GeneralName[] names = nameList.toArray(new GeneralName[nameList.size()]);
			this.subjectAltNames = new GeneralNames(names);
		}
		return this;
	}
}
