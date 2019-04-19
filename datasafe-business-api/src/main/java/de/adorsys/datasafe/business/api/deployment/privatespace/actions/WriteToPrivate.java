package de.adorsys.datasafe.business.api.deployment.privatespace.actions;

import de.adorsys.datasafe.business.api.types.privatespace.PrivateWriteRequest;

import java.io.OutputStream;

public interface WriteToPrivate {

    OutputStream write(PrivateWriteRequest request);
}
