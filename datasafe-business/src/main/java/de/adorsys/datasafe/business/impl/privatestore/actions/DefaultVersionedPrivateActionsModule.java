package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.business.impl.version.latest.LatestPrivateSpaceImpl;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;

/**
 * This module is responsible for providing default actions on PRIVATE folder.
 */
@Module
public abstract class DefaultVersionedPrivateActionsModule {

    @Binds
    abstract VersionedPrivateSpaceService<LatestDFSVersion> versionVersionedPrivateSpaceService(LatestPrivateSpaceImpl<LatestDFSVersion> impl);
}
