#!/bin/bash

set +e

#echo -e "\033[0;32m ./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.adorsys.android.datasafe.DatasafeLogicTest --stacktrace \033[0m"
#./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.adorsys.android.datasafe.DatasafeLogicTest --stacktrace

echo -e "\033[0;32m Android test are coming soon \033[0m"

echo -e "\033[0;32m cd datasafe-android \033[0m"
cd datasafe-android

echo -e "\033[0;32m ./gradlew  :datasafe:clean --stacktrace \033[0m"
./gradlew :datasafe:clean --stacktrace

echo -e "\033[0;32m ./gradlew :datasafe:assembleDebug --stacktrace \033[0m"
./gradlew :datasafe:assembleDebug --stacktrace