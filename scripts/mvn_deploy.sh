#!/usr/bin/env bash

set -e

echo "$GPG_SECRET_KEY" | base64 --decode | $GPG_EXECUTABLE --import --no-tty --batch --yes || true
echo "$GPG_OWNERTRUST" | base64 --decode | $GPG_EXECUTABLE --import-ownertrust --no-tty --batch --yes || true

mvn deploy -ntp --settings scripts/settings.xml gpg:sign -Prelease -DskipTests -U || exit 1