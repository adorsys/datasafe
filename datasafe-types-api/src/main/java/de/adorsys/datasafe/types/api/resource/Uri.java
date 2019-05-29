package de.adorsys.datasafe.types.api.resource;

import de.adorsys.datasafe.types.api.utils.Log;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class that prevents leaking of URI content.
 */
@UtilityClass
public class Uri {

    @SneakyThrows
    public URI build(String path) {
        try {
            return new URI(path);
        } catch (URISyntaxException ex) {
            throw new URISyntaxException(Log.secure(ex.getInput(), "/"), ex.getReason());
        }
    }
}
