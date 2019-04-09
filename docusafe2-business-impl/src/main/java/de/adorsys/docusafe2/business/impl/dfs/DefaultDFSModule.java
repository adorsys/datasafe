package de.adorsys.docusafe2.business.impl.dfs;

import dagger.Binds;
import dagger.Module;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;

/**
 * This module is responsible for DFS connection retreival.
 */
@Module
public abstract class DefaultDFSModule {

    @Binds
    abstract DFSConnectionService dfsConnectionService(DFSConnectionServiceImpl impl);
}
