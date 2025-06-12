# Stage 1: Build using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory to /app
WORKDIR /app

# Copy pom.xml and download dependencies first (for Docker caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline

# Now copy all project files
COPY . .

# Build the project (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the app using a lightweight JDK
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Spring Boot default)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
