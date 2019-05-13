package de.adorsys.datasafe.business.impl.privatespace.actions;

import dagger.internal.Factory;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class WriteToPrivateImpl_Factory implements Factory<WriteToPrivateImpl> {
  private final Provider<PrivateKeyService> privateKeyServiceProvider;

  private final Provider<EncryptedResourceResolver> resolverProvider;

  private final Provider<EncryptedDocumentWriteService> writerProvider;

  public WriteToPrivateImpl_Factory(
      Provider<PrivateKeyService> privateKeyServiceProvider,
      Provider<EncryptedResourceResolver> resolverProvider,
      Provider<EncryptedDocumentWriteService> writerProvider) {
    this.privateKeyServiceProvider = privateKeyServiceProvider;
    this.resolverProvider = resolverProvider;
    this.writerProvider = writerProvider;
  }

  @Override
  public WriteToPrivateImpl get() {
    return provideInstance(privateKeyServiceProvider, resolverProvider, writerProvider);
  }

  public static WriteToPrivateImpl provideInstance(
      Provider<PrivateKeyService> privateKeyServiceProvider,
      Provider<EncryptedResourceResolver> resolverProvider,
      Provider<EncryptedDocumentWriteService> writerProvider) {
    return new WriteToPrivateImpl(
        privateKeyServiceProvider.get(), resolverProvider.get(), writerProvider.get());
  }

  public static WriteToPrivateImpl_Factory create(
      Provider<PrivateKeyService> privateKeyServiceProvider,
      Provider<EncryptedResourceResolver> resolverProvider,
      Provider<EncryptedDocumentWriteService> writerProvider) {
    return new WriteToPrivateImpl_Factory(
        privateKeyServiceProvider, resolverProvider, writerProvider);
  }

  public static WriteToPrivateImpl newWriteToPrivateImpl(
      PrivateKeyService privateKeyService,
      EncryptedResourceResolver resolver,
      EncryptedDocumentWriteService writer) {
    return new WriteToPrivateImpl(privateKeyService, resolver, writer);
  }
}
