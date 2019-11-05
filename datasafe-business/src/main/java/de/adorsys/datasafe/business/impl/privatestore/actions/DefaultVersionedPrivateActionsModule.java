package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedList;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedRead;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedRemove;
import de.adorsys.datasafe.metainfo.version.api.actions.VersionedWrite;
import de.adorsys.datasafe.metainfo.version.api.version.EncryptedLatestLinkService;
import de.adorsys.datasafe.metainfo.version.api.version.VersionInfoService;
import de.adorsys.datasafe.metainfo.version.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.metainfo.version.impl.version.VersionEncoderDecoder;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.DefaultVersionEncoderDecoderRuntimeDelegatable;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.DefaultVersionInfoServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.EncryptedLatestLinkServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.LatestPrivateSpaceImplRuntimeDelegatable;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.actions.LatestListImplRuntimeDelegatable;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.actions.LatestReadImplRuntimeDelegatable;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.actions.LatestRemoveImplRuntimeDelegatable;
import de.adorsys.datasafe.metainfo.version.impl.version.latest.actions.LatestWriteImplRuntimeDelegatable;
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
     * Encode version into URL, by default http://example.com/path is encoded to http://example.com/path/VERSION
     */
    @Binds
    abstract VersionEncoderDecoder versionEncoder(DefaultVersionEncoderDecoderRuntimeDelegatable impl);

    /**
     * Lists all resource versions.
     */
    @Binds
    abstract VersionInfoService<DFSVersion> versionInfoService(DefaultVersionInfoServiceImplRuntimeDelegatable impl);

    /**
     * Resolver that locates latest document link and reads it.
     */
    @Binds
    abstract EncryptedLatestLinkService latestLink(EncryptedLatestLinkServiceImplRuntimeDelegatable impl);

    /**
     * Lists only latest files in users' privatespace.
     */
    @Binds
    abstract VersionedList<LatestDFSVersion> latestList(LatestListImplRuntimeDelegatable<LatestDFSVersion> impl);

    /**
     * Reads latest blob associated with the resource.
     */
    @Binds
    abstract VersionedRead<LatestDFSVersion> latestRead(LatestReadImplRuntimeDelegatable<LatestDFSVersion> impl);

    /**
     * Removes link to latest blob, so it won't get listed.
     */
    @Binds
    abstract VersionedRemove<LatestDFSVersion> latestRemove(LatestRemoveImplRuntimeDelegatable<LatestDFSVersion> impl);

    /**
     * Writes blob and updates the latest link, so that it points to written blob (creates a version of
     * the document that automatically should become the latest).
     */
    @Binds
    abstract VersionedWrite<LatestDFSVersion> latestWrite(LatestWriteImplRuntimeDelegatable<LatestDFSVersion> impl);

    /**
     * Aggregate view of operations on latest files in privatespace.
     */
    @Binds
    abstract VersionedPrivateSpaceService<LatestDFSVersion> versionVersionedPrivateSpaceService(
            LatestPrivateSpaceImplRuntimeDelegatable<LatestDFSVersion> impl
    );
}
