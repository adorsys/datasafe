package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyFactorySpi;

import javax.inject.Inject;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Public key serializer/deserializer, writes public key content as Base64 encoded string.
 */
@RuntimeDelegate
public class PublicKeySerdeImpl implements PublicKeySerde {


    @Inject
    public PublicKeySerdeImpl() {
    }

    @Override
    @SneakyThrows
    public PublicKey readPubKey(String encoded) {
        // FIXME: Legacy stuff
        byte[] bytes = Base64.getDecoder().decode(encoded);
        return new KeyFactorySpi().generatePublic(SubjectPublicKeyInfo.getInstance(bytes));
    }

    @Override
    @SneakyThrows
    public String writePubKey(PublicKey publicKey) {
        // FIXME: Legacy stuff
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
