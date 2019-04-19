package de.adorsys.datasafe.business.impl.cmsencryption.services;

import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyID;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.impl.cmsencryption.exceptions.AsymmetricEncryptionException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Cryptographic message syntax document encoder/decoder - see
 * @see <a href=https://en.wikipedia.org/wiki/Cryptographic_Message_Syntax">CMS wiki</a>
 */
@Slf4j
public class CMSEncryptionServiceImpl implements CMSEncryptionService {

    @Inject
    public CMSEncryptionServiceImpl() {
    }
    
    @Override
    @SneakyThrows
    public OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, PublicKey publicKey, KeyID publicKeyID) {
        CMSEnvelopedDataStreamGenerator gen = new CMSEnvelopedDataStreamGenerator();

        gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(
        		publicKeyID.getValue().getBytes(),
                publicKey
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
