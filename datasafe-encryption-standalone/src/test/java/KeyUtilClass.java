import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import lombok.SneakyThrows;

import java.security.KeyStore;
import java.util.Enumeration;

@lombok.experimental.UtilityClass
public class KeyUtilClass {

    @SneakyThrows
    public KeyID keyIdByPrefix(KeyStore keyStore, String prefix) {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String element = aliases.nextElement();
            if (element.startsWith(prefix)) {
                return new KeyID(element);
            }
        }

        throw new IllegalArgumentException("Keystore does not contain key with prefix: " + prefix);
    }
}
