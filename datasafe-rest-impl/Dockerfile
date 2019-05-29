FROM adorsys/openjdk-jre-base:8-minideb
ENV JAVA_OPTS -Xmx512m
WORKDIR /home/datasafe
COPY target/datasafe-rest-server.jar .
EXPOSE 9999
CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /home/datasafe/datasafe-rest-server.jar
