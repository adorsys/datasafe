package de.adorsys.datasafe.business.impl.encryption.keystore;

import de.adorsys.datasafe.business.api.encryption.keystore.PublicKeySerde;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.Base64;

public class PublicKeySerdeImpl implements PublicKeySerde {

    @Inject
    PublicKeySerdeImpl() {
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
