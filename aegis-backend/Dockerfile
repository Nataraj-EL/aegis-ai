# Stage 1: Build & Extract Layers
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY checkstyle.xml .
COPY pmd-ruleset.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests
RUN java -Djarmode=layertools -jar target/backend-0.0.1-SNAPSHOT.jar extract

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
RUN apk update && apk upgrade --no-cache
WORKDIR /app

# Create a non-root group and user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the extracted layers from build stage
COPY --from=build /app/dependencies/ ./
COPY --from=build /app/spring-boot-loader/ ./
COPY --from=build /app/snapshot-dependencies/ ./
COPY --from=build /app/application/ ./

# Change ownership of the runtime files to the non-root user
RUN chown -R spring:spring /app

USER spring

# Expose port and configure health check
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep UP || exit 1

# Environment configurations and JVM limits
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -Xms512m -Xmx1g"

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
