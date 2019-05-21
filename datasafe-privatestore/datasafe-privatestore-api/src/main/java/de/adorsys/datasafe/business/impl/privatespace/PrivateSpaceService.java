package de.adorsys.datasafe.business.impl.privatespace;

import de.adorsys.datasafe.business.impl.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.impl.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.impl.privatespace.actions.RemoveFromPrivate;
import de.adorsys.datasafe.business.impl.privatespace.actions.WriteToPrivate;

public interface PrivateSpaceService extends ListPrivate, ReadFromPrivate, WriteToPrivate, RemoveFromPrivate {
}
