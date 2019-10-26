package de.adorsys.datasafe.rest.impl.config;

import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datasafe")
@Data
public class DatasafeProperties {

    /**
     * S3 bucket name, used when creating S3 client.
     */
    private String bucketName;

    /**
     * Location within storage system where to store user metainformation (path to his private folder, etc.).
     * By default, Datasafe stores user profile data in json files that are located relative to this folder.
     */
    private String systemRoot;

    /**
     * Password used to open keystore. It is not sufficient to read private/secret keys with it.
     */
    private String keystorePassword;

    private String amazonUrl;
    private String amazonAccessKeyID;
    private String amazonSecretAccessKey;
    private String amazonRegion;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    /**
     * From where to serve static resources.
     */
    private String staticResources;

    private MutableEncryptionConfig encryption = new MutableEncryptionConfig();
}
