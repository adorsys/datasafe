package de.adorsys.datasafe.business.impl.version.latest;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.action.RemoveRequest;
import de.adorsys.datasafe.business.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.impl.privatespace.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * TODO: split down.
 * Each operation will be applied to latest file version.
 *
 * @param <V>
 */
public class LatestPrivateSpaceImpl<V extends LatestDFSVersion> implements VersionedPrivateSpaceService<V> {

    @Getter
    private final V versionStrategy;

    private final EncryptedResourceResolver encryptedResourceResolver;
    private final ProfileRetrievalService profiles;
    private final PrivateSpaceService privateSpace;

    @Inject
    public LatestPrivateSpaceImpl(V versionStrategy, EncryptedResourceResolver encryptedResourceResolver,
                                  ProfileRetrievalService profiles, PrivateSpaceService privateSpace) {
        this.versionStrategy = versionStrategy;
        this.encryptedResourceResolver = encryptedResourceResolver;
        this.profiles = profiles;
        this.privateSpace = privateSpace;
    }

    @Override
    public Stream<AbsoluteResourceLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        UserPrivateProfile privateProfile = profiles.privateProfile(request.getOwner());

        ListRequest<UserIDAuth, PrivateResource> forLatestSnapshotDir = request.toBuilder().location(
                request.getLocation().resolve(privateProfile.getDocumentVersionStorage().getResource())
        ).build();

        return privateSpace
                .list(forLatestSnapshotDir)
                .map(it -> readLinkAndTransform(request.getOwner(), it, privateProfile));
    }

    private AbsoluteResourceLocation<PrivateResource> readLinkAndTransform(
            UserIDAuth owner,
            AbsoluteResourceLocation<PrivateResource> latestLink,
            UserPrivateProfile privateProfile) {
        String relativeToPrivateUri = readLink(owner, latestLink);

        PrivateResource userPrivate = privateProfile.getPrivateStorage().getResource();

        PrivateResource resource = privateProfile.getPrivateStorage().getResource().resolve(
                URI.create(relativeToPrivateUri),
                URI.create("")
        );

        return encryptedResourceResolver.decryptAndResolvePath(owner, resource, userPrivate);
    }

    @SneakyThrows
    private String readLink(UserIDAuth owner, AbsoluteResourceLocation<PrivateResource> latestLink) {
        return new String(
                ByteStreams.toByteArray(privateSpace.read(ReadRequest.forPrivate(owner, latestLink.getResource()))),
                Charsets.UTF_8
        );
    }

    @Override
    public InputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {
        UserPrivateProfile privateProfile = profiles.privateProfile(request.getOwner());

        AbsoluteResourceLocation<PrivateResource> latestSnapshotLink = resolveEncryptedLinkLocation(request.getOwner(), request.getLocation(), privateProfile);

        return privateSpace.read(request.toBuilder()
                .location(readLinkAndTransform(request.getOwner(), latestSnapshotLink, privateProfile).getResource())
                .build()
        );
    }

    @Override
    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        UserPrivateProfile privateProfile = profiles.privateProfile(request.getOwner());

        AbsoluteResourceLocation<PrivateResource> latestSnapshotLink =
                resolveEncryptedLinkLocation(request.getOwner(), request.getLocation(), privateProfile);

        URI decryptedPath = URI.create(request.getLocation().location() + "-" + UUID.randomUUID().toString());

        PrivateResource resourceRelativeToPrivate = encryptWuthUUID(request.getOwner(), decryptedPath, request.getLocation());

        return new VersionCommittingStream(
                privateSpace.write(
                        request.toBuilder().location(DefaultPrivateResource.forPrivate(decryptedPath)).build()
                ),
                privateSpace,
                request.toBuilder().location(latestSnapshotLink.getResource()).build(),
                resourceRelativeToPrivate
        );
    }

    @Override
    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
    }

    private AbsoluteResourceLocation<PrivateResource> resolveEncryptedLinkLocation(UserIDAuth auth, PrivateResource resource, UserPrivateProfile privateProfile) {
        AbsoluteResourceLocation<PrivateResource> encryptedPath = encryptedResourceResolver.encryptAndResolvePath(
                auth,
                resource
        );

        return new AbsoluteResourceLocation<>(
                encryptedPath.resolve(privateProfile.getDocumentVersionStorage().getResource())
        );
    }

    private PrivateResource encryptWuthUUID(UserIDAuth auth, URI uri, PrivateResource base) {
        AbsoluteResourceLocation<PrivateResource> resource = encryptedResourceResolver.encryptAndResolvePath(
                auth,
                base.resolve(uri, URI.create(""))
        );

        return DefaultPrivateResource.forPrivate(resource.getResource().encryptedPath());
    }

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

            try (OutputStream os = privateSpaceService.write(request)) {
                os.write(writtenResource.location().toASCIIString().getBytes());
            }
        }
    }
}
