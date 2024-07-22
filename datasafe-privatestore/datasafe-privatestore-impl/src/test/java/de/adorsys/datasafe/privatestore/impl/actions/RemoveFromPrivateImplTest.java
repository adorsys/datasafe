package de.adorsys.datasafe.privatestore.impl.actions;


import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RemoveFromPrivateImplTest extends BaseMockitoTest {
    private static final URI ABSOLUTE_PATH = URI.create("s3://absolute");
    private static final String PATH = "./";
    private final UserIDAuth auth = new UserIDAuth(new UserID(""), ReadKeyPasswordTestFactory.getForString(""));
    @Mock
    private EncryptedResourceResolver resolver;
    @Mock
    private StorageRemoveService removeService;
    private RemoveFromPrivateImpl removeFromPrivate;

    @Test
    @SneakyThrows
    void removePrivate() {
        removeFromPrivate = new RemoveFromPrivateImpl(resolver, removeService);
        AbsoluteLocation<PrivateResource> resource = BasePrivateResource.forAbsolutePrivate(ABSOLUTE_PATH);
        RemoveRequest<UserIDAuth, PrivateResource> removeReq = RemoveRequest.forDefaultPrivate(auth, new Uri(PATH));
        when(resolver.encryptAndResolvePath(removeReq.getOwner(), removeReq.getLocation(), removeReq.getStorageIdentifier()))
                .thenReturn(resource);
        removeFromPrivate.remove(removeReq);

        verify(removeService).remove(resource);
    }
}
