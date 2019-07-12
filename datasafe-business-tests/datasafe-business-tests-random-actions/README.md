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
![Path resolution](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/datasafe-business-tests/datasafe-business-tests-random-actions/high_level_view.puml&fmt=svg&vvv=2&sanitize=true)
