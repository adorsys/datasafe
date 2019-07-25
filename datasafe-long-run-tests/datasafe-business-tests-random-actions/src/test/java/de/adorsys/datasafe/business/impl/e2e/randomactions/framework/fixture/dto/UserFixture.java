package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto;

import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import lombok.Data;

/**
 * Get the user level Fixture and Storage system.
 */
@Data
public class UserFixture {
    /**
     * User operation list, private space and public space
     */
    private final Fixture fixturebyUser;

    /**
     * Respective storage system
     */
    private final WithStorageProvider.StorageDescriptor descriptor;

    private final DefaultDatasafeServices datasafeServices;

    private final StatisticService statisticService;
}
