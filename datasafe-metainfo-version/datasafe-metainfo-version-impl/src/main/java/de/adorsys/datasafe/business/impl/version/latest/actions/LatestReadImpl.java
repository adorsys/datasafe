package de.adorsys.datasafe.business.impl.version.latest.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.actions.VersionedRead;
import de.adorsys.datasafe.business.impl.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.impl.version.latest.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import lombok.Getter;

import javax.inject.Inject;
import java.io.InputStream;

public class LatestReadImpl<V extends LatestDFSVersion> implements VersionedRead<V> {

    @Getter
    private final V strategy;

    private final ReadFromPrivate readFromPrivate;
    private final EncryptedLatestLinkServiceImpl latestVersionLinkLocator;

    @Inject
    public LatestReadImpl(V versionStrategy, ReadFromPrivate readFromPrivate,
                          EncryptedLatestLinkServiceImpl latestVersionLinkLocator) {
        this.strategy = versionStrategy;
        this.readFromPrivate = readFromPrivate;
        this.latestVersionLinkLocator = latestVersionLinkLocator;
    }

    @Override
    public InputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {

        AbsoluteLocation<PrivateResource> latestSnapshotLink =
                latestVersionLinkLocator.resolveLatestLinkLocation(
                        request.getOwner(),
                        request.getLocation()
                );

        return readFromPrivate.read(request.toBuilder()
                .location(latestVersionLinkLocator.readLinkAndDecrypt(
                        request.getOwner(),
                        latestSnapshotLink).getResource()
                )
                .build()
        );
    }
}
