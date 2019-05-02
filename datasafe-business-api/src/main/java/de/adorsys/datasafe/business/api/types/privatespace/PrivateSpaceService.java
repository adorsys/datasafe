package de.adorsys.datasafe.business.api.types.privatespace;

import de.adorsys.datasafe.business.api.types.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.types.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.api.types.privatespace.actions.WriteToPrivate;

public interface PrivateSpaceService extends ListPrivate, ReadFromPrivate, WriteToPrivate {
}
