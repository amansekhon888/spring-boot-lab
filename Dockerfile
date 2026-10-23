# Stage 1: Build the Java Application using Maven with Java 17
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the build file first so Docker can cache dependency downloads.
COPY pom.xml .
COPY src ./src
RUN mvn -B package -Dmaven.test.skip=true

# Stage 2: Lightweight runtime environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q -O - http://localhost:8080/api/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
