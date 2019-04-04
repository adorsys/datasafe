package de.adorsys.docusafe2.business.impl.cmsencryption.services;

import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.docusafe2.business.api.cmsencryption.CMSEncryptionService;
import de.adorsys.docusafe2.business.impl.cmsencryption.exceptions.AsymmetricEncryptionException;
import de.adorsys.docusafe2.business.api.cmsencryption.types.DocumentContent;
import lombok.extern.slf4j.Slf4j;
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
    public CMSEnvelopedData encrypt(DocumentContent data, PublicKey publicKey, byte[] publicKeyId) {
        try {
            CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
            JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(publicKeyId, publicKey);
            cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);
            CMSTypedData msg = new CMSProcessableByteArray(data.getValue());
            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build();
            return cmsEnvelopedDataGenerator.generate(msg, encryptor);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public DocumentContent decrypt(CMSEnvelopedData cmsEnvelopedData, KeyStore keyStore, char[] keyStorePass) {
        try {

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

            return new DocumentContent(recipientInfo.getContent(recipient));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
