# ====== 1. Build Stage ======
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven configuration first to cache dependencies
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copy source and build the jar
COPY src ./src
RUN mvn -q clean package -DskipTests

# ====== 2. Runtime Stage ======
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy only the built JAR from previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
