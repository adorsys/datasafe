#!/bin/bash
mv ../src/main/resources/logback-spring.xml ../src/main/resources/weg-logback-spring.xml
mvn clean package -f ../pom.xml -DskipTests
mv ../src/main/resources/weg-logback-spring.xml ../src/main/resources/logback-spring.xml
VERSION=0.0.9-SNAPSHOT
cp ../target/datasafe-rest-impl-${VERSION}.jar datasafe-rest-server.jar
