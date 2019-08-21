#!/usr/bin/env bash

echo -e "\033[0;32m cd datasafe-android \033[0m"
cd datasafe-android

echo -e "\033[0;32m ./gradlew  :datasafe:clean --stacktrace \033[0m"
./gradlew :datasafe:clean --stacktrace

echo -e "\033[0;32m ./gradlew :datasafe:assembleDebug --stacktrace \033[0m"
./gradlew :datasafe:assembleDebug --stacktrace

#echo -e "\033[0;32m :datasafe:install --stacktrace \033[0m"
#./gradlew :datasafe:install --stacktrace

if [ "$CI" == true ] && [ "$TRAVIS_PULL_REQUEST" == false ] && [ "$TRAVIS_BRANCH" == "master" ]; then

    echo -e "\033[0;32m Bintray upload coming soon \033[0m"
    # echo -e "\033[0;32m ./gradlew :datasafe:bintrayUpload \033[0m"
    # ./gradlew :datasafe:bintrayUpload --stacktrace

else
    echo -e "\033[0;32m Bintray upload coming soon \033[0m"
#   echo -e "\033[0;32m Current branch is not master, will not upload to bintray. \033[0m"
fi