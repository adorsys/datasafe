#!/bin/bash

###### TODO: Make it as docker-compose?

AWS_KEY=accessKey
AWS_SECRET=secretKey
BUCKET=demo-bucket
API_USER=root
API_PASSWORD=root

trap stop SIGINT

function stop() {
	echo Terminating
	docker stop -t 0 datasafe-rest-test
	docker stop -t 0 datasafe-minio
	docker network rm datasafe-test
}

echo =====================================================
echo "PLEASE VISIT http://localhost:8080/static/index.html"
echo "MINIO secret key / access key - ${AWS_KEY}/${AWS_SECRET}"
echo
which pbcopy
if (( $? == 0 ))
then
    echo "http://localhost:8080/static/index.html" | pbcopy
    echo "this url is already in your clipboard :-)"
fi
echo "pres CTRL-C to stop container"
echo
echo =====================================================

docker network create --attachable -d overlay datasafe-test

docker run 										                                        \
	--rm 											                                    \
	-d                                                                                  \
	-it                                                                                 \
	--name datasafe-minio      								                            \
	--network=datasafe-test                                                             \
	-p 9000:9000 										                                \
	-e MINIO_ACCESS_KEY="${AWS_KEY}" 							                        \
	-e MINIO_SECRET_KEY="${AWS_SECRET}" 								                \
	--entrypoint "/bin/sh"                                                              \
	minio/minio                                                                         \
	-c "mkdir -p /data/${BUCKET} && /usr/bin/minio server /data"

MINIO_IP=`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' datasafe-minio`

docker run 										                                        \
	--rm 											                                    \
	-it                                                                                 \
	-d                                                                                  \
	--network=datasafe-test                                                             \
	--name datasafe-rest-test								                            \
	-p 8080:8080 										                                \
	-e JWT_SECRET=jnknjknvkjdfnjkvnkdfnvjkndfivfnjkvnskcnncjksnjkvndjfknjkvndfknvjk 	\
	-e DEFAULT_USER="${API_USER}"						                                \
	-e DEFAULT_PASSWORD="${API_PASSWORD}" 	      						                \
	-e DATASAFE_AMAZON_URL=http://${MINIO_IP}:9000                                      \
	-e AWS_ACCESS_KEY_ID="${AWS_KEY}" 					                                \
	-e AWS_SECRET_ACCESS_KEY="${AWS_SECRET}"                      				        \
	-e AWS_BUCKET="${BUCKET}"						                                    \
	-e DATASAFE_S3_STORAGE=true                                                         \
	-e EXPOSE_API_CREDS=true 								                            \
	datasafe-rest-test:latest

tail -f /dev/null
