package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.exceptions.DecryptionException;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

import javax.inject.Inject;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Public key serializer/deserializer, writes public key content as Base64 encoded string.
 */
@RuntimeDelegate
public class PublicKeySerdeImpl implements PublicKeySerde {

    private static final ASN1ObjectIdentifier RSA = PKCSObjectIdentifiers.rsaEncryption;
    private static final ASN1ObjectIdentifier EC = X9ObjectIdentifiers.id_ecPublicKey;

    @Inject
    public PublicKeySerdeImpl() {
    }

    @Override
    @SneakyThrows
    public PublicKey readPubKey(String encoded) {
        // FIXME: Legacy stuff
        byte[] bytes = Base64.getDecoder().decode(encoded);
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(bytes);
        if (RSA.equals(subjectPublicKeyInfo.getAlgorithm())) {
            return new org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyFactorySpi().generatePublic(SubjectPublicKeyInfo.getInstance(bytes));
        } else if (EC.equals(subjectPublicKeyInfo.getAlgorithm())) {
            return new org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi.ECDH().generatePublic(subjectPublicKeyInfo);
        }
        throw new DecryptionException("PublicKeySerdeImpl.UnsupportedEncodedKey");
    }

    @Override
    @SneakyThrows
    public String writePubKey(PublicKey publicKey) {
        // FIXME: Legacy stuff
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
