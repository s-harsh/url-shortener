# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependencies separately from source
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Security: run as non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/target/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
