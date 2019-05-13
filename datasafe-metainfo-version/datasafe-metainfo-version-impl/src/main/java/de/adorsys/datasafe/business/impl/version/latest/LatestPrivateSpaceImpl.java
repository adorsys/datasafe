package de.adorsys.datasafe.business.impl.version.latest;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.version.types.action.ListRequest;
import de.adorsys.datasafe.business.api.version.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.version.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.impl.privatespace.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.stream.Stream;

/**
 * TODO: split down.
 * Each operation will be applied to latest file version.
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

        AbsoluteResourceLocation<PrivateResource> latestSnapshotLink = new AbsoluteResourceLocation<>(
                request.getLocation().resolve(privateProfile.getDocumentVersionStorage().getResource())
        );

        return privateSpace.read(request.toBuilder()
                .location(readLinkAndTransform(request.getOwner(), latestSnapshotLink, privateProfile).getResource())
                .build()
        );
    }

    @Override
    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        UserPrivateProfile privateProfile = profiles.privateProfile(request.getOwner());

        AbsoluteResourceLocation<PrivateResource> latestSnapshotLink = new AbsoluteResourceLocation<>(
                request.getLocation().resolve(privateProfile.getDocumentVersionStorage().getResource())
        );

        return privateSpace.write(request.toBuilder()
                .location(readLinkAndTransform(request.getOwner(), latestSnapshotLink, privateProfile).getResource())
                .build()
        );
    }
}
