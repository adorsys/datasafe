package de.adorsys.datasafe.directory.impl.profile.operations;

import de.adorsys.datasafe.directory.api.profile.operations.ProfileOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileStorageCredentialsService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileUpdatingService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Aggregate view of profile operations for DFS-based profiles.
 */
@Slf4j
@RuntimeDelegate
public class DFSBasedProfileStorageImpl implements ProfileOperations {

    @Delegate
    private final ProfileRegistrationService registrationService;

    @Delegate
    private final ProfileRetrievalService retrievalService;

    @Delegate
    private final ProfileRemovalService removalService;

    @Delegate
    private final ProfileUpdatingService updatingService;

    @Delegate
    private final ProfileStorageCredentialsService storageCredentialsService;

    @Inject
    public DFSBasedProfileStorageImpl(ProfileRegistrationService registrationService,
                                      ProfileRetrievalService retrievalService, ProfileRemovalService removalService,
                                      ProfileUpdatingService updatingService,
                                      ProfileStorageCredentialsService storageCredentialsService) {
        this.registrationService = registrationService;
        this.retrievalService = retrievalService;
        this.removalService = removalService;
        this.updatingService = updatingService;
        this.storageCredentialsService = storageCredentialsService;
    }
}
