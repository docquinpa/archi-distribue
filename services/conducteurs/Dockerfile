# syntax=docker/dockerfile:1.7
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

COPY --from=build /app/target/*.jar /app/conducteurs.jar
ENV JAVA_TOOL_OPTIONS="-Dserver.port=8082"
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/conducteurs.jar"]
