package de.adorsys.datasafe.simple.adapter.spring;

import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringAmazonS3DFSCredentialsProperties;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringDFSCredentialProperties;
import de.adorsys.datasafe.simple.adapter.spring.properties.SpringFilesystemDFSCredentialsProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringPropertiesToDFSCredentialsUtil {
    static public DFSCredentials dfsCredentials(SpringDFSCredentialProperties properties) {
        DFSCredentials dfsCredentials = null;
        if (properties.getAmazons3() != null) {
            SpringAmazonS3DFSCredentialsProperties props = properties.getAmazons3();
            dfsCredentials = AmazonS3DFSCredentials.builder()
                    .rootBucket(props.getRootbucket())
                    .accessKey(props.getAccesskey())
                    .secretKey(props.getSecretkey())
                    .region(props.getRegion())
                    .url(props.getUrl())
                    .noHttps(props.isNohttps())
                    .threadPoolSize(props.getThreadpoolsize())
                    .requestTimeout(props.getRequesttimeout())
                    .maxConnections(props.getMaxconnections())
                    .build();
        }
        if (properties.getFilesystem() != null) {
            SpringFilesystemDFSCredentialsProperties props = properties.getFilesystem();
            dfsCredentials = FilesystemDFSCredentials.builder()
                    .root(props.getRootbucket())
                    .build();
        }
        if (dfsCredentials == null) {
            throw new SimpleAdapterException("Spring properties are not set correctly. Please use at least one of those:\n" + SpringFilesystemDFSCredentialsProperties.template + SpringAmazonS3DFSCredentialsProperties.template);
        }

        return dfsCredentials;
    }

}
