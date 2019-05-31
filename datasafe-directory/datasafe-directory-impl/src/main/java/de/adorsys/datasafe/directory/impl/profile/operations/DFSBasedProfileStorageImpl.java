package de.adorsys.datasafe.directory.impl.profile.operations;

import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Aggregate view of profile operations for DFS-based profiles.
 */
@Slf4j
public class DFSBasedProfileStorageImpl implements
        ProfileRegistrationService,
        ProfileRetrievalService,
        ProfileRemovalService {

    @Delegate
    private final ProfileRegistrationService registrationService;

    @Delegate
    private final ProfileRetrievalService retrievalService;

    @Delegate
    private final ProfileRemovalService removalService;

    @Inject
    public DFSBasedProfileStorageImpl(ProfileRegistrationService registrationService,
                                      ProfileRetrievalService retrievalService,
                                      ProfileRemovalService removalService) {
        this.registrationService = registrationService;
        this.retrievalService = retrievalService;
        this.removalService = removalService;
    }
}
