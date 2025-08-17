# --- Build stage (Maven + JDK) ---
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests package

# --- Run stage (JRE only) ---
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -ms /bin/bash appuser
COPY --from=build /workspace/target/api-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+UseContainerSupport -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US"
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
