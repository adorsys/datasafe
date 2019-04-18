package de.adorsys.datasafe.business.impl.cmsencryption.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Iterator;

import javax.inject.Inject;

import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSEnvelopedDataParser;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OutputEncryptor;

import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyID;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.types.DocumentContent;
import de.adorsys.datasafe.business.impl.cmsencryption.exceptions.AsymmetricEncryptionException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
    public CMSEnvelopedData encrypt(DocumentContent data, PublicKey publicKey, KeyID publicKeyId) {
        CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
        JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(
                publicKeyId.getValue().getBytes(),
                publicKey
        );

        cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);
        CMSTypedData msg = new CMSProcessableByteArray(data.getValue());

        OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build();

        return cmsEnvelopedDataGenerator.generate(msg, encryptor);
    }

    @Override
    @SneakyThrows
    public DocumentContent decrypt(CMSEnvelopedData cmsEnvelopedData, KeyStoreAccess keyStoreAccess) {
        RecipientInformationStore recipients = cmsEnvelopedData.getRecipientInfos();

        Iterator<RecipientInformation> recipientInformationIterator = recipients.getRecipients().iterator();

        if (!recipientInformationIterator.hasNext()) {
            throw new AsymmetricEncryptionException("CMS Envelope doesn't contain recipients");
        }

        RecipientInformation recipientInfo = recipientInformationIterator.next();

        if (recipientInformationIterator.hasNext()) {
            throw new AsymmetricEncryptionException("PROGRAMMING ERROR. HANDLE OF MORE THAN ONE RECIPIENT NOT DONE YET");
        }

        KeyTransRecipientId recipientId = (KeyTransRecipientId) recipientInfo.getRID();
        byte[] subjectKeyIdentifier = recipientId.getSubjectKeyIdentifier();
        String keyId = new String(subjectKeyIdentifier);
        log.debug("Private key ID from envelope: {}", keyId);

        PrivateKey privateKey = (PrivateKey) keyStoreAccess.getKeyStore().getKey(
                keyId,
                keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray()
        );

        JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(privateKey);

        return new DocumentContent(recipientInfo.getContent(recipient));
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
