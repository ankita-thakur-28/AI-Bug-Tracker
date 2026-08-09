# Stage 1: Build the backend application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create execution environment
FROM mcr.microsoft.com/playwright:v1.45.0-jammy
WORKDIR /app

# Install Java 17 JRE and Docker CLI
RUN apt-get update && \
    apt-get install -y openjdk-17-jre-headless docker.io && \
    rm -rf /var/lib/apt/lists/*

# Set Node path to resolve playwright packages globally
ENV NODE_PATH=/usr/lib/node_modules

# Copy built jar from stage 1
COPY --from=build /app/target/AI-BugTracker-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
