package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.storage.actions.StorageListService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.BasePrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListPrivateImplTest extends BaseMockitoTest {

    private static final String PATH = "./";
    private static final URI ABSOLUTE_PATH = URI.create("s3://absolute");

    private UserIDAuth auth = new UserIDAuth(new UserID(""), new ReadKeyPassword(""));

    @Mock
    private EncryptedResourceResolver resolver;

    @Mock
    private StorageListService listService;

    @InjectMocks
    private ListPrivateImpl inbox;

    @Test
    void list() {
        AbsoluteResourceLocation<PrivateResource> resource = BasePrivateResource.forAbsolutePrivate(ABSOLUTE_PATH);
        ListRequest<UserIDAuth, PrivateResource> request = ListRequest.forDefaultPrivate(auth, PATH);
        when(resolver.encryptAndResolvePath(request.getOwner(), request.getLocation())).thenReturn(resource);
        when(listService.list(resource)).thenReturn(Stream.of(resource));

        assertThat(inbox.list(request)).hasSize(1);
    }
}