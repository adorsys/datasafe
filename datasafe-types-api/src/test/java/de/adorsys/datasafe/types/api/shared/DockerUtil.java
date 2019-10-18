package de.adorsys.datasafe.types.api.shared;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.net.URI;

@UtilityClass
public class DockerUtil {

    @SneakyThrows
    public static String getDockerUri(String defaultUri) {
        String dockerHost = System.getenv("DOCKER_HOST");
        if (dockerHost == null) {
            return defaultUri;
        }

        URI dockerUri = new URI(dockerHost);
        return "http://" + dockerUri.getHost();
    }
}
