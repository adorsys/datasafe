package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;

import java.io.OutputStream;

public interface WriteToInbox {

    OutputStream write(WriteRequest<UserID, PublicResource> request);
}
