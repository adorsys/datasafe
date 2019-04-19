package de.adorsys.datasafe.business.api.deployment.privatespace.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;

import java.net.URI;
import java.util.stream.Stream;

public interface ListPrivate {

    Stream<URI> list(UserIDAuth forUser);
}
