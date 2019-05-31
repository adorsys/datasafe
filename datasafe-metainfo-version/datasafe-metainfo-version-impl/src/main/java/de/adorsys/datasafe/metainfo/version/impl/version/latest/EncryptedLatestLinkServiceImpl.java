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
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.URI;

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
            UserIDAuth owner, PrivateResource resource) {
        UserPrivateProfile privateProfile = profiles.privateProfile(owner);

        AbsoluteLocation<PrivateResource> encryptedPath = resolver.encryptAndResolvePath(
                owner,
                resource
        );

        return new AbsoluteLocation<>(
                encryptedPath.resolve(privateProfile.getDocumentVersionStorage().getResource())
        );
    }

    @Override
    public AbsoluteLocation<PrivateResource> readLinkAndDecrypt(
            UserIDAuth owner,
            AbsoluteLocation<PrivateResource> latestLink) {
        UserPrivateProfile privateProfile = profiles.privateProfile(owner);

        String relativeToPrivateUri = readLink(owner, latestLink);

        PrivateResource userPrivate = privateProfile.getPrivateStorage().getResource();

        PrivateResource resource = privateProfile.getPrivateStorage().getResource().resolve(
                URI.create(relativeToPrivateUri),
                URI.create("")
        );

        return resolver.decryptAndResolvePath(owner, resource, userPrivate);
    }

    @SneakyThrows
    private String readLink(UserIDAuth owner, AbsoluteLocation<PrivateResource> latestLink) {
        try (InputStream is = privateSpace.read(ReadRequest.forPrivate(owner, latestLink.getResource()))) {
            return new String(ByteStreams.toByteArray(is), Charsets.UTF_8);
        }
    }
}
