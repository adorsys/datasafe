package de.adorsys.docusafe2.business.impl.keystore;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class V3CertificateUtils {

	public static X509Certificate getX509JavaCertificate(X509CertificateHolder holder) {
		try {
			return new JcaX509CertificateConverter().setProvider(ProviderUtils.bcProvider).getCertificate(holder);
		} catch (CertificateException e) {
			throw new IllegalStateException(e);
		}
	}

	public static org.bouncycastle.asn1.x509.Certificate getX509BCCertificate(X509CertificateHolder certHolder) {
		return certHolder.toASN1Structure();
	}

	public static org.bouncycastle.asn1.x509.Certificate getX509BCCertificate(Certificate certificate) {
		X509CertificateHolder x509CertificateHolder = getX509CertificateHolder(certificate);
		return getX509BCCertificate(x509CertificateHolder);
	}

	public static X509CertificateHolder getX509CertificateHolder(Certificate certificate) {
		try {
			return new X509CertificateHolder(certificate.getEncoded());
		} catch (CertificateEncodingException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static X509CertificateHolder getX509CertificateHolder(org.bouncycastle.asn1.x509.Certificate certificate) {
		return new X509CertificateHolder(certificate);
	}

	public static X509Certificate getX509JavaCertificate(org.bouncycastle.asn1.x509.Certificate certificate) {
		return getX509JavaCertificate(new X509CertificateHolder(certificate));
	}

	public static X509Certificate[] convert(org.bouncycastle.asn1.x509.Certificate... certificates) {
		X509Certificate[] list = new X509Certificate[certificates.length];
		for (int i = 0; i < certificates.length; i++) {
			list[i] = getX509JavaCertificate(certificates[i]);
		}
		return list;
	}

	public static final PublicKey extractPublicKey(X509CertificateHolder subjectCertificate) {
		try {
			return PublicKeyUtils.getPublicKey(subjectCertificate, ProviderUtils.bcProvider);
		} catch (InvalidKeySpecException e) {
			throw new IllegalStateException(e);
		}
	}

	public static JcaX509ExtensionUtils getJcaX509ExtensionUtils() {
		try {
			return new JcaX509ExtensionUtils();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	public static ContentSigner getContentSigner(PrivateKey privatekey, String algo) {
		try {
			return new JcaContentSignerBuilder(algo).setProvider(ProviderUtils.bcProvider).build(privatekey);
		} catch (OperatorCreationException e) {
			throw new IllegalStateException(e);
		}
	}

	public static List<List<X509CertificateHolder>> splitCertList(List<X509CertificateHolder> certList) {
		LinkedList<X509CertificateHolder> currentList = new LinkedList<X509CertificateHolder>();
		List<List<X509CertificateHolder>> result = new ArrayList<List<X509CertificateHolder>>();
		;
		for (X509CertificateHolder signed : certList) {
			if (currentList.isEmpty()) {
				currentList.add(signed);
				continue;
			}
			X509CertificateHolder signer = currentList.getLast();
			if (CheckCaCertificate.isSigingCertificate(signed, signer)) {
				currentList.add(signer);
				continue;
			}
			if (!currentList.isEmpty()) {
				result.add(currentList);
				currentList = new LinkedList<X509CertificateHolder>();
			}
		}
		return result;
	}

	public static CertStore createCertStore(X509CertificateHolder... certs) {
		return createCertStore(Arrays.asList(certs));
	}

	public static CertStore createCertStore(List<X509CertificateHolder> certs) {
		try {
			if (certs.isEmpty())
				return null;
			JcaCertStoreBuilder certStoreBuilder = new JcaCertStoreBuilder().setProvider(ProviderUtils.bcProvider);
			for (X509CertificateHolder signerCertificate : certs) {
				certStoreBuilder.addCertificate(signerCertificate);
			}
			return certStoreBuilder.build();
		} catch (GeneralSecurityException e) {
			return null;
		}

	}

	public static List<X509Certificate> convert(Certificate[] certificateChain) {
		X509Certificate[] list = new X509Certificate[certificateChain.length];
		for (int i = 0; i < certificateChain.length; i++) {
			list[i] = (X509Certificate) certificateChain[i];
		}
		return Arrays.asList(list);
	}
}
