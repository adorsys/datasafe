package de.adorsys.datasafe.encrypiton.impl.cmsencryption.decryptors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.cms.RecipientInformation;

import java.io.InputStream;
import java.security.Key;

/**
 * Base class used to for decryption based on key type.
 */
@Getter
@EqualsAndHashCode(of = "keyId")
@RequiredArgsConstructor
public abstract class Decryptor {

    /**
     * Key ID associated with this decryptor.
     */
    protected final String keyId;

    /**
     * Key type that can be handled by this decryptor.
     */
    protected final int keyType;

    /**
     * Recipient information (contains encrypted data) that is going to be decrypted using this class.
     */
    protected final RecipientInformation recipientInfo;

    /**
     * Decrypt data using provided key
     * @param key Key to decrypt data
     * @return decrypted key
     */
    public abstract InputStream decryptionStream(Key key);
}