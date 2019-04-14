package de.adorsys.datasafe.business.api.deployment.privatespace.actions;

import de.adorsys.datasafe.business.api.types.privatespace.PrivateReadRequest;

public interface ReadFromPrivate {

    void read(PrivateReadRequest request);
}
