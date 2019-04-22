package de.adorsys.datasafe.business.api.directory.privatespace;

import de.adorsys.datasafe.business.api.directory.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.directory.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.api.directory.privatespace.actions.WriteToPrivate;

public interface PrivateSpaceService extends ListPrivate, ReadFromPrivate, WriteToPrivate {
}
