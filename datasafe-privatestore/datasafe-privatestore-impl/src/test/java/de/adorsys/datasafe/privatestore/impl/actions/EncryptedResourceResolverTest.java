package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EncryptedResourceResolverTest extends BaseMockitoTest {

    private static final String ENCRYPTED = "ENCRYPTED";
    private static final String DECRYPTED = "DECRYPTED";

    private PrivateResource root = BasePrivateResource.forPrivate(URI.create("s3://root/"));
    private PrivateResource absoluteEncrypted = BasePrivateResource.forPrivate(root.location().resolve(ENCRYPTED));
    private PrivateResource absolute = BasePrivateResource.forPrivate(URI.create("s3://root/bucket/"));
    private PrivateResource relative = BasePrivateResource.forPrivate(URI.create("./path"));
    private PrivateResource relativeEncrypted = BasePrivateResource.forPrivate(URI.create("./path/")
            .resolve(ENCRYPTED));
    private UserIDAuth auth = new UserIDAuth(new UserID(""), new ReadKeyPassword(""));

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private PathEncryption pathEncryption;

    @InjectMocks
    private EncryptedResourceResolverImpl resolver;

    @Captor
    private ArgumentCaptor<PrivateResource> captor;

    @BeforeEach
    void init() {
        when(resourceResolver.isAbsolute(any()))
                .thenAnswer(inv -> inv.getArgument(0, BasePrivateResource.class).location().isAbsolute());
    }

    @Test
    void encryptAndResolvePathAbsolute() {
        AbsoluteLocation<PrivateResource> resource = resolver.encryptAndResolvePath(auth, absolute);

        assertThat(resource.location()).isEqualTo(absolute.location());
        verify(pathEncryption, never()).encrypt(any(), any());
    }

    @Test
    void encryptAndResolvePathRelative() {
        when(pathEncryption.encrypt(auth, relative.location())).thenReturn(URI.create(ENCRYPTED));
        when(resourceResolver.resolveRelativeToPrivate(eq(auth), any()))
                .thenAnswer(inv -> BasePrivateResource.forAbsolutePrivate(root.location().resolve(ENCRYPTED)));

        AbsoluteLocation<PrivateResource> resource = resolver.encryptAndResolvePath(auth, relative);

        verify(resourceResolver).resolveRelativeToPrivate(eq(auth), captor.capture());
        assertThat(resource.location()).asString().isEqualTo("s3://root/" + ENCRYPTED);
        assertThat(captor.getValue().encryptedPath()).asString().isEqualTo(ENCRYPTED);
        assertThat(captor.getValue().decryptedPath()).asString().isEqualTo("path");
    }

    @Test
    void decryptAndResolvePathAbsolute() {
        when(pathEncryption.decrypt(auth, URI.create(ENCRYPTED))).thenReturn(URI.create(DECRYPTED));
        when(resourceResolver.resolveRelativeToPrivate(auth, absoluteEncrypted))
                .thenReturn(new AbsoluteLocation<>(absolute));

        AbsoluteLocation<PrivateResource> resource =
                resolver.decryptAndResolvePath(auth, absoluteEncrypted, root);

        assertThat(resource.location()).asString().isEqualTo("s3://root/bucket/" + ENCRYPTED);
        assertThat(resource.getResource().decryptedPath()).asString().isEqualTo(DECRYPTED);
        assertThat(resource.getResource().encryptedPath()).asString().isEqualTo(ENCRYPTED);
    }

    @Test
    void decryptAndResolvePathRelative() {
        when(pathEncryption.decrypt(auth, URI.create("path/" + ENCRYPTED))).thenReturn(URI.create(DECRYPTED));
        when(resourceResolver.resolveRelativeToPrivate(auth, relativeEncrypted))
                .thenReturn(new AbsoluteLocation<>(absolute));

        AbsoluteLocation<PrivateResource> resource =
                resolver.decryptAndResolvePath(auth, relativeEncrypted, root);

        assertThat(resource.location()).asString().isEqualTo("s3://root/bucket/path/" + ENCRYPTED);
        assertThat(resource.getResource().decryptedPath()).asString().isEqualTo(DECRYPTED);
        assertThat(resource.getResource().encryptedPath()).asString().isEqualTo("path/" + ENCRYPTED);
    }
}
