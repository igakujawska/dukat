# Etap budowania: Maven i JDK nie trafiają do gotowego obrazu.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S dukat && adduser -S dukat -G dukat
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER dukat
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
