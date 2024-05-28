package de.adorsys.datasafe.encrypiton.impl.cmsencryption.decryptors;

import lombok.SneakyThrows;
import org.bouncycastle.cms.KeyAgreeRecipientId;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.jcajce.JceKeyAgreeEnvelopedRecipient;

import java.io.InputStream;
import java.security.Key;
import java.security.PrivateKey;

public class KeyAgreeDecryptor extends Decryptor {

    static final int KEY_ID = RecipientId.keyAgree;

    KeyAgreeDecryptor(RecipientInformation recipientInformation) {
        super(
                new String(((KeyAgreeRecipientId) recipientInformation.getRID()).getSubjectKeyIdentifier()),
                KEY_ID,
                recipientInformation
        );
    }

    @Override
    @SneakyThrows
    public InputStream decryptionStream(Key privateKey) {
        return recipientInfo
                .getContentStream(new JceKeyAgreeEnvelopedRecipient((PrivateKey) privateKey))
                .getContentStream();
    }
}