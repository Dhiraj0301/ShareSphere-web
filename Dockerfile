# Stage 1: Build the application
FROM maven:3.8.5-eclipse-temurin-17 AS builder

# Set working directory inside the container
WORKDIR /build

# Copy only the ShareSphere directory which contains pom.xml and code
COPY ShareSphere /build

# Package the Spring Boot app (skip tests for faster build)
RUN mvn clean package -DskipTests

# Stage 2: Run the app with JRE only
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=builder /build/target/ShareSphere-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

