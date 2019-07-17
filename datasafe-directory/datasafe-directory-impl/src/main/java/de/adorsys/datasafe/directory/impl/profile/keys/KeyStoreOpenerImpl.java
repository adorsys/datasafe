package de.adorsys.datasafe.directory.impl.profile.keys;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.KeyStoreOpener;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows to re-read keystore if supplied ReadKeyPassword can't open it.
 */
@RuntimeDelegate
public class KeyStoreOpenerImpl implements KeyStoreOpener {

    private final DFSConfig dfsConfig;
    private final BucketAccessService bucketAccessService;
    private final ProfileRetrievalService profile;
    private final StorageReadService readService;
    private final KeyStoreCache keystoreCache;
    private final KeyStoreService keyStoreService;

    @Inject
    public KeyStoreOpenerImpl(DFSConfig dfsConfig, BucketAccessService bucketAccessService,
                              ProfileRetrievalService profile, StorageReadService readService,
                              KeyStoreCache keystoreCache, KeyStoreService keyStoreService) {
        this.dfsConfig = dfsConfig;
        this.bucketAccessService = bucketAccessService;
        this.profile = profile;
        this.readService = readService;
        this.keystoreCache = keystoreCache;
        this.keyStoreService = keyStoreService;
    }

    /**
     * Tries to re-read keystore from storage if supplied password can't open cached keystore.
     */
    @Override
    @SneakyThrows
    public Key getKey(UserIDAuth forUser, String alias) {
        try {
            return keyStore(forUser).getKey(alias, forUser.getReadKeyPassword().getValue().toCharArray());
        } catch (UnrecoverableKeyException ex) {
            keystoreCache.getKeystore().remove(forUser.getUserID());
            keystoreCache.getPublicKeys().remove(forUser.getUserID());

            return keyStore(forUser).getKey(alias, forUser.getReadKeyPassword().getValue().toCharArray());
        }
    }

    /**
     * Reads aliases from keystore associated with user.
     */
    @Override
    @SneakyThrows
    public Set<String> readAliases(UserIDAuth forUser) {
        Set<String> result = new HashSet<>();
        Enumeration<String> aliases = keyStore(forUser).aliases();
        while (aliases.hasMoreElements()) {
            result.add(aliases.nextElement());
        }

        return result;
    }

    private KeyStore keyStore(UserIDAuth forUser) {
        return keystoreCache.getKeystore().computeIfAbsent(
                forUser.getUserID(),
                userId -> readKeyStore(forUser)
        );
    }

    @SneakyThrows
    private KeyStore readKeyStore(UserIDAuth forUser) {
        AbsoluteLocation<PrivateResource> access = bucketAccessService.privateAccessFor(
                forUser,
                profile.privateProfile(forUser).getKeystore().getResource()
        );

        byte[] payload;
        try (InputStream is = readService.read(access)) {
            payload = ByteStreams.toByteArray(is);
        }

        return keyStoreService.deserialize(
                payload,
                forUser.getUserID().getValue(),
                dfsConfig.privateKeyStoreAuth(forUser).getReadStorePassword()
        );
    }
}
