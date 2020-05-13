FROM markhobson/maven-chrome:jdk-13

WORKDIR /usr/local/

COPY . .

#ENTRYPOINT ["java","-jar","target/gse-application-0.9.7.jar"]
ENTRYPOINT ["/bin/bash", "-c", "sleep 30 && java -jar target/gse-application-0.9.7.jar"]
