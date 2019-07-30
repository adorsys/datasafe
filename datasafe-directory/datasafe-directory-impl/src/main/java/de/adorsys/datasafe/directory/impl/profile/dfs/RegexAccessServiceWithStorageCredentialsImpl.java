package de.adorsys.datasafe.directory.impl.profile.dfs;

import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.StorageIdentifier;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Specifies how to access desired user resource (example: private bucket). Reads credentials that are associated
 * with provided URI using {@link de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations}.
 * Matches credentials with requested URI using regex-pattern.
 */
@Slf4j
@RuntimeDelegate
public class RegexAccessServiceWithStorageCredentialsImpl implements BucketAccessService {

    private final StorageKeyStoreOperations storageKeyStoreOperations;

    @Inject
    public RegexAccessServiceWithStorageCredentialsImpl(StorageKeyStoreOperations storageKeyStoreOperations) {
        this.storageKeyStoreOperations = storageKeyStoreOperations;
    }

    /**
     * Regex-match associated private resource URI's
     */
    @Override
    public AbsoluteLocation<PrivateResource> privateAccessFor(UserIDAuth user, PrivateResource resource) {
        log.debug("get private access for user {} and bucket {}", user, resource);
        String uri = resource.location().asString();
        Optional<StorageIdentifier> storageAccess = storageKeyStoreOperations.readAliases(user)
                .stream()
                .filter(it -> uri.matches(it.getId()))
                .findFirst();

        if (!storageAccess.isPresent()) {
            return new AbsoluteLocation<>(resource);
        }

        StorageCredentials credentials = storageKeyStoreOperations.getStorageCredentials(user, storageAccess.get());
        return new AbsoluteLocation<>(resource.withAuthority(credentials.getUsername(), credentials.getPassword()));
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
}
