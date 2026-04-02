# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Cache dependencies before copying source
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Skip tests - handled in CI pipeline
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# curl for healthcheck; non-root user for security
RUN apk add --no-cache curl && \
    addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/*.jar app.jar
# ZGC + Generational GC - optimized for Virtual Threads (Java 21)
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-jar", "app.jar"]