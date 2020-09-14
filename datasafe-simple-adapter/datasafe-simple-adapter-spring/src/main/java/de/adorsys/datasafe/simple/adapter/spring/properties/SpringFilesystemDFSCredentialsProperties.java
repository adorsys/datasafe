package de.adorsys.datasafe.simple.adapter.spring.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "datasafe.storeconnection.filesystem")
@Validated
@Getter
@Setter
@ToString
public class SpringFilesystemDFSCredentialsProperties {
    private final static String DEFAULT_ROOT = "datasafe-root";
    public final static String template = "\n" +
            "datasafe:\n" +
            "  pathEncryption: (optional, default true)\n" +
            "  storeconnection:\n" +
            "    filesystem:\n" +
            "      rootbucket: (mandatory)\n";


    @Nullable
    private String rootbucket = DEFAULT_ROOT;

}
