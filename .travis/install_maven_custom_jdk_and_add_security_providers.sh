#!/bin/bash

# This script:
# 1. Installs custom JDK.
# 2. Downloads BouncyCastle security provider and TLS libs. (to use native java SSL)

REPO_ROOT=`pwd`

# 1. Custom JDK and standard maven
curl "${GRAVIS}.install-jdk-travis.sh" --output ~/.install-jdk-travis.sh
source ~/.install-jdk-travis.sh

echo "Installing maven"
apt-get install maven -y

cd "$REPO_ROOT"

# 2. BC libs - download
# 2.1 Parse BouncyCastle version
BC_VERSION=`grep "<bouncycastle\.version>.*</bouncycastle\.version>" pom.xml | cut -d">" -f2 | cut -d"<" -f1`

# 2.2 Download BC jars needed
curl "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/${BC_VERSION}/bcprov-jdk15on-${BC_VERSION}.jar" \
    --output "$JAVA_HOME/jre/lib/ext/bcprov-jdk15on-${BC_VERSION}.jar"

curl "https://repo1.maven.org/maven2/org/bouncycastle/bctls-jdk15on/${BC_VERSION}/bctls-jdk15on-${BC_VERSION}.jar" \
    --output "$JAVA_HOME/jre/lib/ext/bctls-jdk15on-${BC_VERSION}.jar"

# Windows does not have Graal Updater (gu) tool, so we install native-image manually
if [[ "$TRAVIS_OS_NAME" == "windows"* ]]; then
  # TODO
  echo "Windows, nothing to do"
else
  # Install native image builder
  gu install native-image
fi


