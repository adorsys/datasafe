#!/usr/bin/env bash
echo ${env.GPG_SECRET_KEY} | base64 --decode | ${env.GPG_EXECUTABLE} --import
echo ${env.GPG_OWNERTRUST} | base64 --decode | ${env.GPG_EXECUTABLE} --import-ownertrust

set -e

mvn --settings .travis/settings.xml package gpg:sign deploy -Prelease -DskipTests -B -U;
