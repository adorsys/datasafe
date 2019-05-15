package de.adorsys.datasafe.business.impl.version.latest.actions;

import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.BasePrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.utils.Log;
import de.adorsys.datasafe.business.api.version.actions.VersionedWrite;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.impl.privatespace.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.business.impl.version.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

public class LatestWriteImpl<V extends LatestDFSVersion> implements VersionedWrite<V> {

    @Getter
    private final V strategy;

    private final EncryptedResourceResolver encryptedResourceResolver;
    private final ProfileRetrievalService profiles;
    private final PrivateSpaceService privateSpace;
    private final EncryptedLatestLinkServiceImpl latestVersionLinkLocator;

    @Inject
    public LatestWriteImpl(V versionStrategy, EncryptedResourceResolver encryptedResourceResolver,
                           ProfileRetrievalService profiles, PrivateSpaceService privateSpace,
                           EncryptedLatestLinkServiceImpl latestVersionLinkLocator) {
        this.strategy = versionStrategy;
        this.encryptedResourceResolver = encryptedResourceResolver;
        this.profiles = profiles;
        this.privateSpace = privateSpace;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        UserPrivateProfile privateProfile = profiles.privateProfile(request.getOwner());

        AbsoluteResourceLocation<PrivateResource> latestSnapshotLink =
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(), request.getLocation(), privateProfile
                );

        URI decryptedPath = URI.create(request.getLocation().location() + "-" + UUID.randomUUID().toString());

        PrivateResource resourceRelativeToPrivate = encryptPath(
                request.getOwner(),
                decryptedPath,
                request.getLocation()
        );

        return new VersionCommittingStream(
                privateSpace.write(
                        request.toBuilder().location(BasePrivateResource.forPrivate(decryptedPath)).build()
                ),
                privateSpace,
                request.toBuilder().location(latestSnapshotLink.getResource()).build(),
                resourceRelativeToPrivate
        );
    }

    private PrivateResource encryptPath(UserIDAuth auth, URI uri, PrivateResource base) {
        AbsoluteResourceLocation<PrivateResource> resource = encryptedResourceResolver.encryptAndResolvePath(
                auth,
                base.resolve(uri, URI.create(""))
        );

        return BasePrivateResource.forPrivate(resource.getResource().encryptedPath());
    }

    @Slf4j
    @RequiredArgsConstructor
    private static final class VersionCommittingStream extends OutputStream {

        private final OutputStream streamToWrite;
        private final PrivateSpaceService privateSpaceService;
        private final WriteRequest<UserIDAuth, PrivateResource> request;
        private final PrivateResource writtenResource;

        @Override
        public void write(int b) throws IOException {
            streamToWrite.write(b);
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            streamToWrite.write(bytes, off, len);
        }

        @Override
        @SneakyThrows
        public void close() {
            super.close();
            streamToWrite.close();

            log.debug("Committing file {} with blob {}", Log.secure(request.getLocation()), Log.secure(writtenResource));
            try (OutputStream os = privateSpaceService.write(request)) {
                os.write(writtenResource.location().toASCIIString().getBytes());
            }
        }
    }
}
