package de.adorsys.datasafe.directory.impl.profile.dfs;

import dagger.Lazy;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

/**
 * Specifies how to access desired user resource (example: private bucket). Reads credentials that are associated
 * with provided URI using {@link de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations}.
 * Matches credentials with requested URI using regex-pattern.
 */
@Slf4j
@RuntimeDelegate
public class RegexAccessServiceWithStorageCredentialsImpl implements BucketAccessService {

    private final Lazy<StorageKeyStoreOperations> storageKeyStoreOperations;

    @Inject
    public RegexAccessServiceWithStorageCredentialsImpl(Lazy<StorageKeyStoreOperations> storageKeyStoreOperations) {
        this.storageKeyStoreOperations = storageKeyStoreOperations;
    }

    /**
     * Regex-match associated private resource URI's
     */
    @Override
    public AbsoluteLocation<PrivateResource> privateAccessFor(UserIDAuth user, PrivateResource resource) {
        log.debug("get private access for user {} and bucket {}", user, resource);
        Optional<StorageIdentifier> storageAccess = getStorageAccessCredentials(user, resource);

        if (!storageAccess.isPresent()) {
            // attempt to re-read storages keystore, maybe cache is expired:
            storageKeyStoreOperations.get().invalidateCache(user);
            storageAccess = getStorageAccessCredentials(user, resource);
            // looks like there is really no storage credentials for this resource, either it can be public:
            if (!storageAccess.isPresent()) {
                return new AbsoluteLocation<>(resource);
            }
        }

        StorageCredentials creds = storageKeyStoreOperations.get().getStorageCredentials(user, storageAccess.get());
        return new AbsoluteLocation<>(resource.withAuthority(creds.getUsername(), creds.getPassword()));
    }

    /**
     * Do nothing, just wrap, real use case would be to plug user credentials to access bucket.
     */
    @Override
    public AbsoluteLocation<PublicResource> publicAccessFor(UserID user, PublicResource resource) {
        log.debug("get public access for user {} and bucket {}", user, resource.location());
        return new AbsoluteLocation<>(resource);
    }

    /**
     * Do nothing, just wrap, real use case would be to plug user credentials to access bucket.
     */
    @Override
    public AbsoluteLocation withSystemAccess(AbsoluteLocation resource) {
        log.debug("get system access for {}", resource.location());
        return new AbsoluteLocation<>(resource);
    }

    private Optional<StorageIdentifier> getStorageAccessCredentials(UserIDAuth user, PrivateResource resource) {
        String uri = resource.location().asString();
        Set<StorageIdentifier> aliases = storageKeyStoreOperations.get().readAliases(user);

        Optional<StorageIdentifier> directMatch = aliases
                .stream()
                .filter(it -> uri.matches(it.getId()))
                .findFirst();

        return directMatch.isPresent()
                ? directMatch
                : aliases.stream().filter(it -> StorageIdentifier.DEFAULT.getId().equals(it.getId())).findFirst();
    }
}
