package de.adorsys.datasafe.business.impl.privatespace.actions;

import dagger.internal.Factory;
import de.adorsys.datasafe.business.api.storage.StorageListService;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class ListPrivateImpl_Factory implements Factory<ListPrivateImpl> {
  private final Provider<EncryptedResourceResolver> resolverProvider;

  private final Provider<StorageListService> listServiceProvider;

  public ListPrivateImpl_Factory(
      Provider<EncryptedResourceResolver> resolverProvider,
      Provider<StorageListService> listServiceProvider) {
    this.resolverProvider = resolverProvider;
    this.listServiceProvider = listServiceProvider;
  }

  @Override
  public ListPrivateImpl get() {
    return provideInstance(resolverProvider, listServiceProvider);
  }

  public static ListPrivateImpl provideInstance(
      Provider<EncryptedResourceResolver> resolverProvider,
      Provider<StorageListService> listServiceProvider) {
    return new ListPrivateImpl(resolverProvider.get(), listServiceProvider.get());
  }

  public static ListPrivateImpl_Factory create(
      Provider<EncryptedResourceResolver> resolverProvider,
      Provider<StorageListService> listServiceProvider) {
    return new ListPrivateImpl_Factory(resolverProvider, listServiceProvider);
  }

  public static ListPrivateImpl newListPrivateImpl(
      EncryptedResourceResolver resolver, StorageListService listService) {
    return new ListPrivateImpl(resolver, listService);
  }
}
