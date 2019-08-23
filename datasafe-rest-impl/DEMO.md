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

### Building

-  Build from sources

```bash
cd datasafe-rest-impl
./1.createDockerimage.sh
```

-  Or pull image from docker registry:

```bash
docker pull adorsys/datasafe && docker tag adorsys/datasafe adorsys/datasafe-rest-test
```

### Running

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
There you can:
-  Create new user
-  Login as user
-  Change REST api endpoint
-  After logging in you will be able to encrypt and store your files.
