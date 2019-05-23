package de.adorsys.datasafe.business.impl.version.latest;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.actions.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.business.api.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.api.privatespace.actions.EncryptedResourceResolver;
import lombok.SneakyThrows;

import javax.inject.Inject;
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
        return new String(
                ByteStreams.toByteArray(privateSpace.read(ReadRequest.forPrivate(owner, latestLink.getResource()))),
                Charsets.UTF_8
        );
    }
}
