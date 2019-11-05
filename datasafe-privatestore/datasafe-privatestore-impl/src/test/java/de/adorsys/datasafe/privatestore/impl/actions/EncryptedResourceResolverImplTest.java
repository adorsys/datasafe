package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptedResourceResolverImplTest extends BaseMockitoTest {

    private static final String ENCRYPTED = "ENCRYPTED";
    private static final String DECRYPTED = "DECRYPTED";

    private PrivateResource root = BasePrivateResource.forPrivate(URI.create("s3://root/"));
    private PrivateResource absoluteEncrypted = BasePrivateResource.forPrivate(root.location().resolve(ENCRYPTED));
    private PrivateResource absolute = BasePrivateResource.forPrivate(URI.create("s3://root/bucket/"));
    private PrivateResource relative = BasePrivateResource.forPrivate(URI.create("./path"));
    private PrivateResource relativeEncrypted = BasePrivateResource.forPrivate(URI.create("./path/")
            .resolve(ENCRYPTED));
    private UserIDAuth auth = new UserIDAuth(new UserID(""), ReadKeyPasswordTestFactory.getForString(""));

    @Mock
    private BucketAccessService accessService;

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
        when(accessService.privateAccessFor(auth, absolute)).thenReturn(new AbsoluteLocation<>(absolute));

        AbsoluteLocation<PrivateResource> resource = resolver.encryptAndResolvePath(
            auth, absolute, StorageIdentifier.DEFAULT
        );

        assertThat(resource.location()).isEqualTo(absolute.location());
        verify(pathEncryption, never()).encrypt(any(), any());
    }

    @Test
    void encryptAndResolvePathRelative() {
        when(pathEncryption.encrypt(auth, relative.location())).thenReturn(new Uri(ENCRYPTED));
        when(resourceResolver.resolveRelativeToPrivate(eq(auth), any(), eq(StorageIdentifier.DEFAULT)))
                .thenAnswer(inv -> BasePrivateResource.forAbsolutePrivate(root.location().resolve(ENCRYPTED)));

        AbsoluteLocation<PrivateResource> resource = resolver
            .encryptAndResolvePath(auth, relative, StorageIdentifier.DEFAULT);

        verify(resourceResolver).resolveRelativeToPrivate(eq(auth), captor.capture(), eq(StorageIdentifier.DEFAULT));
        assertThat(resource.location()).extracting(Uri::toASCIIString).isEqualTo("s3://root/" + ENCRYPTED);
        assertThat(captor.getValue().encryptedPath()).extracting(Uri::toASCIIString).isEqualTo(ENCRYPTED);
        assertThat(captor.getValue().decryptedPath()).extracting(Uri::toASCIIString).isEqualTo("path");
    }

    @Test
    void decryptAndResolvePathAbsolute() {
        when(pathEncryption.decryptor(auth)).thenReturn(
                path -> path.asString().equals(ENCRYPTED) ? new Uri(DECRYPTED) : null
        );
        when(resourceResolver.resolveRelativeToPrivate(auth, absoluteEncrypted, StorageIdentifier.DEFAULT))
                .thenReturn(new AbsoluteLocation<>(absolute));

        AbsoluteLocation<PrivateResource> resource =
                resolver.decryptingResolver(auth, root, StorageIdentifier.DEFAULT).apply(absoluteEncrypted);

        assertThat(resource.location()).extracting(Uri::toASCIIString).isEqualTo("s3://root/bucket/" + ENCRYPTED);
        assertThat(resource.getResource().decryptedPath()).extracting(Uri::toASCIIString).isEqualTo(DECRYPTED);
        assertThat(resource.getResource().encryptedPath()).extracting(Uri::toASCIIString).isEqualTo(ENCRYPTED);
    }

    @Test
    void decryptAndResolvePathRelative() {
        when(pathEncryption.decryptor(auth)).thenReturn(
                path -> path.asString().equals("path/" + ENCRYPTED) ? new Uri(DECRYPTED) : null
        );
        when(resourceResolver.resolveRelativeToPrivate(auth, relativeEncrypted, StorageIdentifier.DEFAULT))
                .thenReturn(new AbsoluteLocation<>(absolute));

        AbsoluteLocation<PrivateResource> resource =
                resolver.decryptingResolver(auth, root, StorageIdentifier.DEFAULT).apply(relativeEncrypted);

        assertThat(resource.location()).extracting(Uri::toASCIIString).isEqualTo("s3://root/bucket/path/" + ENCRYPTED);
        assertThat(resource.getResource().decryptedPath()).extracting(Uri::toASCIIString).isEqualTo(DECRYPTED);
        assertThat(resource.getResource().encryptedPath()).extracting(Uri::toASCIIString).isEqualTo("path/" + ENCRYPTED);
    }
}
