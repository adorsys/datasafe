#!/usr/bin/env bash

echo -e "\033[0;32m cd datasafe-android \033[0m"
cd datasafe-android

echo -e "\033[0;32m ./gradlew  :datasafe-android:clean --stacktrace \033[0m"
./gradlew :datasafe-android:clean --stacktrace

echo -e "\033[0;32m ./gradlew :datasafe-android:assembleDebug --stacktrace \033[0m"
./gradlew :datasafe-android:assembleDebug --stacktrace

#echo -e "\033[0;32m :datasafe-android:install --stacktrace \033[0m"
#./gradlew :datasafe-android:install --stacktrace

if [ "$CI" == true ] && [ "$TRAVIS_PULL_REQUEST" == false ] && [ "$TRAVIS_BRANCH" == "master" ]; then

    echo -e "\033[0;32m Bintray upload coming soon \033[0m"
    # echo -e "\033[0;32m ./gradlew :datasafe-android:bintrayUpload \033[0m"
    # ./gradlew :datasafe-android:bintrayUpload --stacktrace

else
    echo -e "\033[0;32m Bintray upload coming soon \033[0m"
#   echo -e "\033[0;32m Current branch is not master, will not upload to bintray. \033[0m"
fi