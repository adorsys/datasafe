package de.adorsys.datasafe.business.impl.privatespace;

import de.adorsys.datasafe.business.impl.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.impl.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.impl.privatespace.actions.WriteToPrivate;
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
