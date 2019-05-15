package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.business.impl.version.latest.LatestPrivateSpaceImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import de.adorsys.datasafe.business.impl.version.types.OlderThanLatestDFSVersion;

/**
 * This module is responsible for providing default actions on PRIVATE folder.
 */
@Module
public abstract class DefaultVersionedPrivateActionsModule {

    @Provides
    static LatestDFSVersion latestDFSVersionStrategy() {
        return new LatestDFSVersion();
    }

    @Provides
    static OlderThanLatestDFSVersion olderThanLatestDFSVersionStrategy() {
        return new OlderThanLatestDFSVersion();
    }

    @Binds
    abstract VersionedPrivateSpaceService<LatestDFSVersion> versionVersionedPrivateSpaceService(LatestPrivateSpaceImpl<LatestDFSVersion> impl);
}
