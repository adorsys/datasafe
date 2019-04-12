package de.adorsys.datasafe.business.impl.profile;

import dagger.Component;
import de.adorsys.datasafe.business.impl.profile.filesystem.HashMapProfileStorageImpl;

@Component(modules = DefaultProfileModule.class)
public interface DefaultProfileService {

    HashMapProfileStorageImpl userProfileRegistration();
}
