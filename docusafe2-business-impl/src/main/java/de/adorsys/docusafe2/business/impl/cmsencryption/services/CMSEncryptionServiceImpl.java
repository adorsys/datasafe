package de.adorsys.docusafe2.business.impl.cmsencryption.services;

import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.docusafe2.business.api.cmsencryption.CMSEncryptionService;
import de.adorsys.docusafe2.business.api.keystore.types.KeyID;
import de.adorsys.docusafe2.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.docusafe2.business.api.types.DocumentContent;
import de.adorsys.docusafe2.business.impl.cmsencryption.exceptions.AsymmetricEncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OutputEncryptor;

import javax.inject.Inject;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Iterator;

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
    public CMSEnvelopedData encrypt(DocumentContent data, PublicKey publicKey, KeyID publicKeyId) {
        try {
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
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public DocumentContent decrypt(CMSEnvelopedData cmsEnvelopedData, KeyStoreAccess keyStoreAccess) {
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

            PrivateKey privateKey = (PrivateKey) keyStoreAccess.getKeyStore().getKey(
                    keyId,
                    keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray()
            );

            JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(privateKey);

            return new DocumentContent(recipientInfo.getContent(recipient));
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
