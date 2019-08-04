package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        try (ObjectInputStream ois =
                     new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)))) {
            return (PublicKey) ois.readObject();
        }
    }

    @Override
    @SneakyThrows
    public String writePubKey(PublicKey publicKey) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(publicKey);
            return new String(Base64.getEncoder().encode(bos.toByteArray()));
        }
    }
}
