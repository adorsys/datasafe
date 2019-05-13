package de.adorsys.datasafe.business.impl.privatespace.actions;

import dagger.internal.Factory;
import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class EncryptedResourceResolver_Factory implements Factory<EncryptedResourceResolver> {
  private final Provider<ResourceResolver> resolverProvider;

  private final Provider<PathEncryption> pathEncryptionProvider;

  public EncryptedResourceResolver_Factory(
      Provider<ResourceResolver> resolverProvider,
      Provider<PathEncryption> pathEncryptionProvider) {
    this.resolverProvider = resolverProvider;
    this.pathEncryptionProvider = pathEncryptionProvider;
  }

  @Override
  public EncryptedResourceResolver get() {
    return provideInstance(resolverProvider, pathEncryptionProvider);
  }

  public static EncryptedResourceResolver provideInstance(
      Provider<ResourceResolver> resolverProvider,
      Provider<PathEncryption> pathEncryptionProvider) {
    return new EncryptedResourceResolver(resolverProvider.get(), pathEncryptionProvider.get());
  }

  public static EncryptedResourceResolver_Factory create(
      Provider<ResourceResolver> resolverProvider,
      Provider<PathEncryption> pathEncryptionProvider) {
    return new EncryptedResourceResolver_Factory(resolverProvider, pathEncryptionProvider);
  }

  public static EncryptedResourceResolver newEncryptedResourceResolver(
      ResourceResolver resolver, PathEncryption pathEncryption) {
    return new EncryptedResourceResolver(resolver, pathEncryption);
  }
}
