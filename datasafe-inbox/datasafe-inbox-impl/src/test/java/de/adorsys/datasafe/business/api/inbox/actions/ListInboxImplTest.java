package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.storage.StorageListService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.net.URI;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListInboxImplTest extends BaseMockitoTest {

    private static final String PATH = "./";
    private static final URI ABSOLUTE_PATH = URI.create("s3://absolute");

    @Spy
    private UserIDAuth auth;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private StorageListService listService;

    @InjectMocks
    private ListInboxImpl inbox;

    @Test
    void list() {
        AbsoluteResourceLocation<PrivateResource> resource = DefaultPrivateResource.forAbsolutePrivate(ABSOLUTE_PATH);
        ListRequest<UserIDAuth, PrivateResource> request = ListRequest.forDefaultPrivate(auth, PATH);
        when(resolver.resolveRelativeToPrivateInbox(request.getOwner(), request.getLocation())).thenReturn(resource);
        when(listService.list(resource)).thenReturn(Stream.of(resource));

        assertThat(inbox.list(request)).hasSize(1);
    }
}