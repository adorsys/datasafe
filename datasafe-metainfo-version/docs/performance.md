#Versioned storage performance
Test imitated 10 users performing different random actions on storage (report is for privatespace)

For details see :
 - de.adorsys.datasafe.business.impl.e2e.AmazonPerformance
 - de.adorsys.datasafe.business.impl.e2e.CephPerformance
 - de.adorsys.datasafe.business.impl.e2e.MinioPerformance

Performance statistic per operation:

## Delete operation on 1000Kb files
![Delete operation](./perf/delete_1000Kb.svg)

## List operation on 1000Kb files
![List operation](./perf/list_1000Kb.svg)

## Read operation on 1000Kb files
![Read operation](./perf/read_1000Kb.svg)

## Write operation on 1000Kb files
![Write operation](./perf/write_1000Kb.svg)