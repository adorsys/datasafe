package de.adorsys.docusafe2.business.impl.keystore.generator;

import lombok.Builder;
import lombok.Getter;
import org.bouncycastle.cert.X509CertificateHolder;

import javax.security.auth.callback.CallbackHandler;

@Getter
public class TrustedCertData extends KeyEntryData implements TrustedCertEntry {

	private final X509CertificateHolder certificate;

	@Builder
	private TrustedCertData(CallbackHandler passwordSource, String alias, X509CertificateHolder certificate) {
		super(passwordSource, alias);
		this.certificate = certificate;
	}
}
