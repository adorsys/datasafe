#!/usr/bin/env bash
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
oc login https://openshift.adorsys.de:443 --username=$OPENSHIFT_USER --password=$OPENSHIFT_PASSWORD
IMAGE_TAG=${TRAVIS_COMMIT:0:7}
REGISTRY_DOMAIN=openshift-registry.adorsys.de
SERVICE_NAME=datasafe-rest-service
IMAGE_NAME=$REGISTRY_DOMAIN/datasafe/$SERVICE_NAME
docker login -u $(oc whoami) -p $(oc whoami -t) https://$REGISTRY_DOMAIN
docker build -t $IMAGE_NAME:$IMAGE_TAG --build-arg JAR_FILE=datasafe-rest-impl-$PROJECT_VERSION.jar ./datasafe-rest-impl
docker push $IMAGE_NAME:$IMAGE_TAG && \
oc tag datasafe/$SERVICE_NAME:$IMAGE_TAG datasafe/$SERVICE_NAME:latest