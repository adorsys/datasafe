package de.adorsys.datasafe.business.api.deployment.privatespace.actions;

import de.adorsys.datasafe.business.api.types.privatespace.PrivateWriteRequest;

public interface WriteToPrivate {

    void write(PrivateWriteRequest request);
}
