package de.adorsys.datasafe.encrypiton.impl.cmsencryption.decryptors;

import lombok.SneakyThrows;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;

import java.io.InputStream;
import java.security.Key;
import java.security.PrivateKey;

/**
 * Asymmetric key decryptor.
 */
class KeyTransDecryptor extends Decryptor {

    static final int KEY_ID = RecipientId.keyTrans;

    KeyTransDecryptor(RecipientInformation recipientInformation) {
        super(
                new String(((KeyTransRecipientId) recipientInformation.getRID()).getSubjectKeyIdentifier()),
                KEY_ID,
                recipientInformation
        );
    }

    @Override
    @SneakyThrows
    public InputStream decryptionStream(Key key) {
        return recipientInfo
                .getContentStream(new JceKeyTransEnvelopedRecipient((PrivateKey) key))
                .getContentStream();
    }
}