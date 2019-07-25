package de.adorsys.datasafe.simple.adapter.spring.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * Created by peter on 04.10.18.
 */
@ConfigurationProperties(prefix = "datasafe.storeconnection.amazons3")
@Validated
@Getter
@Setter
@ToString
public class SpringAmazonS3DFSCredentialsProperties {
    private final static String DEFAULT_REGION = "eu-central-1";
    private final static String DEFAULT_ROOT = "datasafe-root";
    public final static String template = "\n" +
            "datasafe:\n" +
            "  storeconnection:\n" +
            "    amazons3:\n" +
            "      url: (mandatory)\n" +
            "      accesskey: (mandatory)\n" +
            "      secretkey: (mandatory)\n" +
            "      region: (optional)\n" +
            "      rootbucket: (optional)\n" +
            "      nohttps: (optional, default false - use https to reach s3 endpoint)\n" +
            "      threadpoolsize: (optional, default 5, how many workers should send chunk requests)\n"
            ;

    private String url;
    private String accesskey;
    private String secretkey;

    @Nullable
    private String region = DEFAULT_REGION;

    @Nullable
    private String rootbucket = DEFAULT_ROOT;

    private boolean noHttps = true;
    private int threadPoolSize = 5;
}
