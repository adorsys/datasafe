package de.adorsys.datasafe.business.api.directory.privatespace.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;

import java.io.InputStream;

public interface ReadFromPrivate {

    InputStream read(ReadRequest<UserIDAuth> request);
}
