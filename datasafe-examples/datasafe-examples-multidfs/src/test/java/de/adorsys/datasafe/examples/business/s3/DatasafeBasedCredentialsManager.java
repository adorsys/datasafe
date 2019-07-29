package de.adorsys.datasafe.examples.business.s3;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.util.io.Streams;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Reads users' credentials based on bucket name (bucket is encoded as hostname into location URI).
 */
@RequiredArgsConstructor
class DatasafeBasedCredentialsManager extends BucketAccessServiceImpl {

    private final DefaultDatasafeServices directoryDatasafe;

    @SneakyThrows
    void registerDfs(UserIDAuth forUser, String bucketName, StorageCredentials credentials) {
        try (OutputStream os = directoryDatasafe
                .privateService()
                .write(WriteRequest.forDefaultPrivate(forUser, bucketName))) {
            os.write(credentials.serialize().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public AbsoluteLocation<PrivateResource> privateAccessFor(UserIDAuth user, PrivateResource resource) {
        StorageCredentials credentials = readCredentials(user, bucketName(resource));
        return new AbsoluteLocation<>(resource.withAuthority(credentials.getUsername(), credentials.getPassword()));
    }

    @Override
    public AbsoluteLocation<PublicResource> publicAccessFor(UserID user, PublicResource resource) {
        // access control to public resources is absent in this example, generally
        // we expect anyone can write to the resource if he knows proper uri.
        // still this method can be overridden for more fine-grained public access control.
        return new AbsoluteLocation<>(resource);
    }

    @Override
    public AbsoluteLocation withSystemAccess(AbsoluteLocation resource) {
        // since user profiles are stored in separate datasafe instance there will be no `system access`
        throw new IllegalArgumentException("Not implemented");
    }

    @SneakyThrows
    private StorageCredentials readCredentials(UserIDAuth forUser, String bucketName) {
        try (InputStream is = directoryDatasafe
                .privateService()
                .read(ReadRequest.forDefaultPrivate(forUser, bucketName))) {
            return new StorageCredentials(new String(Streams.readAll(is)));
        }
    }

    private String bucketName(ResourceLocation resource) {
        return resource.location().asURI().getHost();
    }
}
