package de.adorsys.datasafe.business.api.privatespace;

import de.adorsys.datasafe.business.api.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.privatespace.actions.WriteToPrivate;
import de.adorsys.datasafe.business.api.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.api.privatespace.actions.RemoveFromPrivate;

public interface PrivateSpaceService extends ListPrivate, ReadFromPrivate, WriteToPrivate, RemoveFromPrivate {
}
