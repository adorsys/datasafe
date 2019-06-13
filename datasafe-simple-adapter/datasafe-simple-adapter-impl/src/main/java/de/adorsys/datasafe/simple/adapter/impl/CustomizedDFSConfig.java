package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.types.api.resource.Uri;

public class CustomizedDFSConfig extends DefaultDFSConfig {
    public CustomizedDFSConfig(Uri systemRoot, ReadStorePassword systemPassword) {
        super(systemRoot, systemPassword);
    }
}
