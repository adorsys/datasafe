package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class DFSTestCredentialsFactory {

    public DFSCredentials credentials(WithStorageProvider.StorageDescriptor descriptor) {
        switch (descriptor.getName()) {
            case FILESYSTEM: {
                log.info("uri:" + descriptor.getRootBucket());
                return FilesystemDFSCredentials
                    .builder()
                    .root(descriptor.getRootBucket())
                    .build();

            }
            case MINIO:
            case CEPH:
            case AMAZON: {
                descriptor.getStorageService().get();
                log.info("uri       :" + descriptor.getLocation());
                log.info("accesskey :" + descriptor.getAccessKey());
                log.info("secretkey :" + descriptor.getSecretKey());
                log.info("region    :" + descriptor.getRegion());
                log.info("rootbucket:" + descriptor.getRootBucket());
                log.info("mapped uri:" + descriptor.getMappedUrl());
                return AmazonS3DFSCredentials.builder()
                    .accessKey(descriptor.getAccessKey())
                    .secretKey(descriptor.getSecretKey())
                    .region(descriptor.getRegion())
                    .rootBucket(descriptor.getRootBucket())
                    .url(descriptor.getMappedUrl())
                    .build();
            }
            default:
                throw new SimpleAdapterException("missing switch for " + descriptor.getName());
        }
    }
}
