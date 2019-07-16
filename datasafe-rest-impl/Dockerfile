FROM openjdk:8-jre-alpine

ARG JAR_FILE
ENV JAR_FILE ${JAR_FILE}
ENV APP_HOME /usr/app

WORKDIR $APP_HOME

COPY target/${JAR_FILE} ${JAR_FILE}
COPY target/dist/datasafe-ui frontend
COPY run.sh run.sh

RUN chmod +x run.sh

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "$APP_HOME/run.sh"]
