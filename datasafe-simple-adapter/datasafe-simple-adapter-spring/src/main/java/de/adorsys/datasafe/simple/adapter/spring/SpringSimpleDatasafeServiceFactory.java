package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import de.adorsys.datasafe.simple.adapter.impl.SimpleDatasafeServiceImpl;

public class SpringSimpleDatasafeServiceFactory {

    public SpringSimpleDatasafeServiceFactory(DFSCredentials dfsCredentials) {
        this.dfsCredentials = dfsCredentials;
    }

    private DFSCredentials dfsCredentials;

    public SimpleDatasafeService getSimpleDataSafeServiceWithSubdir(String subdirBelowRoot) {
        DFSCredentials d = null;
        if (dfsCredentials instanceof AmazonS3DFSCredentials) {
            AmazonS3DFSCredentials amazonS3DFSCredentials = (AmazonS3DFSCredentials) dfsCredentials;
            d = AmazonS3DFSCredentials.builder()
                    .accessKey(amazonS3DFSCredentials.getAccessKey())
                    .secretKey(amazonS3DFSCredentials.getSecretKey())
                    .region(amazonS3DFSCredentials.getRegion())
                    .url(amazonS3DFSCredentials.getUrl())
                    .rootBucket(amazonS3DFSCredentials.getRootBucket() + "/" + subdirBelowRoot)
                    .build();
        }
        if (dfsCredentials instanceof FilesystemDFSCredentials) {
            FilesystemDFSCredentials filesystemDFSCredentials = (FilesystemDFSCredentials) dfsCredentials;
            d = FilesystemDFSCredentials.builder()
                    .root(filesystemDFSCredentials.getRoot() + "/" + subdirBelowRoot)
                    .build();

        }
        if (d == null) {
            throw new SimpleAdapterException("missing switch for DFSCredentials" + dfsCredentials);
        }

        return new SimpleDatasafeServiceImpl(d);
    }
}
