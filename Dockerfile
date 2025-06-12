# Stage 1: Build using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy only pom.xml and download dependencies first
COPY pom.xml ./
RUN mvn dependency:go-offline

# Now copy the full project
COPY . .

# Build the project (skip tests for faster build)
RUN mvn clean package -DskipTests

# Stage 2: Run the app using lightweight image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
