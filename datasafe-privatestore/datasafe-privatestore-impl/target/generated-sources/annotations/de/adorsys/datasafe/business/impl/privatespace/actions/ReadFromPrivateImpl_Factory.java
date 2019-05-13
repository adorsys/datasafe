package de.adorsys.datasafe.business.impl.privatespace.actions;

import dagger.internal.Factory;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentReadService;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class ReadFromPrivateImpl_Factory implements Factory<ReadFromPrivateImpl> {
  private final Provider<EncryptedResourceResolver> resolverProvider;

  private final Provider<EncryptedDocumentReadService> readerProvider;

  public ReadFromPrivateImpl_Factory(
      Provider<EncryptedResourceResolver> resolverProvider,
      Provider<EncryptedDocumentReadService> readerProvider) {
    this.resolverProvider = resolverProvider;
    this.readerProvider = readerProvider;
  }

  @Override
  public ReadFromPrivateImpl get() {
    return provideInstance(resolverProvider, readerProvider);
  }

  public static ReadFromPrivateImpl provideInstance(
      Provider<EncryptedResourceResolver> resolverProvider,
      Provider<EncryptedDocumentReadService> readerProvider) {
    return new ReadFromPrivateImpl(resolverProvider.get(), readerProvider.get());
  }

  public static ReadFromPrivateImpl_Factory create(
      Provider<EncryptedResourceResolver> resolverProvider,
      Provider<EncryptedDocumentReadService> readerProvider) {
    return new ReadFromPrivateImpl_Factory(resolverProvider, readerProvider);
  }

  public static ReadFromPrivateImpl newReadFromPrivateImpl(
      EncryptedResourceResolver resolver, EncryptedDocumentReadService reader) {
    return new ReadFromPrivateImpl(resolver, reader);
  }
}
