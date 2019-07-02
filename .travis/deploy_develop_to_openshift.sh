#!/usr/bin/env bash
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
oc login https://openshift.adorsys.de:443 --username=$OPENSHIFT_USER --password=$OPENSHIFT_PASSWORD

# this should be fixed by DOC-209
IMAGE_TAG="latest"

docker login -u $(oc whoami) -p $(oc whoami -t) https://openshift-registry.adorsys.de
docker build -t datasafe-rest-service:$IMAGE_TAG --build-arg JAR_FILE=datasafe-rest-impl-$PROJECT_VERSION.jar ./datasafe-rest-impl
export IMAGE_NAME=openshift-registry.adorsys.de/datasafe/datasafe-rest-service
docker tag datasafe-rest-service:$IMAGE_TAG $IMAGE_NAME:$IMAGE_TAG
docker push $IMAGE_NAME:$IMAGE_TAG

# Should be done by DOC-209
# oc tag $IMAGE_NAME:$IMAGE_TAG datasafe-rest-service:latest