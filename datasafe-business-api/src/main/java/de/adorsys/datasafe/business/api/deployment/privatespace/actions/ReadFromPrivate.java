package de.adorsys.datasafe.business.api.deployment.privatespace.actions;

import de.adorsys.datasafe.business.api.types.privatespace.PrivateReadRequest;

import java.io.InputStream;

public interface ReadFromPrivate {

    InputStream read(PrivateReadRequest request);
}
