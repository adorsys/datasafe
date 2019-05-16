package de.adorsys.datasafe.business.impl.version;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.impl.privatespace.actions.EncryptedResourceResolver;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.net.URI;

public class EncryptedLatestLinkServiceImpl implements EncryptedLatestLinkService {

    private final EncryptedResourceResolver resolver;
    private final PrivateSpaceService privateSpace;

    @Inject
    public EncryptedLatestLinkServiceImpl(EncryptedResourceResolver resolver, PrivateSpaceService privateSpace) {
        this.resolver = resolver;
        this.privateSpace = privateSpace;
    }

    @Override
    public AbsoluteLocation<PrivateResource> resolveLatestLinkLocation(
            UserIDAuth auth, PrivateResource resource, UserPrivateProfile privateProfile) {
        AbsoluteLocation<PrivateResource> encryptedPath = resolver.encryptAndResolvePath(
                auth,
                resource
        );

        return new AbsoluteLocation<>(
                encryptedPath.resolve(privateProfile.getDocumentVersionStorage().getResource())
        );
    }

    @Override
    public AbsoluteLocation<PrivateResource> readLinkAndDecrypt(
            UserIDAuth owner,
            AbsoluteLocation<PrivateResource> latestLink,
            UserPrivateProfile privateProfile) {
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
