package de.adorsys.datasafe.privatestore.api;

import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.privatestore.api.actions.ReadFromPrivate;
import de.adorsys.datasafe.privatestore.api.actions.RemoveFromPrivate;
import de.adorsys.datasafe.privatestore.api.actions.WriteToPrivate;

/**
 * Aggregate view of operations possible with users' privatespace. Users' privatespace - encrypted storage
 * of users' private files.
 */
public interface PrivateSpaceService extends ListPrivate, ReadFromPrivate, WriteToPrivate, RemoveFromPrivate {
}
