package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class WriteToPrivateImplTest extends BaseMockitoTest {

    private static final String BYTES = "Hello";
    private static final URI ABSOLUTE_PATH = URI.create("s3://absolute");

    private UserIDAuth auth = new UserIDAuth(new UserID(""), new ReadKeyPassword(""));

    private SecretKeyIDWithKey secretKeyIDWithKey;

    @Mock
    private SecretKey secretKey;

    @Mock
    private PrivateKeyService privateKeyService;

    @Mock
    private EncryptedResourceResolver resolver;

    @Mock
    private EncryptedDocumentWriteService writeService;

    @InjectMocks
    private WriteToPrivateImpl inbox;

    @BeforeEach
    void init() {
        this.secretKeyIDWithKey = new SecretKeyIDWithKey(new KeyID(""), secretKey);
    }

    @Test
    @SneakyThrows
    void write() {
        AbsoluteLocation<PrivateResource> resource = BasePrivateResource.forAbsolutePrivate(ABSOLUTE_PATH);
        WriteRequest<UserIDAuth, PrivateResource> request = WriteRequest.forDefaultPrivate(auth, ABSOLUTE_PATH);
        when(privateKeyService.documentEncryptionSecretKey(auth)).thenReturn(secretKeyIDWithKey);
        when(resolver.encryptAndResolvePath(request.getOwner(), request.getLocation())).thenReturn(resource);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(writeService.write(resource, secretKeyIDWithKey)).thenReturn(outputStream);

        inbox.write(request).write(BYTES.getBytes());

        assertThat(outputStream.toByteArray()).contains(BYTES.getBytes());
    }
}
