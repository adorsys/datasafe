package de.adorsys.datasafe.metainfo.version.impl.version.latest.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedWrite;
import de.adorsys.datasafe.metainfo.version.api.version.VersionEncoder;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.types.LatestDFSVersion;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.privatestore.api.actions.WriteToPrivate;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.utils.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class LatestWriteImpl<V extends LatestDFSVersion> implements VersionedWrite<V> {

    @Getter
    private final V strategy;

    private final VersionEncoder encoder;
    private final EncryptedResourceResolver encryptedResourceResolver;
    private final WriteToPrivate writeToPrivate;
    private final EncryptedLatestLinkServiceImpl latestVersionLinkLocator;

    @Inject
    public LatestWriteImpl(V strategy, VersionEncoder encoder, EncryptedResourceResolver encryptedResourceResolver,
                           WriteToPrivate writeToPrivate, EncryptedLatestLinkServiceImpl latestVersionLinkLocator) {
        this.strategy = strategy;
        this.encoder = encoder;
        this.encryptedResourceResolver = encryptedResourceResolver;
        this.writeToPrivate = writeToPrivate;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        AbsoluteLocation<PrivateResource> latestSnapshotLink =
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(), request.getLocation()
                );

        URI decryptedPath = encoder.newVersion(request.getLocation().location()).getPathWithVersion();

        PrivateResource resourceRelativeToPrivate = encryptPath(
                request.getOwner(),
                decryptedPath,
                request.getLocation()
        );

        return new VersionCommittingStream(
                writeToPrivate.write(
                        request.toBuilder().location(BasePrivateResource.forPrivate(decryptedPath)).build()
                ),
                writeToPrivate,
                request.toBuilder().location(latestSnapshotLink.getResource()).build(),
                resourceRelativeToPrivate
        );
    }

    private PrivateResource encryptPath(UserIDAuth auth, URI uri, PrivateResource base) {
        AbsoluteLocation<PrivateResource> resource = encryptedResourceResolver.encryptAndResolvePath(
                auth,
                base.resolve(uri, URI.create(""))
        );

        return BasePrivateResource.forPrivate(resource.getResource().encryptedPath());
    }

    @Slf4j
    @RequiredArgsConstructor
    private static final class VersionCommittingStream extends OutputStream {

        private final OutputStream streamToWrite;
        private final WriteToPrivate writeToPrivate;
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
            try (OutputStream os = writeToPrivate.write(request)) {
                os.write(writtenResource.location().toASCIIString().getBytes());
            }
        }
    }
}
