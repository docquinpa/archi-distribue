FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

COPY --from=build /app/target/*.jar /app/gateway.jar
ENV JAVA_TOOL_OPTIONS="-Dserver.port=8086"
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "/app/gateway.jar"]