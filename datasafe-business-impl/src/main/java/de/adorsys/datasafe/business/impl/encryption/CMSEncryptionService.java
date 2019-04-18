package de.adorsys.datasafe.business.impl.encryption;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;

import de.adorsys.datasafe.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.keystore.types.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.impl.cmsencryption.exceptions.AsymmetricEncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.adorsys.datasafe.business.api.encryption.EncryptionService;
import lombok.SneakyThrows;

@Slf4j
public class CMSEncryptionService implements EncryptionService {

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

}
