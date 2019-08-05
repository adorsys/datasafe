package de.adorsys.datasafe.simple.adapter.spring.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

// TODO: it is far more than credentials, rename it and parent class to DFSConfig in next major release
@ConfigurationProperties(prefix = "datasafe.storeconnection")
@Validated
@Getter
@Setter
@ToString
public class SpringDFSCredentialProperties {

    @Nullable
    private SpringAmazonS3DFSCredentialsProperties amazons3;
    @Nullable
    private SpringFilesystemDFSCredentialsProperties filesystem;

}
