package de.adorsys.datasafe.directory.impl.profile.keys;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Retrieves and opens public keystore associated with user location DFS storage.
 */
@RuntimeDelegate
public class DFSPublicKeyServiceImpl implements PublicKeyService {

    private final KeyStoreCache keystoreCache;
    private final BucketAccessService bucketAccessService;
    private final ProfileRetrievalService profiles;
    private final StorageReadService readService;
    private final GsonSerde serde;

    @Inject
    public DFSPublicKeyServiceImpl(KeyStoreCache keystoreCache,
                                   BucketAccessService bucketAccessService, ProfileRetrievalService profiles,
                                   StorageReadService readService, GsonSerde serde) {
        this.keystoreCache = keystoreCache;
        this.bucketAccessService = bucketAccessService;
        this.profiles = profiles;
        this.readService = readService;
        this.serde = serde;
    }

    /**
     * Reads users' public key from DFS and caches the result.
     */
    @Override
    public PublicKeyIDWithPublicKey publicKey(UserID forUser) {
        return keystoreCache.getPublicKeys().computeIfAbsent(
                forUser,
                id -> publicKeyList(forUser)
        ).get(0);
    }

    @SneakyThrows
    private List<PublicKeyIDWithPublicKey> publicKeyList(UserID forUser) {
        AbsoluteLocation<PublicResource> accessiblePublicKey = bucketAccessService.publicAccessFor(
                forUser,
                profiles.publicProfile(forUser).getPublicKeys().getResource()
        );

        try (JsonReader is = new JsonReader(new InputStreamReader(readService.read(accessiblePublicKey)))) {
            return serde.fromJson(is, new TypeToken<List<PublicKeyIDWithPublicKey>>() {}.getType());
        }
    }
}
