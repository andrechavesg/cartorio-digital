# syntax=docker/dockerfile:1

ARG MAVEN_IMAGE=maven:3.9.6-eclipse-temurin-21
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre

FROM ${MAVEN_IMAGE} AS build
WORKDIR /workspace

COPY ../../pom.xml pom.xml
COPY ../../services services

RUN mvn -pl services/identity -am package -DskipTests

FROM ${RUNTIME_IMAGE}
WORKDIR /opt/app

COPY --from=build /workspace/services/identity/target/quarkus-app ./quarkus-app

EXPOSE 8081
ENTRYPOINT ["java","-jar","/opt/app/quarkus-app/quarkus-run.jar"]
