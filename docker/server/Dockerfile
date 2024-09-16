FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

COPY .git /.git/
COPY src /src/
COPY pom.xml /pom.xml

RUN mvn -e --no-transfer-progress clean package -am -DskipTests

FROM eclipse-temurin:17.0.12_7-jre-alpine
LABEL maintainer="Ozren Dabić (dabico@usi.ch)"

COPY --from=build /target/ghs-application-*.jar /server.jar

RUN apk update --quiet && \
    apk add --no-cache \
            --quiet \
            curl~=8.10.0 \
            git~=2.45.2 \
            jq~=1.7.1 \
            perl~=5.38.2

ENTRYPOINT ["java", "-jar", "server.jar"]

HEALTHCHECK \
    --start-period=30s \
    --interval=60s \
    --timeout=5s \
    --retries=5 \
    CMD curl --silent --fail --request GET http://localhost:8080/api/actuator/health | jq --exit-status -n 'inputs | if has("status") then .status=="UP" else false end' > /dev/null || exit 1
