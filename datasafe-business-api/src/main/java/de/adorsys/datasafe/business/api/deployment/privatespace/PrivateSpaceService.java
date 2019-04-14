package de.adorsys.datasafe.business.api.deployment.privatespace;

import de.adorsys.datasafe.business.api.deployment.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.deployment.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.api.deployment.privatespace.actions.WriteToPrivate;

public interface PrivateSpaceService extends ListPrivate, ReadFromPrivate, WriteToPrivate {
}
