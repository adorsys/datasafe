#!/bin/bash
mvn clean package -DskipTests
VERSION=0.0.9-SNAPSHOT
cp target/datasafe-rest-impl-${VERSION}.jar target/datasafe-rest-server.jar
