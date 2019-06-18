package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentialsFactory;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class DFSCredentialsFactoryTest {
    String amazonBeforeTest;
    String fsBeforeTest;
    @BeforeEach
    public void before() {
        String amazonBeforeTest = System.getProperty(DFSCredentialsFactory.AMAZON_ENV);
        String fsBeforeTest = System.getProperty(DFSCredentialsFactory.FILESYSTEM_ENV);
        Properties sysProps = System.getProperties();
        sysProps.remove(DFSCredentialsFactory.AMAZON_ENV);
        sysProps.remove(DFSCredentialsFactory.FILESYSTEM_ENV);
    }

    @AfterEach
    public void after() {
        if (amazonBeforeTest == null) {
            System.getProperties().remove(DFSCredentialsFactory.AMAZON_ENV);
        } else {
            System.setProperty(DFSCredentialsFactory.AMAZON_ENV, amazonBeforeTest);
        }

        if (fsBeforeTest == null) {
            System.getProperties().remove(DFSCredentialsFactory.FILESYSTEM_ENV);
        } else {
            System.setProperty(DFSCredentialsFactory.FILESYSTEM_ENV, fsBeforeTest);
        }

    }
    @Test
    public void forFileSystem() {
        DFSCredentials dfsCredentials = DFSCredentialsFactory.getFromEnvironmnet();
        Assertions.assertTrue(dfsCredentials instanceof FilesystemDFSCredentials);
        String defaultPath = "affe/1/2/3";
        System.setProperty(DFSCredentialsFactory.FILESYSTEM_ENV, defaultPath);
        dfsCredentials = DFSCredentialsFactory.getFromEnvironmnet();
        Assertions.assertTrue(dfsCredentials instanceof FilesystemDFSCredentials);
        FilesystemDFSCredentials filesystemDFSCredentials = (FilesystemDFSCredentials) dfsCredentials;
        Assertions.assertTrue(filesystemDFSCredentials.getRoot().toString().endsWith(defaultPath));
    }


    @Test
    public void forAmazon() {
        String uri = "http://go.here";
        String accessKey = "accesskey";
        String secretKey = "secretKey";
        String region = "region";
        String bucket = "/root/bucket/a";

        System.setProperty(DFSCredentialsFactory.AMAZON_ENV, uri + "," + accessKey + "," + secretKey + "," + region + "," + bucket);
        DFSCredentials dfsCredentials = DFSCredentialsFactory.getFromEnvironmnet();
        Assertions.assertTrue(dfsCredentials instanceof AmazonS3DFSCredentials);
        AmazonS3DFSCredentials amazonS3DFSCredentials = (AmazonS3DFSCredentials) dfsCredentials;
        Assertions.assertEquals(accessKey, amazonS3DFSCredentials.getAccessKey());
        Assertions.assertEquals(secretKey, amazonS3DFSCredentials.getSecretKey());
        Assertions.assertEquals(bucket, amazonS3DFSCredentials.getRootBucket());
        Assertions.assertEquals(region, amazonS3DFSCredentials.getRegion());
        Assertions.assertEquals(uri, amazonS3DFSCredentials.getUrl());
    }
}
