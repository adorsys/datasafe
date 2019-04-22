package de.adorsys.datasafe.business.impl.dfs;

import dagger.Module;
import dagger.Provides;

/**
 * This module is responsible for DFS connection retrieval.
 */
@Module
public abstract class DefaultDFSModule {

    @Provides
    abstract DFSConnectionServiceImpl dfsConnectionService(DFSConnectionServiceImpl impl);
}
