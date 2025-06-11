# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy everything (pom.xml + src/ + mvnw + .mvn etc.)
COPY . .

# Package the Spring Boot app (skip tests for faster build)
RUN mvn clean package -DskipTests

# Stage 2: Run the app with JRE only
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
