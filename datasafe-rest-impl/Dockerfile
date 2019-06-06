FROM openjdk:8-jre-alpine
ARG JAR_FILE
EXPOSE 8080
ENV APP_HOME /usr/app
COPY target/${JAR_FILE} $APP_HOME/app.jar
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar app.jar"]