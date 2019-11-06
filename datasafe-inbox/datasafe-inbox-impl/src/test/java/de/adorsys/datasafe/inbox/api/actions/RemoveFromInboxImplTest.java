package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.inbox.impl.actions.RemoveFromInboxImpl;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BaseResolvedResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;
import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoveFromInboxImplTest extends BaseMockitoTest {

    private static final String PATH = "./";
    private static final URI ABSOLUTE_PATH = URI.create("s3://absolute");

    private UserIDAuth auth = new UserIDAuth(new UserID(""), ReadKeyPasswordTestFactory.getForString(""));

    @Mock
    private PrivateKeyService privateKeyService;

    @Mock
    private AbsoluteLocation<ResolvedResource> absoluteResolvedResource;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private StorageRemoveService removeService;

    @InjectMocks
    private RemoveFromInboxImpl inbox;

    @Test
    void remove() {
        AbsoluteLocation<PrivateResource> resource = BasePrivateResource.forAbsolutePrivate(ABSOLUTE_PATH);
        RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forDefaultPrivate(auth, new Uri(PATH));
        when(resolver.resolveRelativeToPrivateInbox(request.getOwner(), request.getLocation())).thenReturn(resource);
        when(absoluteResolvedResource.location()).thenReturn(new Uri(ABSOLUTE_PATH));
        when(absoluteResolvedResource.getResource()).thenReturn(new BaseResolvedResource(
                BasePrivateResource.forPrivate(ABSOLUTE_PATH), Instant.now()));

        inbox.remove(request);

        verify(removeService).remove(resource);
        verify(privateKeyService).validateUserHasAccessOrThrow(auth);
    }
}
