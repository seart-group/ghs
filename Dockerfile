FROM adoptopenjdk/openjdk12:alpine-jre

ARG JAR_FILE=target/gse-application-0.9.0.jar

WORKDIR /usr/src/app

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","app.jar"]
