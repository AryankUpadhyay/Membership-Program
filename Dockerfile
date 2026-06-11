# ── Stage 1: Build ─────────────────────────────────────────────────────────────
# Using eclipse-temurin:21-jdk-alpine — supports both linux/amd64 AND linux/arm64
# (Apple Silicon M1/M2/M3 compatible). Java 17 Alpine has no arm64 manifest.
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and POM first for dependency layer caching
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Ensure the wrapper is executable (critical in Docker — file permissions may be lost)
RUN chmod +x ./mvnw

# Download dependencies (this layer is cached as long as pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source and build the fat JAR
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
# Minimal JRE-only image — same arm64/amd64 compatibility as builder
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root group and user for security
RUN addgroup -S firstclub && adduser -S -G firstclub app
USER app

# Copy only the fat JAR from the builder stage
COPY --from=builder /app/target/membership-program-1.0.0.jar app.jar

EXPOSE 8080

# Health check against a real API endpoint (no Actuator dependency needed)
HEALTHCHECK --interval=30s --timeout=10s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/v1/plans || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
