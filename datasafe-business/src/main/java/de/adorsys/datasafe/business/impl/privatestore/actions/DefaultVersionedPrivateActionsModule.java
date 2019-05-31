package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedList;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedRead;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedRemove;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedWrite;
import de.adorsys.datasafe.metainfo.version.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.metainfo.version.api.version.VersionEncoderDecoder;
import de.adorsys.datasafe.metainfo.version.api.version.VersionInfoService;
import de.adorsys.datasafe.metainfo.version.api.version.VersionedPrivateSpaceService;
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
 * This module is responsible for providing versioned actions on PRIVATE folder - getting latest files and
 * their versions.
 */
@Module
public abstract class DefaultVersionedPrivateActionsModule {

    /**
     * Version tag class to get latest document.
     */
    @Provides
    static LatestDFSVersion latestDFSVersion() {
        return new LatestDFSVersion();
    }

    /**
     * Encode version into URL, by default http://example.com/path is encoded to http://example.com/path--VERSION
     */
    @Binds
    abstract VersionEncoderDecoder versionEncoder(DefaultVersionEncoderDecoder impl);

    /**
     * Lists all resource versions.
     */
    @Binds
    abstract VersionInfoService<DFSVersion> versionInfoService(DefaultVersionInfoServiceImpl impl);

    /**
     * Resolver that locates latest document link and reads it.
     */
    @Binds
    abstract EncryptedLatestLinkService latestLink(EncryptedLatestLinkServiceImpl impl);

    /**
     * Lists only latest files in users' privatespace.
     */
    @Binds
    abstract VersionedList<LatestDFSVersion> latestList(LatestListImpl<LatestDFSVersion> impl);

    /**
     * Reads latest blob associated with the resource.
     */
    @Binds
    abstract VersionedRead<LatestDFSVersion> latestRead(LatestReadImpl<LatestDFSVersion> impl);

    /**
     * Removes link to latest blob, so it won't get listed.
     */
    @Binds
    abstract VersionedRemove<LatestDFSVersion> latestRemove(LatestRemoveImpl<LatestDFSVersion> impl);

    /**
     * Writes blob and updates the latest link, so that it points to written blob (creates a version of
     * the document that automatically should become the latest).
     */
    @Binds
    abstract VersionedWrite<LatestDFSVersion> latestWrite(LatestWriteImpl<LatestDFSVersion> impl);

    /**
     * Aggregate view of operations on latest files in privatespace.
     */
    @Binds
    abstract VersionedPrivateSpaceService<LatestDFSVersion> versionVersionedPrivateSpaceService(
            LatestPrivateSpaceImpl<LatestDFSVersion> impl
    );
}
