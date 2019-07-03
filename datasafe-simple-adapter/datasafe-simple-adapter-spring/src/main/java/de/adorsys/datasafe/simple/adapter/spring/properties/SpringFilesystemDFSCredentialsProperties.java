package de.adorsys.datasafe.simple.adapter.spring.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Created by peter on 05.10.18.
 */
@Component
@ConfigurationProperties(prefix = "docusafe.storeconnection.filesystem")
@Validated
@Getter
@Setter
@ToString
public class SpringFilesystemDFSCredentialsProperties {
    private final static String DEFAULT_ROOT = "datasafe-root";

    private final static Logger LOGGER = LoggerFactory.getLogger(SpringFilesystemDFSCredentialsProperties.class);
    public final static String template = "\n" +
            "datasafe:\n" +
            "  storeconnection:\n" +
            "    filesystem:\n" +
            "      rootbucket: (mandatory)\n";


    @Nullable
    private String rootbucket = DEFAULT_ROOT;

}
