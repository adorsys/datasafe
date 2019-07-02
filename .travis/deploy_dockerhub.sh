#!/usr/bin/env bash
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
docker build -t datasafe-rest-service:$IMAGE_TAG --build-arg JAR_FILE=datasafe-rest-impl-$PROJECT_VERSION.jar ./datasafe-rest-impl
docker login -u $DOCKER_USER -p $DOCKER_PASS
IMAGE_NAME=adorsys/datasafe-rest-service
IMAGE_TAG=${TRAVIS_COMMIT:0:7}
docker push $IMAGE_NAME:$IMAGE_TAG
oc tag $IMAGE_NAME:$IMAGE_TAG datasafe-rest-service:latest
