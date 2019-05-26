package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.inbox.impl.actions.ListInboxImpl;
import de.adorsys.datasafe.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListInboxImplTest extends BaseMockitoTest {

    private static final String PATH = "./";
    private static final URI ABSOLUTE_PATH = URI.create("s3://absolute");

    private UserIDAuth auth = new UserIDAuth(new UserID(""), new ReadKeyPassword(""));

    @Mock
    private ResolvedResource resolvedResource;

    @Mock
    private AbsoluteLocation<ResolvedResource> absoluteResolvedResource;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private StorageListService listService;

    @InjectMocks
    private ListInboxImpl inbox;

    @Test
    void list() {
        AbsoluteLocation<PrivateResource> resource = BasePrivateResource.forAbsolutePrivate(ABSOLUTE_PATH);
        when(resolvedResource.asPrivate()).thenReturn(resource.getResource());
        when(resolvedResource.withResource(resource.getResource())).thenReturn(resolvedResource);
        ListRequest<UserIDAuth, PrivateResource> request = ListRequest.forDefaultPrivate(auth, PATH);
        when(resolver.resolveRelativeToPrivateInbox(request.getOwner(), request.getLocation())).thenReturn(resource);
        when(listService.list(resource)).thenReturn(Stream.of(absoluteResolvedResource));

        assertThat(inbox.list(request)).hasSize(1);
    }
}
