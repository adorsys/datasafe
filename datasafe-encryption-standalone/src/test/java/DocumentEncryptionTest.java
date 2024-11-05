import de.adorsys.DocumentEncryption;
import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DocumentEncryptionTest extends BaseMockitoTest {
    private static final int LARGE_SIZE = 10 * 1024 * 1024 + 100;
    private KeyStore keystore;
    private KeyStoreAccess keyStoreAccess;
    private Uri dir;

    private final String testPath = "file:///somepath/to/test";

    @InjectMocks
    private DocumentEncryption documentEncryption;
    @Mock
    private Properties properties;
    @Mock
    private EncryptedDocumentReadService reader;

    @Mock
    private EncryptedDocumentWriteService writer;

    public DocumentEncryptionTest() {
    }

    @BeforeEach
    void setUp() {
        documentEncryption = new DocumentEncryption(properties, writer, reader);
        when(properties.getSystemRoot()).thenReturn("file:///somepath/to/test");
    }

    @SneakyThrows
    @Test
    void testEncryptWithPublicKey() {
        PublicKeyIDWithPublicKey publicKey = mock(PublicKeyIDWithPublicKey.class);
        PrivateKey privateKey = mock(PrivateKey.class);

        byte[] input = "Test content".getBytes(StandardCharsets.UTF_8);

        OutputStream os = mock(OutputStream.class);
        when(writer.write(any(Map.class), any(KeyPair.class))).thenReturn(os);

        documentEncryption.encryptWithPubKey(List.of(publicKey), privateKey, input, "testfile");

        verify(writer).write(any(Map.class), any(KeyPair.class));
        verify(os).write(input);
        verify(os).close();

    }
    @SneakyThrows
    @Test
    void testEncryptWithSecretKey() {
        SecretKeyIDWithKey secretKey = mock(SecretKeyIDWithKey.class);

        byte[] input = "Test content".getBytes(StandardCharsets.UTF_8);

        OutputStream os = mock(OutputStream.class);
        when(writer.write(any(), any(SecretKeyIDWithKey.class))).thenReturn(os);

        documentEncryption.encryptWithSecretKey(secretKey, input, "testfile");

        verify(writer).write(any(), any(SecretKeyIDWithKey.class));
        verify(os).write(input);
        verify(os).close();
    }
    @SneakyThrows
    @Test
    void testDecrypt(@TempDir Path tempDir) {
        String filename = "testfile";
        UserIDAuth user = mock(UserIDAuth.class);

        InputStream is = mock(InputStream.class);
        when(reader.read(any(ReadRequest.class))).thenReturn(is);
        when(properties.getSystemRoot()).thenReturn("file:///" + tempDir);

        documentEncryption.decrypt(filename, user);

        verify(reader).read(any(ReadRequest.class));
        verify(is).close();

    }

}
