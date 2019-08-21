#!/bin/bash

echo -e "\033[0;32m cd datasafe-android \033[0m"
cd datasafe-android

echo -e "\033[0;32m ./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.adorsys.android.datasafeandroidsample.ExampleInstrumentedTest --stacktrace \033[0m"
./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.adorsys.android.datasafeandroidsample.ExampleInstrumentedTest --stacktrace