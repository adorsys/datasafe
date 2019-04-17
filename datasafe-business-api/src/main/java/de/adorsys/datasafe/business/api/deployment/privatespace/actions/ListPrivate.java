package de.adorsys.datasafe.business.api.deployment.privatespace.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateBucketPath;

import java.util.stream.Stream;

public interface ListPrivate {

    Stream<PrivateBucketPath> list(UserIDAuth forUser);
}
