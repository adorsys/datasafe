# Datasafe business tests random actions (part of durability test suite)

This module contains tests that imitate random user actions executed in parallel in close-to-production 
deployment environment. It is rather **optional** test suite that supplements core tests in 
[datasafe-business](../../datasafe-business/src/test/java/de/adorsys/datasafe/business/impl/e2e)

Tests can be disabled using (for performance reasons): 
`DISABLE_RANDOM_ACTIONS_TEST` system property equal to `true`

Tests for Datasafe:
 - [RandomActionsOnDatasafeTest](src/test/java/de/adorsys/datasafe/business/impl/e2e/randomactions/RandomActionsOnDatasafeTest.java)

Tests for Datasafe-wrapper:
 - [RandomActionsOnSimpleDatasafeAdapterTest](src/test/java/de/adorsys/datasafe/business/impl/e2e/randomactions/RandomActionsOnSimpleDatasafeAdapterTest.java)

### Test flow overview:
![Path resolution](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/datasafe-long-run-tests/datasafe-business-tests-random-actions/high_level_view.puml&fmt=svg&vvv=2&sanitize=true)

This tests are based on json fixture files. Fixture contains list of actions with their result expectation. 
For example it can contain sequence like this: 
1. `For user1 WRITE some/path/file.txt` with content id `ID-1`(*)
1. `For user1 READ some/path/file.txt` and expect checksum of its content to match checksum of content id `ID-1`
1. `For user1 LIST some/path/` and expect to receive `[file.txt]` as the result

 `*` - content id is nothing more than blob identifier, so it is not needed to store blob in fixture itself
 
Each sequence of actions with their expectation can be executed in parallel. To achieve that we simply prefix 
path of an action with some identifier (execution identifier) and run those actions on shared user pool 
in parallel. This yields:

`Execution 1`:
1. `For user1 WRITE exec1/some/path/file.txt` with content id `ID-1`(*)
1. `For user1 READ exec1/some/path/file.txt` and expect checksum of its content to match checksum of content id `ID-1`
1. `For user1 LIST exec1/some/path/` and expect to receive `[file.txt]` as the result

`Execution 2`:
1. `For user1 WRITE exec2/some/path/file.txt` with content id `ID-1`(*)
1. `For user1 READ exec2/some/path/file.txt` and expect checksum of its content to match checksum of content id `ID-1`
1. `For user1 LIST exec2/some/path/` and expect to receive `[file.txt]` as the result

It is clear that aforementioned 2 sets of actions (`Execution 1` and `Execution 2`) can be executed in parallel 
by two threads because they are independent - `Execution 1` is independent of `Execution 2`. Still, while 
being independent - they share same user, so when executing these executions in 2 threads we actually
are doing random actions on some user (`user1` in this example). 
This way, we achieve parallel execution of random actions and can calculate performance statistics of each action.

These tests run on test plan matrix of (Content-size x Thread-count). 
As the result they produce following output in logs:
```text
12:51:36.383 [main] INFO de.adorsys.datasafe.business.impl.e2e.randomactions.RandomActionsOnDatasafeTest - ==== Statistics for FILESYSTEM with 11 threads and 3 Mb filesize: ====
12:51:36.383 [main] INFO de.adorsys.datasafe.business.impl.e2e.randomactions.RandomActionsOnDatasafeTest - WRITE : StatisticService.Percentiles(stat={50=398.0, 99=629.78, 90=493.8, 75=436.0, 95=515.9}, throughputPerThread=2.4650780608052587)
12:51:36.383 [main] INFO de.adorsys.datasafe.business.impl.e2e.randomactions.RandomActionsOnDatasafeTest - LIST : StatisticService.Percentiles(stat={50=2.0, 99=12.0, 90=7.0, 75=4.0, 95=8.0}, throughputPerThread=389.06752411575565)
12:51:36.383 [main] INFO de.adorsys.datasafe.business.impl.e2e.randomactions.RandomActionsOnDatasafeTest - SHARE : StatisticService.Percentiles(stat={50=1987.5, 99=4022.18, 90=3323.7, 75=2448.0, 95=3456.65}, throughputPerThread=0.5068871369708885)
12:51:36.383 [main] INFO de.adorsys.datasafe.business.impl.e2e.randomactions.RandomActionsOnDatasafeTest - CREATE_USER : StatisticService.Percentiles(stat={50=461.0, 99=830.83, 90=658.3, 75=544.0, 95=754.15}, throughputPerThread=2.048340843916428)
12:51:36.383 [main] INFO de.adorsys.datasafe.business.impl.e2e.randomactions.RandomActionsOnDatasafeTest - READ : StatisticService.Percentiles(stat={50=511.0, 99=690.92, 90=602.6, 75=551.0, 95=646.0}, throughputPerThread=1.9656450727288677)
12:51:36.383 [main] INFO de.adorsys.datasafe.business.impl.e2e.randomactions.RandomActionsOnDatasafeTest - DELETE : StatisticService.Percentiles(stat={50=3.0, 99=8.05, 90=4.0, 75=4.0, 95=5.0}, throughputPerThread=397.5903614457831)
```
Where, 
```text
==== Statistics for FILESYSTEM with 11 threads and 3 Mb filesize: ====
```
Is the heading, saying that test ran on filesystem storage, with 11 threads and 3 mebibyte (MiB) content size
```text
WRITE : StatisticService.Percentiles(stat={50=398.0, 99=629.78, 90=493.8, 75=436.0, 95=515.9}, throughputPerThread=2.4650780608052587)
```
Is the result row for write operation to users' privatespace, with operation performance percentiles:
 - so `95=515.9` means that 95% of WRITE operations were finished less than 515. milliseconds; 
 - `throughputPerThread=2.4650780608052587` means that on average each of these 11 threads did approximately 2.465 write 
 operations per second with file size equal to 3 mebibyte 
 - and totally system was able to serve `11 * 2.465 = 27.115` write operations per second, yielding
`27.115 * 3 = 81.35` MiB/s total byte write throughput.

**Note:** SHARE operations have different recipients' count (specified by fixture), on average it is greater than 1,
hence its performance is lower than write. Recipients' count is fully controlled by fixture.
