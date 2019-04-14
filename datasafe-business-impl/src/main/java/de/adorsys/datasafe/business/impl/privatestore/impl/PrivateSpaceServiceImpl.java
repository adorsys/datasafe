package de.adorsys.datasafe.business.impl.privatestore.impl;

import de.adorsys.datasafe.business.api.deployment.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.api.deployment.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.deployment.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.api.deployment.privatespace.actions.WriteToPrivate;
import lombok.experimental.Delegate;

import javax.inject.Inject;

public class PrivateSpaceServiceImpl implements PrivateSpaceService {

    @Delegate
    private final ListPrivate listPrivate;

    @Delegate
    private final ReadFromPrivate readFromPrivate;

    @Delegate
    private final WriteToPrivate writeToPrivate;

    @Inject
    public PrivateSpaceServiceImpl(
            ListPrivate listInbox,
            ReadFromPrivate readDocumentFromInbox,
            WriteToPrivate writeDocumentToInbox) {
        this.listPrivate = listInbox;
        this.readFromPrivate = readDocumentFromInbox;
        this.writeToPrivate = writeDocumentToInbox;
    }
}
