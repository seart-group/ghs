FROM maven:3.9.3-eclipse-temurin-17-alpine AS build

WORKDIR /
COPY src src/
COPY pom.xml pom.xml

RUN mvn -e --no-transfer-progress clean install -am -DskipTests

FROM eclipse-temurin:17.0.9_9-jre-alpine
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

COPY --from=build /target/ghs-application-*.jar /server.jar

RUN apk update --quiet && \
    apk add --no-cache \
            --quiet \
            git~=2.40.1 \
            cloc~=1.96 \
            curl~=8.5.0

ENTRYPOINT ["java", "-jar", "server.jar"]
