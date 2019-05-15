package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.business.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.business.api.version.actions.VersionedList;
import de.adorsys.datasafe.business.api.version.actions.VersionedRead;
import de.adorsys.datasafe.business.api.version.actions.VersionedRemove;
import de.adorsys.datasafe.business.api.version.actions.VersionedWrite;
import de.adorsys.datasafe.business.impl.version.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.business.impl.version.latest.LatestPrivateSpaceImpl;
import de.adorsys.datasafe.business.impl.version.latest.actions.LatestListImpl;
import de.adorsys.datasafe.business.impl.version.latest.actions.LatestReadImpl;
import de.adorsys.datasafe.business.impl.version.latest.actions.LatestRemoveImpl;
import de.adorsys.datasafe.business.impl.version.latest.actions.LatestWriteImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;

/**
 * This module is responsible for providing versioned actions on PRIVATE folder.
 */
@Module
public abstract class DefaultVersionedPrivateActionsModule {

    @Provides
    static LatestDFSVersion latestDFSVersion() {
        return new LatestDFSVersion();
    }

    @Binds
    abstract EncryptedLatestLinkService latestLink(EncryptedLatestLinkServiceImpl impl);

    @Binds
    abstract VersionedList<LatestDFSVersion> latestList(LatestListImpl<LatestDFSVersion> impl);

    @Binds
    abstract VersionedRead<LatestDFSVersion> latestRead(LatestReadImpl<LatestDFSVersion> impl);

    @Binds
    abstract VersionedRemove<LatestDFSVersion> latestRemove(LatestRemoveImpl<LatestDFSVersion> impl);

    @Binds
    abstract VersionedWrite<LatestDFSVersion> latestWrite(LatestWriteImpl<LatestDFSVersion> impl);

    @Binds
    abstract VersionedPrivateSpaceService<LatestDFSVersion> versionVersionedPrivateSpaceService(
            LatestPrivateSpaceImpl<LatestDFSVersion> impl
    );
}
