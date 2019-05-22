FROM openjdk:8-jre-alpine
ENV JAR_FILE datasafe-rest-0.0.6-SNAPSHOT.jar
ENV APP_HOME /usr/app
EXPOSE 8080
COPY target/$JAR_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $JAR_FILE"]