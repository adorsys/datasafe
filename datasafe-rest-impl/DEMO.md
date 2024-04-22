# Library demo

This example will demonstrate how Datasafe can read/encrypt/decrypt files. 

For demonstration purposes, library is wrapped into REST interface using Spring,
so that it is acting as data-encryption server that encrypts data and stores it on S3 or filesystem. 
In real usecase, of course, there will be no REST server and client will call same library functions directly.

## Prerequisites

To **build** demo from sources (you can skip it and pull from docker registry):

-  Docker
-  Node.js for UI

To **run** demo:

- Docker

## Building and running demo

### Run it with Docker Compose
The easiest way to run Datasafe Rest Application is using Docker Compose. By default, it works with filesystem with root
directory `datasafe-rest-impl/ROOT_BUCKET`.
Build it with:
``` bash
# build backend
mvn clean package

# build frontend
cd frontend/datasafe-ui
npm i
ng build --deploy-url /static/ --base-href /static/
mv dist ../../datasafe-rest-impl/target/dist

# build image and start datasafe in docker
docker compose up datasafe
```

Frontend is available at http://localhost:8080/static/index.html

### Alternatively you can use shell scripts to build and run datasafe with different types of storages

#### Building

-  Build from sources

```bash
cd datasafe-rest-impl
./1.createDockerimage.sh
```

-  Or pull image from docker registry:

```bash
docker pull adorsys/datasafe && docker tag adorsys/datasafe datasafe-rest-test:latest
```

#### Running

Run using local filesystem, all data will be stored in `target/ROOT_BUCKET` folder:
```bash
cd datasafe-rest-impl
./2.startDockerImageWithLocalFilesystem.sh
```

Run using minio S3-compatible storage, all stored data can be viewed using 
[minio UI](http://localhost:9000/minio/), minio credentials are `accessKey/secretKey`:
```bash
cd datasafe-rest-impl
./2.startDockerImageWithMinio.sh
```

Frontend will be available [here](http://localhost:8080/static/index.html)

Using frontend you can:
-  Create new user
-  Login as user
-  Change REST api endpoint
-  After logging in you will be able to encrypt and store your files.

By viewing requests done in browser or using REST-documentation, you can see how each operation is implemented
with Datasafe. Endpoint code is [here](src/main/java/de/adorsys/datasafe/rest/impl/controller).

Also you can always check [Datasafe-examples folder](../datasafe-examples) to see how something can be done with Datasafe.
