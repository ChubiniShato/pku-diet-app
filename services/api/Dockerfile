# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -Dspotless.check.skip=true -Dproject.build.outputTimestamp=2020-01-01T00:00:00Z package

# ---- Runtime stage ----
FROM gcr.io/distroless/java21-debian12
WORKDIR /app
COPY --from=build /src/target/*.jar /app/app.jar
USER 65534:65534
EXPOSE 8080

# OCI Labels for traceability
LABEL org.opencontainers.image.source="https://github.com/chubini/pku-diet-app"
LABEL org.opencontainers.image.description="PKU Diet App API"
LABEL org.opencontainers.image.licenses="Apache-2.0"

# JVM Tuning for containers
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 \
    -XX:+UseContainerSupport \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp \
    -XX:+ExitOnOutOfMemoryError \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=UTC \
    -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["java","-jar","/app/app.jar"]