package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.impl.SimpleDatasafeServiceImpl;

public class SpringSimpleDatasafeServiceFactory {

    public SpringSimpleDatasafeServiceFactory(DFSCredentials dfsCredentials) {
        this.dfsCredentials = dfsCredentials;
    }

    private DFSCredentials dfsCredentials;

    public SimpleDatasafeService getSimpleDataSafeServiceWithSubdir(String subdirBelowRoot) {
        return new SimpleDatasafeServiceImpl();
    }
}
