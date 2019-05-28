package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.metainfo.version.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.metainfo.version.api.version.VersionEncoderDecoder;
import de.adorsys.datasafe.metainfo.version.api.version.VersionInfoService;
import de.adorsys.datasafe.metainfo.version.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedList;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedRead;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedRemove;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedWrite;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.DefaultVersionEncoderDecoder;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.DefaultVersionInfoServiceImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.EncryptedLatestLinkServiceImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.LatestPrivateSpaceImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.actions.LatestListImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.actions.LatestReadImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.actions.LatestRemoveImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.actions.LatestWriteImpl;
import de.adorsys.datasafe.metainfo.version.impl.version.types.DFSVersion;
import de.adorsys.datasafe.metainfo.version.impl.version.types.LatestDFSVersion;

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
    abstract VersionEncoderDecoder versionEncoder(DefaultVersionEncoderDecoder impl);

    @Binds
    abstract VersionInfoService<DFSVersion> versionInfoService(DefaultVersionInfoServiceImpl impl);

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
