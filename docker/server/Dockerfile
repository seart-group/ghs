FROM maven:3.9.3-eclipse-temurin-17-alpine AS build

COPY ./pom.xml /pom.xml
COPY ./src ./src

RUN mvn -e --no-transfer-progress clean install -am -DskipTests

FROM eclipse-temurin:17.0.9_9-jre-alpine
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

COPY --from=build /target/ghs-application-*.jar /server.jar

RUN apk update --quiet && \
    apk add --no-cache \
            --quiet \
            git \
            cloc \
            curl

ENTRYPOINT java -jar server.jar
