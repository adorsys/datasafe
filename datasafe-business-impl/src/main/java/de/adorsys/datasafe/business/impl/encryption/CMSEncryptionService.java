package de.adorsys.datasafe.business.impl.encryption;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;

import javax.crypto.SecretKey;

import org.bouncycastle.cms.CMSEnvelopedDataParser;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.KEKRecipientId;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKEKEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKEKRecipient;
import org.bouncycastle.cms.jcajce.JceKEKRecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import de.adorsys.datasafe.business.api.encryption.EncryptionService;
import de.adorsys.datasafe.business.api.encryption.EncryptionSpec;
import de.adorsys.datasafe.business.api.encryption.KeySource;
import de.adorsys.datasafe.business.api.encryption.SignatureSpec;
import de.adorsys.datasafe.business.impl.keystore.generator.ProviderUtils;
import lombok.SneakyThrows;

public class CMSEncryptionService implements EncryptionService {

	@Override
	@SneakyThrows
	public OutputStream buildEncryptionOutputStream(OutputStream outputStream, EncryptionSpec encryptionSpec) {

		CMSEnvelopedDataStreamGenerator gen = new CMSEnvelopedDataStreamGenerator();
		
		encryptionSpec.getSecretRecipients().stream().forEach(secretRecipient -> {
			gen.addRecipientInfoGenerator(
					new JceKEKRecipientInfoGenerator(secretRecipient.getKeyID().getValue().getBytes(), secretRecipient.getSecretKey()));
		});

		encryptionSpec.getPublicRecipients().stream().forEach(publicRecipient -> {
			gen.addRecipientInfoGenerator(
					new JceKeyTransRecipientInfoGenerator(publicRecipient.getKeyID().getValue().getBytes(), publicRecipient.getPublicKey()));
		});

		return gen.open(outputStream,
				new JceCMSContentEncryptorBuilder(encryptionSpec.getEncryptionAlgo()).setProvider(ProviderUtils.bcProvider).build());
	}

	@Override
	@SneakyThrows
	public OutputStream buildSignatureOutputStream(OutputStream outputStream, SignatureSpec signatureSpec) {

		ContentSigner contentSigner = new JcaContentSignerBuilder(signatureSpec.getSignatureAlgorithm())
				.setProvider(ProviderUtils.bcProvider).build(signatureSpec.getPrivateKey());
		BcDigestCalculatorProvider digestProvider = new BcDigestCalculatorProvider();
		SignerInfoGenerator signerInfoGenerator = new SignerInfoGeneratorBuilder(digestProvider).build(contentSigner,
				signatureSpec.getKeyId());
		CMSSignedDataStreamGenerator gen = new CMSSignedDataStreamGenerator();
		gen.addSignerInfoGenerator(signerInfoGenerator);
		return gen.open(outputStream, true);
	}

	@Override
	@SneakyThrows
	public InputStream buildDecryptionInputStream(InputStream inputStream, KeySource keySource) {
		RecipientInformationStore recipientInfos = new CMSEnvelopedDataParser(inputStream).getRecipientInfos();
		for (RecipientInformation recipientInfo : recipientInfos) {
			RecipientId recipientId = recipientInfo.getRID();
			switch (recipientId.getType()) {
				case RecipientId.keyTrans:
					PrivateKey privateKey = keySource
							.findPrivateKey(((KeyTransRecipientId) recipientId).getSubjectKeyIdentifier());
					return recipientInfo
							.getContentStream(transRececipient(privateKey)).getContentStream();
				case RecipientId.kek:
					SecretKey secretKey = keySource
							.findSecretKey(((KEKRecipientId) recipientId).getKeyIdentifier());
					return recipientInfo
							.getContentStream(kekRecipient(secretKey)).getContentStream();
			}
		}

		throw new IllegalStateException("No reci[pient found in store.");
	}

	@Override
	@SneakyThrows
	public InputStream buildVerifyicationInputStream(InputStream inputStream, KeySource keySource) {
		DigestCalculatorProvider digestCalculatorProvider = new JcaDigestCalculatorProviderBuilder()
				.setProvider(ProviderUtils.bcProvider).build();
		return new CMSSignedDataParser(digestCalculatorProvider, inputStream).getSignedContent().getContentStream();
	}
	

	private JceKEKRecipient kekRecipient(SecretKey secretKey) {
		return new JceKEKEnvelopedRecipient(secretKey).setProvider(ProviderUtils.bcProvider);
	}

	private JceKeyTransRecipient transRececipient(PrivateKey privateKey) {
		return new JceKeyTransEnvelopedRecipient(privateKey).setProvider(ProviderUtils.bcProvider);
	}
	
}
