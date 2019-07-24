package de.adorsys.datasafe.business.impl.e2e.randomactions;

import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services.StatisticService;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.teststorage.WithStorageProvider;

import java.util.List;

public class ExecuteTest extends RandomActionsOnDatasafeMultiS3Test implements Runnable {
    DefaultDatasafeServices datasafeServices;
    StatisticService statisticService;
    StorageDescriptor descriptor;
    int threadCount;
    int filesizeInMb;

    public ExecuteTest(DefaultDatasafeServices datasafeServices, StatisticService statisticService, StorageDescriptor descriptor, int threadCount, int filesizeInMb){
        this.datasafeServices = datasafeServices;
        this.statisticService = statisticService;
        this.descriptor = descriptor;
        this.threadCount = threadCount;
        this.filesizeInMb = filesizeInMb;
    }

    @Override
    public void run() {
        execute(datasafeServices, statisticService, descriptor, threadCount, filesizeInMb);
    }
}
