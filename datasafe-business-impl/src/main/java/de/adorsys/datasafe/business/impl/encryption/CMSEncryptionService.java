package de.adorsys.datasafe.business.impl.encryption;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.util.Iterator;
import java.util.Optional;

import javax.crypto.SecretKey;

import de.adorsys.datasafe.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.keystore.types.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.DocumentContent;
import de.adorsys.datasafe.business.impl.cmsencryption.exceptions.AsymmetricEncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKEKEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKEKRecipient;
import org.bouncycastle.cms.jcajce.JceKEKRecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import de.adorsys.datasafe.business.api.encryption.EncryptionService;
import de.adorsys.datasafe.business.api.encryption.EncryptionSpec;
import de.adorsys.datasafe.business.api.encryption.KeySource;
import de.adorsys.datasafe.business.api.encryption.SignatureSpec;
import de.adorsys.datasafe.business.impl.keystore.generator.ProviderUtils;
import lombok.SneakyThrows;

@Slf4j
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
    public OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, PublicKeyIDWithPublicKey publicKeyIdWithPublicKey) {
        CMSEnvelopedDataStreamGenerator gen = new CMSEnvelopedDataStreamGenerator();

        gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(
                publicKeyIdWithPublicKey.getKeyID().getValue().getBytes(),
                publicKeyIdWithPublicKey.getPublicKey()
        ));

        return gen.open(dataContentStream, new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build());
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

        throw new IllegalStateException("No recipient found in store.");
    }

    @Override
    @SneakyThrows
    public InputStream buildDecryptionInputStream(InputStream inputStream, KeyStoreAccess keyStoreAccess) {
        RecipientInformationStore recipientInfoStore = new CMSEnvelopedDataParser(inputStream).getRecipientInfos();

        if(recipientInfoStore.size() == 0) {
            throw new AsymmetricEncryptionException("CMS Envelope doesn't contain recipients");
        }
        if(recipientInfoStore.size() > 1) {
            throw new AsymmetricEncryptionException("Programming error. Handling of more that one recipient not done yet");
        }
        RecipientInformation recipientInfo = recipientInfoStore.getRecipients().stream().findFirst().get();
        KeyTransRecipientId recipientId = (KeyTransRecipientId) recipientInfo.getRID();
        byte[] subjectKeyIdentifier = recipientId.getSubjectKeyIdentifier();
        String keyId = new String(subjectKeyIdentifier);
        log.debug("Private key ID from envelope: {}", keyId);

        PrivateKey privateKey = (PrivateKey) keyStoreAccess.getKeyStore().getKey(
                keyId,
                keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray()
        );

        return recipientInfo.getContentStream(new JceKeyTransEnvelopedRecipient(privateKey)).getContentStream();
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
