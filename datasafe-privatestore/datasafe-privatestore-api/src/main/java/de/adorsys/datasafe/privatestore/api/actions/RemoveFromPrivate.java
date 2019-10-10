package de.adorsys.datasafe.privatestore.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

/**
 * Removes users' file from privatespace.
 */
public interface RemoveFromPrivate {

    /**
     * Deletes users' file inside users' privatespace
     * @param request Resource location (relative or absolute)
     */
    void remove(RemoveRequest<UserIDAuth, PrivateResource> request);
    void makeSurePasswordClearanceIsDone(); // this abstract method will make sure new implementations dont forget to handle password clearance
}
