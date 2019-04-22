package de.adorsys.datasafe.business.api.directory.privatespace.actions;

import de.adorsys.datasafe.business.api.types.action.PrivateWriteRequest;

import java.io.OutputStream;

public interface WriteToPrivate {

    OutputStream write(PrivateWriteRequest request);
}
