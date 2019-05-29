#!/bin/bash
set -x
set -e

PROJECT_NAME=datasafe-rest-server
IMAGE_NAME=${PROJECT_NAME}:latest
LOCAL_IMAGE=local/${IMAGE_NAME}

docker build -t ${LOCAL_IMAGE} .

OPENSHIFT_REGISTRY=openshift-registry.adorsys.de:443
OPENSHIFT_PROJECT=psp-docusafe-performancetest
OPENSHIFT_IMAGE_NAME=${PROJECT_NAME}:latest
OPENSHIFT_IMAGE=${OPENSHIFT_REGISTRY}/${OPENSHIFT_PROJECT}/${OPENSHIFT_IMAGE_NAME}
LDAP_USER=psp

docker tag $LOCAL_IMAGE $OPENSHIFT_IMAGE

oc login -u $LDAP_USER
TOKEN=`oc whoami -t`

docker login -u $LDAP_USER -p ${TOKEN} ${OPENSHIFT_REGISTRY}
docker push $OPENSHIFT_IMAGE
