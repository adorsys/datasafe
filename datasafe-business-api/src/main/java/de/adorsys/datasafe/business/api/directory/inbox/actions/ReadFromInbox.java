package de.adorsys.datasafe.business.api.directory.inbox.actions;

import de.adorsys.datasafe.business.api.types.action.ReadRequest;

import java.io.InputStream;

public interface ReadFromInbox {

    InputStream read(ReadRequest request);
}
