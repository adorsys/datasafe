#!/usr/bin/env bash

echo "$GPG_SECRET_KEY" | base64 --decode | "$GPG_EXECUTABLE" --import
echo "$GPG_OWNERTRUST" | base64 --decode | "$GPG_EXECUTABLE" --import-ownertrust

set -e

mvn --settings .travis/settings.xml package gpg:sign deploy -Prelease -DskipTests -B -U;
