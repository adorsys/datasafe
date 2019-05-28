package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@UtilityClass
public class V3CertificateUtils {

	@SneakyThrows
	public static X509Certificate getX509JavaCertificate(X509CertificateHolder holder) {
			return new JcaX509CertificateConverter().setProvider(ProviderUtils.bcProvider).getCertificate(holder);
	}

	@SneakyThrows
	public static JcaX509ExtensionUtils getJcaX509ExtensionUtils() {
			return new JcaX509ExtensionUtils();
	}

	@SneakyThrows
	public static ContentSigner getContentSigner(PrivateKey privatekey, String algo) {
			return new JcaContentSignerBuilder(algo).setProvider(ProviderUtils.bcProvider).build(privatekey);
	}
}
