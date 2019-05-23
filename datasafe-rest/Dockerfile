FROM openjdk:8-jre-alpine
ARG JAR_FILE=datasafe-rest-${project.version}.jar
ENV APP_HOME /usr/app
EXPOSE 8080
COPY target/$JAR_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $JAR_FILE"]