package de.adorsys.docusafe2.business.api.keystore.types;

import de.adorsys.common.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.PublicKey;

@AllArgsConstructor
@Getter
public class PublicKeyIDWithPublicKey {
    private KeyID keyID;
    private PublicKey publicKey;

    @Override
    public String toString() {
        return "PublicKeyIDWithPublicKey{" +
                "keyID=" + keyID +
                ", publicKey.algorithm = " + publicKey.getAlgorithm() +
                ", publicKey.format = " + publicKey.getFormat() +
                ", publicKey.encoded = " + HexUtil.convertBytesToHexString(publicKey.getEncoded()) +
                '}';
    }
}
