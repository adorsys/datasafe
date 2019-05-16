package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.KeyID;
import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.BasePublicResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class WriteToInboxImplTest extends BaseMockitoTest {

    private static final String BYTES = "Hello";
    private static final URI ABSOLUTE_PATH = URI.create("s3://absolute");

    private UserID auth = new UserID("");

    private PublicKeyIDWithPublicKey publicKeyWithId;

    @Mock
    private PublicKey publicKey;

    @Mock
    private PublicKeyService publicKeyService;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private EncryptedDocumentWriteService writeService;

    @InjectMocks
    private WriteToInboxImpl inbox;

    @BeforeEach
    void init() {
        this.publicKeyWithId = new PublicKeyIDWithPublicKey(new KeyID(""), publicKey);
    }

    @Test
    @SneakyThrows
    void write() {
        AbsoluteLocation<PublicResource> resource = BasePublicResource.forAbsolutePublic(ABSOLUTE_PATH);
        WriteRequest<UserID, PublicResource> request = WriteRequest.forDefaultPublic(auth, ABSOLUTE_PATH);
        when(publicKeyService.publicKey(auth)).thenReturn(publicKeyWithId);
        when(resolver.resolveRelativeToPublicInbox(request.getOwner(), request.getLocation())).thenReturn(resource);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(writeService.write(resource, publicKeyWithId)).thenReturn(outputStream);

        inbox.write(request).write(BYTES.getBytes());

        assertThat(outputStream.toByteArray()).contains(BYTES.getBytes());
    }
}