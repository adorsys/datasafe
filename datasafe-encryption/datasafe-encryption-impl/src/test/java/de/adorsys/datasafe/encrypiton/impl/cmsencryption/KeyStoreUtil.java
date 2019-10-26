package de.adorsys.datasafe.encrypiton.impl.cmsencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.security.Key;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
class KeyStoreUtil {

    @SneakyThrows
    public static Map<String, Key> getKeys(Set<String> ids, KeyStoreAccess access) {
        return ids.stream().filter(it -> containsAlias(it, access)).collect(Collectors.toMap(
                it -> it,
                it -> getKey(it, access)
        ));
    }

    @SneakyThrows
    private boolean containsAlias(String id, KeyStoreAccess access) {
        return access.getKeyStore().containsAlias(id);
    }

    @SneakyThrows
    private Key getKey(String id, KeyStoreAccess access) {
        return access.getKeyStore().getKey(id, access.getKeyStoreAuth().getReadKeyPassword().getValue());
    }
}
