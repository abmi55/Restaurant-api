# Build stage. Maven 3.9.11 matches the version pinned by the Maven wrapper.
FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
# Tests already run in CI.
RUN mvn -B -ntp package -DskipTests

# Run stage: JRE only, no Maven or sources.
FROM eclipse-temurin:25-jre
RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY --from=build /build/target/restaurant-api-1.0-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
