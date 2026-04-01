# Build the JAR with Maven, then run it in a small JRE image.
# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/app.jar app.jar
# Must match server.port in application.properties (gateway forwards to this)
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
