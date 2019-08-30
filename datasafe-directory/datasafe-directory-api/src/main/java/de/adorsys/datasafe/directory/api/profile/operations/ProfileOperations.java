package de.adorsys.datasafe.directory.api.profile.operations;

/**
 * Aggregate interface for all profile operations.
 */
public interface ProfileOperations extends ProfileRegistrationService, ProfileUpdatingService, ProfileRetrievalService,
        ProfileRemovalService, ProfileStorageCredentialsService {
}
