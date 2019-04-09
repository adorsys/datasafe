package de.adorsys.docusafe2.business.impl.dfs;

import dagger.Module;
import dagger.Provides;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;

@Module
public class DefaultDFSModule {

    @Provides
    public DFSConnectionService dfsConnectionService() {
        return new DFSConnectionServiceImpl();
    }
}
