# Stage 1: Build the Quarkus application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy pom.xml and Maven wrapper to leverage Docker cache
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the application source code
COPY src ./src

# Build the application
RUN ./mvnw package -Dquarkus.package.type=fast-jar -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built JAR and libs from the build stage
COPY --from=build /app/target/quarkus-app/lib/ /app/lib/
COPY --from=build /app/target/quarkus-app/*.jar /app/
COPY --from=build /app/target/quarkus-app/app/ /app/app/
COPY --from=build /app/target/quarkus-app/quarkus/ /app/quarkus/


EXPOSE 8080

CMD ["java", "-jar", "quarkus-run.jar"]
