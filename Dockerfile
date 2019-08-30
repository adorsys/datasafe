FROM ubuntu:xenial

RUN apt-get update && apt-get install curl -y

ENV GRAVIS="https://raw.githubusercontent.com/DanySK/Gravis-CI/master/"
ENV JDK="graalvm@19.2.0"

###
ENV TRAVIS_OS_NAME=linux

# A
RUN curl "${GRAVIS}.install-jdk-travis.sh" --output ~/.install-jdk-travis.sh
RUN chmod +x ~/.install-jdk-travis.sh
RUN ~/.install-jdk-travis.sh

RUN apt-get install maven -y && apt-get install git -y

RUN git clone https://github.com/adorsys/datasafe && cd datasafe && git checkout -t origin/feature/datasafe-cli-w-s3
RUN cd datasafe
