package de.adorsys.docusafe2.services;

import de.adorsys.docusafe2.exceptions.AsymmetricEncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.OutputEncryptor;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Iterator;

@Slf4j
public class CMSEncryptionServiceImpl implements CMSEncryptionService {

    @Override
    public byte[] encrypt(byte[] data, PublicKey publicKey, byte[] publicKeyId) {
        try {
            CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
            JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(publicKeyId, publicKey);
            cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);
            CMSTypedData msg = new CMSProcessableByteArray(data);
            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build();
            return cmsEnvelopedDataGenerator.generate(msg, encryptor).getEncoded();
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, KeyStore keyStore, char[] keyStorePass) {
        try {
            CMSEnvelopedData cmsEnvelopedData = new CMSEnvelopedData(encryptedData);

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

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyId, keyStorePass);
            JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(privateKey);

            return recipientInfo.getContent(recipient);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
