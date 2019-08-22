package de.adorsys.datasafe.types.api.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VersionedPrivateResource<V extends Version>
        implements VersionedResourceLocation<PrivateResource, V>, PrivateResource {

    private final PrivateResource resource;

    private final V version;

    @Override
    public Uri encryptedPath() {
        return resource.encryptedPath();
    }

    @Override
    public Uri decryptedPath() {
        return resource.decryptedPath();
    }

    @Override
    public Uri location() {
        return resource.location();
    }

    @Override
    public VersionedPrivateResource<V> resolveFrom(ResourceLocation absolute) {
        return new VersionedPrivateResource<>(resource.resolveFrom(absolute), version);
    }

    @Override
    public VersionedPrivateResource<V> resolve(Uri encryptedPath, Uri decryptedPath) {
        return new VersionedPrivateResource<>(resource.resolve(encryptedPath, decryptedPath), version);
    }

    @Override
    public VersionedPrivateResource<V> withAuthority(String username, String password) {
        return new VersionedPrivateResource<>(resource.withAuthority(username, password), version);
    }
}
