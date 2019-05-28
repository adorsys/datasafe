package de.adorsys.datasafe.encrypiton.api.types.keystore;


/**
 * Created by peter on 26.02.18 at 17:03.
 */
public interface SecretKeyGenerator {
    SecretKeyEntry generate(String alias, ReadKeyPassword readKeyPassword);
}
