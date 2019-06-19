package de.adorsys.datasafe.encrypiton.impl.cmsencryption.decryptors;

import lombok.SneakyThrows;
import org.bouncycastle.cms.KEKRecipientId;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.jcajce.JceKEKEnvelopedRecipient;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.security.Key;

/**
 * Symmetric key decryptor.
 */
class KekDecryptor extends Decryptor {

    static final int KEY_ID = RecipientId.kek;

    KekDecryptor(RecipientInformation recipientInformation) {
        super(
                new String(((KEKRecipientId) recipientInformation.getRID()).getKeyIdentifier()),
                KEY_ID,
                recipientInformation
        );
    }

    @Override
    @SneakyThrows
    public InputStream decryptionStream(Key key) {
         return recipientInfo
                 .getContentStream(new JceKEKEnvelopedRecipient((SecretKey) key))
                .getContentStream();
    }
}
