# Datasafe business tests random actions

This module contains tests that imitate random user actions executed in parallel in close-to-production 
deployment environment.

Tests can be disabled using (for performance reasons): 
`DISABLE_RANDOM_ACTIONS_TEST` system property equal to `true`

Tests for Datasafe:
 - [RandomActionsOnDatasafeTest](src/test/java/de/adorsys/datasafe/business/impl/e2e/randomactions/RandomActionsOnDatasafeTest.java)

Tests for Datasafe-wrapper:
 - [RandomActionsOnSimpleDatasafeAdapterTest](src/test/java/de/adorsys/datasafe/business/impl/e2e/randomactions/RandomActionsOnSimpleDatasafeAdapterTest.java)