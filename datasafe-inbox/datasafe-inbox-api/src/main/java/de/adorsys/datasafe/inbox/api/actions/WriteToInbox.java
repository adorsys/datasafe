package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.PublicResource;

import java.io.OutputStream;

public interface WriteToInbox {

    OutputStream write(WriteRequest<UserID, PublicResource> request);
}
