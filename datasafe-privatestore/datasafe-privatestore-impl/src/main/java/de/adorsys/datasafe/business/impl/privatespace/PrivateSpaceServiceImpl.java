package de.adorsys.datasafe.business.impl.privatespace;

import de.adorsys.datasafe.business.api.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.api.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.privatespace.actions.ReadFromPrivate;
import de.adorsys.datasafe.business.api.privatespace.actions.RemoveFromPrivate;
import de.adorsys.datasafe.business.api.privatespace.actions.WriteToPrivate;
import lombok.experimental.Delegate;

import javax.inject.Inject;

public class PrivateSpaceServiceImpl implements PrivateSpaceService {

    @Delegate
    private final ListPrivate listPrivate;

    @Delegate
    private final ReadFromPrivate readFromPrivate;

    @Delegate
    private final WriteToPrivate writeToPrivate;

    @Delegate
    private final RemoveFromPrivate removefromPrivate;

    @Inject
    public PrivateSpaceServiceImpl(
            ListPrivate listPrivate,
            ReadFromPrivate readFromPrivate,
            WriteToPrivate writeToPrivate,
            RemoveFromPrivate removeFromPrivate) {
        this.listPrivate = listPrivate;
        this.readFromPrivate = readFromPrivate;
        this.writeToPrivate = writeToPrivate;
        this.removefromPrivate = removeFromPrivate;
    }
}
