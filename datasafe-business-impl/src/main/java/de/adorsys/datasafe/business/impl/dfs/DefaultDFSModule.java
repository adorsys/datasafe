package de.adorsys.datasafe.business.impl.dfs;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * This module is responsible for DFS connection retrieval.
 */
@Module
public abstract class DefaultDFSModule {

    @Binds
    abstract DFSConnectionServiceImpl dfsConnectionService(DFSConnectionServiceImpl impl);
}
