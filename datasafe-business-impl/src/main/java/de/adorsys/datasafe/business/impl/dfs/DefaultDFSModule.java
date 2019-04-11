package de.adorsys.datasafe.business.impl.dfs;

import dagger.Binds;
import dagger.Module;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;

/**
 * This module is responsible for DFS connection retrieval.
 */
@Module
public abstract class DefaultDFSModule {

    @Binds
    abstract DFSConnectionService dfsConnectionService(DFSConnectionServiceImpl impl);
}
