package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.directory.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.inbox.impl.actions.WriteToInboxImpl;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Set;

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
        WriteRequest<Set<UserID>, PublicResource> request = WriteRequest
                .forDefaultPublic(Collections.singleton(auth), ABSOLUTE_PATH);
        when(publicKeyService.publicKey(auth)).thenReturn(publicKeyWithId);
        when(resolver.resolveRelativeToPublicInbox(auth, request.getLocation())).thenReturn(resource);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(writeService.write(Collections.singletonMap(publicKeyWithId, resource))).thenReturn(outputStream);

        inbox.write(request).write(BYTES.getBytes());

        assertThat(outputStream.toByteArray()).contains(BYTES.getBytes());
    }
}
