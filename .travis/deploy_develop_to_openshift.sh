#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" == “develop" ]; then
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
oc login https://openshift.adorsys.de:443 --username=$OPENSHIFT_USER --password=$OPENSHIFT_PASSWORD
docker login -u $(oc whoami) -p $(oc whoami -t) https://openshift-registry.adorsys.de
docker build -t datasafe-rest-service:$TRAVIS_COMMIT --build-arg JAR_FILE=datasafe-rest-impl-$PROJECT_VERSION.jar ./datasafe-rest-impl
export IMAGE_NAME=openshift-registry.adorsys.de/datasafe/datasafe-rest-service
docker tag datasafe-rest-service:$TRAVIS_COMMIT $IMAGE_NAME:$TRAVIS_COMMIT
docker push $IMAGE_NAME:$TRAVIS_COMMIT
oc tag $IMAGE_NAME:$TRAVIS_COMMIT datasafe-rest-service:latest
fi