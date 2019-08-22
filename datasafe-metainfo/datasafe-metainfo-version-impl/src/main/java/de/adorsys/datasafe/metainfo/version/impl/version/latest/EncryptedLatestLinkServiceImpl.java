package de.adorsys.datasafe.metainfo.version.impl.version.latest;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.metainfo.version.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;

/**
 * Default latest link service that stores latest resource links within
 * {@link UserPrivateProfile#getDocumentVersionStorage()}. Those links have encrypted path by
 * {@link EncryptedResourceResolver} and point to versioned blobs within privatespace (so that link
 * content is always relative resource location inside privatespace)
 */
@Slf4j
@RuntimeDelegate
public class EncryptedLatestLinkServiceImpl implements EncryptedLatestLinkService {

    private final ProfileRetrievalService profiles;
    private final EncryptedResourceResolver resolver;
    private final PrivateSpaceService privateSpace;

    @Inject
    public EncryptedLatestLinkServiceImpl(EncryptedResourceResolver resolver, PrivateSpaceService privateSpace,
                                          ProfileRetrievalService profiles) {
        this.resolver = resolver;
        this.privateSpace = privateSpace;
        this.profiles = profiles;
    }

    @Override
    public AbsoluteLocation<PrivateResource> resolveLatestLinkLocation(
            UserIDAuth owner, PrivateResource resource, StorageIdentifier identifier) {
        UserPrivateProfile privateProfile = profiles.privateProfile(owner);

        if (null == privateProfile.getDocumentVersionStorage()) {
            log.error("Missing version storage for {}", Obfuscate.secure(owner.getUserID().getValue()));
            throw new IllegalStateException("User private profile is missing document version storage");
        }

        AbsoluteLocation<PrivateResource> encryptedPath = resolver.encryptAndResolvePath(
                owner,
                resource,
                identifier
        );

        return new AbsoluteLocation<>(
                encryptedPath.resolveFrom(privateProfile.getDocumentVersionStorage().getResource())
        );
    }

    @Override
    public Function<AbsoluteLocation<PrivateResource>, AbsoluteLocation<PrivateResource>> linkDecryptingReader(
        UserIDAuth owner, StorageIdentifier identifier) {
        UserPrivateProfile privateProfile = profiles.privateProfile(owner);
        PrivateResource userPrivate = privateProfile.getPrivateStorage().get(identifier).getResource();

        Function<PrivateResource, AbsoluteLocation<PrivateResource>> decryptingResolver =
                resolver.decryptingResolver(owner, userPrivate, identifier);

        return latestLink -> {
            String relativeToPrivateUri = readLink(owner, latestLink);

            PrivateResource resource = userPrivate.resolve(
                    new Uri(URI.create(relativeToPrivateUri)),
                    new Uri("")
            );

            return decryptingResolver.apply(resource);
        };
    }

    @SneakyThrows
    private String readLink(UserIDAuth owner, AbsoluteLocation<PrivateResource> latestLink) {
        try (InputStream is = privateSpace.read(ReadRequest.forPrivate(owner, latestLink.getResource()))) {
            return new String(ByteStreams.toByteArray(is), Charsets.UTF_8);
        }
    }
}
