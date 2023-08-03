FROM maven:3.9.2-eclipse-temurin-17-alpine AS build

COPY ./pom.xml /pom.xml
COPY ./src ./src

RUN mvn -e --no-transfer-progress clean install -am -DskipTests

FROM eclipse-temurin:17.0.7_7-jre-alpine

COPY --from=build /target/ghs-application-*.jar /server.jar

RUN apk update --quiet
RUN apk add --no-cache \
            --quiet \
            bash \
            git \
            cloc \
            curl

EXPOSE 8080

ENTRYPOINT java -jar server.jar
