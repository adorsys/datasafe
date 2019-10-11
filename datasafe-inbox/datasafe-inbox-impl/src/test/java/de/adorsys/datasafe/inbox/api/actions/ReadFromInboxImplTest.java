package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.inbox.impl.actions.ReadFromInboxImpl;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ReadFromInboxImplTest extends BaseMockitoTest {

    private static final String BYTES = "Hello";
    private static final URI ABSOLUTE_PATH = URI.create("s3://absolute");

    private UserIDAuth auth = new UserIDAuth(new UserID(""), ReadKeyPasswordTestFactory.getForString(""));

    @Mock
    private ResourceResolver resolver;

    @Mock
    private EncryptedDocumentReadService readService;

    @InjectMocks
    private ReadFromInboxImpl inbox;

    @Captor
    private ArgumentCaptor<ReadRequest<UserIDAuth, AbsoluteLocation<PrivateResource>>> captor;

    @Test
    void read() {
        AbsoluteLocation<PrivateResource> resource = BasePrivateResource.forAbsolutePrivate(ABSOLUTE_PATH);
        ReadRequest<UserIDAuth, PrivateResource> request = ReadRequest.forPrivate(
                auth,
                BasePrivateResource.forPrivate(ABSOLUTE_PATH)
        );
        when(resolver.resolveRelativeToPrivateInbox(request.getOwner(), request.getLocation())).thenReturn(resource);
        when(readService.read(captor.capture())).thenReturn(new ByteArrayInputStream(BYTES.getBytes()));

        assertThat(inbox.read(request)).hasContent(BYTES);
        assertThat(captor.getValue().getLocation()).isEqualTo(resource);
    }
}
