package de.adorsys.datasafe.business.impl.dfs;

import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.dfs.connection.impl.factory.DFSConnectionFactory;

import javax.inject.Inject;

// DEPLOYMENT
public class DFSConnectionServiceImpl {

    private static final DFSConnection CONN = DFSConnectionFactory.get();

    @Inject
    public DFSConnectionServiceImpl() {
    }

    public DFSConnection obtain(ResourceLocation connectTo) {
        return CONN;
    }
}
