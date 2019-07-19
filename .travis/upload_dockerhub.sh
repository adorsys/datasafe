#!/usr/bin/env bash
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
IMAGE_NAME=adorsys/datasafe
IMAGE_TAG=${TRAVIS_COMMIT:0:7}
docker login -u $DOCKERHUB_USER -p $DOCKERHUB_PASS

docker build -t $IMAGE_NAME:$IMAGE_TAG --build-arg JAR_FILE=datasafe-rest-impl-$PROJECT_VERSION.jar ./datasafe-rest-impl
docker push $IMAGE_NAME:$IMAGE_TAG || exit 1

docker tag $IMAGE_NAME:$IMAGE_TAG $IMAGE_NAME:latest
docker push $IMAGE_NAME:latest || exit 1