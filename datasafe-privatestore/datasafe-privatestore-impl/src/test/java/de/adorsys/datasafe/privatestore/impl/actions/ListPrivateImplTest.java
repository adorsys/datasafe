package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import org.junit.jupiter.api.BeforeEach;
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

    private UserIDAuth auth = new UserIDAuth(new UserID(""), ReadKeyPasswordTestFactory.getForString(""));

    @Mock
    private ResolvedResource resolvedResource;

    @Mock
    private AbsoluteLocation<ResolvedResource> absoluteResolvedResource;

    @Mock
    private EncryptedResourceResolver resolver;

    @Mock
    private StorageListService listService;

    @InjectMocks
    private ListPrivateImpl privateService;

    @BeforeEach
    void init() {
        when(absoluteResolvedResource.getResource()).thenReturn(resolvedResource);
        when(resolvedResource.location()).thenReturn(new Uri(ABSOLUTE_PATH));
    }

    @Test
    void list() {
        AbsoluteLocation<PrivateResource> resource = BasePrivateResource.forAbsolutePrivate(ABSOLUTE_PATH);
        when(resolvedResource.asPrivate()).thenReturn(resource.getResource());
        when(resolvedResource.withResource(resource.getResource())).thenReturn(resolvedResource);
        ListRequest<UserIDAuth, PrivateResource> request = ListRequest.forDefaultPrivate(auth, PATH);
        when(resolver.encryptAndResolvePath(request.getOwner(), request.getLocation(), request.getStorageIdentifier()))
            .thenReturn(resource);
        when(resolver.decryptingResolver(request.getOwner(), resource.getResource(), request.getStorageIdentifier()))
                .thenReturn(path -> resource);
        when(listService.list(resource)).thenReturn(Stream.of(absoluteResolvedResource));

        assertThat(privateService.list(request)).hasSize(1);
    }
}
