package de.adorsys.datasafe.examples.business.s3;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileOperations;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImpl;
import lombok.experimental.Delegate;

/**
 * For storage-only Datasafe instances keystore is always placed within storage itself, somewhere beneath
 * private files root.
 */
class DatasafeBasedProfileManager extends ProfileRetrievalServiceImpl {

    @Delegate
    private final ProfileOperations profileOperations;

    DatasafeBasedProfileManager(DefaultDatasafeServices directoryDatasafe) {
        super(null, null, null, null, null, null);
        this.profileOperations = directoryDatasafe.userProfile();
    }
}
