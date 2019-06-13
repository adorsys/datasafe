package de.adorsys.datasafe.encrypiton.impl.cmsencryption;

import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.exceptions.DecryptionException;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.utils.Log;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.function.Function;

/**
 * Cryptographic message syntax document encoder/decoder - see
 *
 * @see <a href=https://en.wikipedia.org/wiki/Cryptographic_Message_Syntax">CMS
 * wiki</a>
 */
@Slf4j
@RuntimeDelegate
public class CMSEncryptionServiceImpl implements CMSEncryptionService {

    private CMSEncryptionConfig encryptionConfig;

    @Inject
    public CMSEncryptionServiceImpl(CMSEncryptionConfig encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }

    /**
     * Asymmetrical encryption-based stream, algorithm is provided by {@link CMSEncryptionConfig#getAlgorithm()}
     * Uses {@link RecipientId#keyTrans} recipient id.
     */
    @Override
    @SneakyThrows
    public OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, PublicKey publicKey,
                                                    KeyID publicKeyID) {
        RecipientInfoGenerator rec = new JceKeyTransRecipientInfoGenerator(publicKeyID.getValue().getBytes(),
                publicKey);

        return streamEncrypt(dataContentStream, rec, encryptionConfig.getAlgorithm());
    }

    /**
     * Symmetrical encryption-based stream, algorithm is provided by {@link CMSEncryptionConfig#getAlgorithm()}
     * Uses {@link RecipientId#kek} recipient id.
     */
    @Override
    @SneakyThrows
    public OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, SecretKey secretKey, KeyID keyID) {
        RecipientInfoGenerator rec = new JceKEKRecipientInfoGenerator(keyID.getValue().getBytes(), secretKey);

        return streamEncrypt(dataContentStream, rec, encryptionConfig.getAlgorithm());
    }

    /**
     * Decryption stream using {@link CMSEnvelopedDataParser}, supported recipients ids are:
     * - {@link RecipientId#keyTrans} for asymmetric encryption
     * - {@link RecipientId#kek} for symmetric encryption
     */
    @Override
    @SneakyThrows
    public InputStream buildDecryptionInputStream(InputStream inputStream, Function<String, Key> keyById) {
        RecipientInformationStore recipientInfoStore = new CMSEnvelopedDataParser(inputStream).getRecipientInfos();

        if (recipientInfoStore.size() == 0) {
            throw new DecryptionException("CMS Envelope doesn't contain recipients");
        }
        if (recipientInfoStore.size() > 1) {
            throw new DecryptionException("Programming error. Handling of more that one recipient not done yet");
        }
        RecipientInformation recipientInfo = recipientInfoStore.getRecipients().stream().findFirst().get();
        RecipientId rid = recipientInfo.getRID();

        switch (rid.getType()) {
            case RecipientId.keyTrans:
                return recipientInfo.getContentStream(new JceKeyTransEnvelopedRecipient(privateKey(keyById, rid)))
                        .getContentStream();
            case RecipientId.kek:
                return recipientInfo.getContentStream(new JceKEKEnvelopedRecipient(secretKey(keyById, rid)))
                        .getContentStream();
            default:
                throw new DecryptionException("Programming error. Handling of more that one recipient not done yet");
        }
    }

    private SecretKey secretKey(Function<String, Key> keyById, RecipientId rid) {
        String keyIdentifier = new String(((KEKRecipientId) rid).getKeyIdentifier());
        log.debug("Secret key ID from envelope: {}", Log.secure(keyIdentifier));
        return (SecretKey) keyById.apply(keyIdentifier);
    }

    private PrivateKey privateKey(Function<String, Key> keyById, RecipientId rid) {
        String subjectKeyIdentifier = new String(((KeyTransRecipientId) rid).getSubjectKeyIdentifier());
        log.debug("Private key ID from envelope: {}", Log.secure(subjectKeyIdentifier));
        return (PrivateKey) keyById.apply(subjectKeyIdentifier);
    }

    private OutputStream streamEncrypt(OutputStream dataContentStream, RecipientInfoGenerator rec,
                                       ASN1ObjectIdentifier algorithm) throws CMSException, IOException {
        CMSEnvelopedDataStreamGenerator gen = new CMSEnvelopedDataStreamGenerator();
        gen.addRecipientInfoGenerator(rec);
        return gen.open(dataContentStream, new JceCMSContentEncryptorBuilder(algorithm)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build());
    }
}
