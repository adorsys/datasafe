package de.adorsys.datasafe.simple.adapter.spring.factory;

import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import de.adorsys.datasafe.simple.adapter.impl.SimpleDatasafeServiceImpl;
import de.adorsys.datasafe.simple.adapter.impl.config.PathEncryptionConfig;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringDatasafeEncryptionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Slf4j
public class SpringSimpleDatasafeServiceFactory {

    @Autowired
    DFSCredentials wiredDfsCredentials;

    @Autowired
    SpringDatasafeEncryptionProperties encryptionProperties;

    DFSCredentials dfsCredentials;

    boolean useWiredCredentials = true;

    @PostConstruct
    public void postConstruct() {
        if (useWiredCredentials) {
            if (wiredDfsCredentials == null) {
                throw new RuntimeException("wiredDfsCredentials are nulL, so injection did not work");
            }
            dfsCredentials = wiredDfsCredentials;
        }
        log.info("encryptionProperties are:{}", encryptionProperties);
    }

    public SpringSimpleDatasafeServiceFactory() {
        useWiredCredentials = true;
    }

    public SpringSimpleDatasafeServiceFactory(DFSCredentials credentials, SpringDatasafeEncryptionProperties springDatasafeEncryptionProperties) {
        if (credentials == null) {
            throw new RuntimeException("dfs credentials passed in must not be null");
        }
        dfsCredentials = credentials;
        encryptionProperties = springDatasafeEncryptionProperties;
        useWiredCredentials = false;
    }

    public SimpleDatasafeService getSimpleDataSafeServiceWithSubdir(String subdirBelowRoot) {
        if (dfsCredentials instanceof AmazonS3DFSCredentials) {
            AmazonS3DFSCredentials amazonS3DFSCredentials = (AmazonS3DFSCredentials) dfsCredentials;
            return new SimpleDatasafeServiceImpl(
                amazonS3DFSCredentials.toBuilder().rootBucket(
                    amazonS3DFSCredentials.getRootBucket() + "/" + subdirBelowRoot
                ).build(),
                null != encryptionProperties ? encryptionProperties.getEncryption() : new MutableEncryptionConfig(),
                new PathEncryptionConfig(null == encryptionProperties ? true : encryptionProperties.getPathEncryption())
            );
        }
        if (dfsCredentials instanceof FilesystemDFSCredentials) {
            FilesystemDFSCredentials filesystemDFSCredentials = (FilesystemDFSCredentials) dfsCredentials;
            return new SimpleDatasafeServiceImpl(
                filesystemDFSCredentials.toBuilder().root(
                    filesystemDFSCredentials.getRoot() + "/" + subdirBelowRoot
                ).build(),
                null != encryptionProperties ? encryptionProperties.getEncryption() : new MutableEncryptionConfig(),
                new PathEncryptionConfig(null == encryptionProperties ? true : encryptionProperties.getPathEncryption())
            );
        }
        throw new SimpleAdapterException("missing switch for DFSCredentials" + dfsCredentials);
    }
}
