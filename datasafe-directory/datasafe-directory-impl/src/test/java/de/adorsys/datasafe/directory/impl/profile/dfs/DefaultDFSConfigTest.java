package de.adorsys.datasafe.directory.impl.profile.dfs;

import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class DefaultDFSConfigTest {
    String uriString = "https://192.168.178.0.1:9090/minio/first/folder";

    @Test
    public void checkStringWithSlash() {
        String result1 = DefaultDFSConfig.addTrailingSlashIfNeeded(uriString);
        Assertions.assertEquals(uriString + "/", result1);
        String result2 = DefaultDFSConfig.addTrailingSlashIfNeeded(result1);
        Assertions.assertEquals(uriString + "/", result2);
    }

    @Test
    @SneakyThrows
    public void checkURIWithSlash() {
        URI uri = new URI(uriString);
        URI result1 = DefaultDFSConfig.addTrailingSlashIfNeeded(uri);
        Assertions.assertEquals(new URI(uriString + "/"), result1);
        URI result2 = DefaultDFSConfig.addTrailingSlashIfNeeded(result1);
        Assertions.assertEquals(new URI(uriString + "/"), result2);
    }


    @Test
    public void checkUriWithSlash() {
        Uri uri = new Uri(uriString);
        Uri result1 = DefaultDFSConfig.addTrailingSlashIfNeeded(uri);
        Assertions.assertEquals(new Uri(uriString + "/"), result1);
        Uri result2 = DefaultDFSConfig.addTrailingSlashIfNeeded(result1);
        Assertions.assertEquals(new Uri(uriString + "/"), result2);
    }

}
