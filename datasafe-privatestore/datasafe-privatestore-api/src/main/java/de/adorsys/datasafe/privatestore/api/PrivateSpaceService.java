package de.adorsys.datasafe.privatestore.api;

import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.privatestore.api.actions.WriteToPrivate;
import de.adorsys.datasafe.privatestore.api.actions.ReadFromPrivate;
import de.adorsys.datasafe.privatestore.api.actions.RemoveFromPrivate;

public interface PrivateSpaceService extends ListPrivate, ReadFromPrivate, WriteToPrivate, RemoveFromPrivate {
}
