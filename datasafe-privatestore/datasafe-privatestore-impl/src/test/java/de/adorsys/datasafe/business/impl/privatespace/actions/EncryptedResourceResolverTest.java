package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.shared.BaseMockitoTest;
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

    private PrivateResource root = DefaultPrivateResource.forPrivate(URI.create("s3://root/"));
    private PrivateResource absoluteEncrypted = DefaultPrivateResource.forPrivate(root.location().resolve(ENCRYPTED));
    private PrivateResource absolute = DefaultPrivateResource.forPrivate(URI.create("s3://root/bucket/"));
    private PrivateResource relative = DefaultPrivateResource.forPrivate(URI.create("./path"));
    private PrivateResource relativeEncrypted = DefaultPrivateResource.forPrivate(URI.create("./path/")
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
                .thenAnswer(inv -> inv.getArgument(0, DefaultPrivateResource.class).location().isAbsolute());
    }

    @Test
    void encryptAndResolvePathAbsolute() {
        AbsoluteResourceLocation<PrivateResource> resource = resolver.encryptAndResolvePath(auth, absolute);

        assertThat(resource.location()).isEqualTo(absolute.location());
        verify(pathEncryption, never()).encrypt(any(), any());
    }

    @Test
    void encryptAndResolvePathRelative() {
        when(pathEncryption.encrypt(auth, relative.location())).thenReturn(URI.create(ENCRYPTED));
        when(resourceResolver.resolveRelativeToPrivate(eq(auth), any()))
                .thenAnswer(inv -> DefaultPrivateResource.forAbsolutePrivate(root.location().resolve(ENCRYPTED)));

        AbsoluteResourceLocation<PrivateResource> resource = resolver.encryptAndResolvePath(auth, relative);

        verify(resourceResolver).resolveRelativeToPrivate(eq(auth), captor.capture());
        assertThat(resource.location()).asString().isEqualTo("s3://root/" + ENCRYPTED);
        assertThat(captor.getValue().encryptedPath()).asString().isEqualTo(ENCRYPTED);
        assertThat(captor.getValue().decryptedPath()).asString().isEqualTo("path");
    }

    @Test
    void decryptAndResolvePathAbsolute() {
        when(pathEncryption.decrypt(auth, URI.create(ENCRYPTED))).thenReturn(URI.create(DECRYPTED));
        when(resourceResolver.resolveRelativeToPrivate(auth, absoluteEncrypted))
                .thenReturn(new AbsoluteResourceLocation<>(absolute));

        AbsoluteResourceLocation<PrivateResource> resource =
                resolver.decryptAndResolvePath(auth, absoluteEncrypted, root);

        assertThat(resource.location()).asString().isEqualTo("s3://root/bucket/" + ENCRYPTED);
        assertThat(resource.getResource().decryptedPath()).asString().isEqualTo(DECRYPTED);
        assertThat(resource.getResource().encryptedPath()).asString().isEqualTo(ENCRYPTED);
    }

    @Test
    void decryptAndResolvePathRelative() {
        when(pathEncryption.decrypt(auth, URI.create("path/" + ENCRYPTED))).thenReturn(URI.create(DECRYPTED));
        when(resourceResolver.resolveRelativeToPrivate(auth, relativeEncrypted))
                .thenReturn(new AbsoluteResourceLocation<>(absolute));

        AbsoluteResourceLocation<PrivateResource> resource =
                resolver.decryptAndResolvePath(auth, relativeEncrypted, root);

        assertThat(resource.location()).asString().isEqualTo("s3://root/bucket/path/" + ENCRYPTED);
        assertThat(resource.getResource().decryptedPath()).asString().isEqualTo(DECRYPTED);
        assertThat(resource.getResource().encryptedPath()).asString().isEqualTo("path/" + ENCRYPTED);
    }
}