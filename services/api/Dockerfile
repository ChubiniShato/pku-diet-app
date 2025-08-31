# --- Build stage (Maven + JDK) ---
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests package

# --- Run stage (Distroless for security) ---
FROM gcr.io/distroless/java21-debian12
WORKDIR /app
COPY --from=build /workspace/target/api-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /workspace/target/openapi.json /app/static/openapi.json

# Create non-root user (distroless images handle this differently)
USER 65534:65534

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+UseContainerSupport -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US -Djava.security.egd=file:/dev/./urandom"
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
