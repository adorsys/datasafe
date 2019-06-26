package de.adorsys.datasafe.encrypiton.impl.cmsencryption.decryptors;

import com.google.common.collect.ImmutableMap;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.exceptions.DecryptionException;
import lombok.experimental.UtilityClass;
import org.bouncycastle.cms.RecipientInformation;

import java.util.Map;
import java.util.function.Function;

/**
 * Constructs decryptor based on {@link RecipientInformation} container.
 */
@UtilityClass
public class DecryptorFactory {

    private static final Map<Integer, Function<RecipientInformation, Decryptor>> DECRYPTORS =
            ImmutableMap.of(
                    KekDecryptor.KEY_ID, KekDecryptor::new,
                    KeyTransDecryptor.KEY_ID, KeyTransDecryptor::new
            );

    /**
     * Get decryptor instance to decrypt data on {@code recipient}
     * @param information Container with encrypted data
     * @return Decryptor that, when supplied by proper key can decrypt data in {@code recipient}
     */
    public Decryptor decryptor(RecipientInformation information) {
        int type = information.getRID().getType();

        Function<RecipientInformation, Decryptor> decryptor = DECRYPTORS.get(type);
        if (null == decryptor) {
            throw new DecryptionException("Unsupported decryptor type " + type);
        }

        return decryptor.apply(information);
    }
}
