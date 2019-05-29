package de.adorsys.datasafe.directory.api.config;

import de.adorsys.datasafe.types.api.resource.Uri;

public interface DFSConfig {

    String keystorePassword();
    Uri systemRoot();
}
