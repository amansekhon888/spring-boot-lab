# Stage 1: Build the Java Application using Maven with Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code directly
COPY pom.xml .
COPY src ./src

# # Download dependencies in advance to cache layers
# RUN mvn dependency:go-offline
# COPY src ./src
# Package application and skip tests
RUN mvn clean package -DskipTests

# Stage 2: Lightweight runtime environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]