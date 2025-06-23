# --- STAGE 1: Build Stage ---
# Use an official Maven image with a specific JDK version to build the application.
# This stage contains all the build tools like Maven and the full JDK.
FROM maven:3.9.8-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project object model file. This is done first to leverage Docker's layer caching.
# If pom.xml doesn't change, Docker won't re-download dependencies.
COPY pom.xml .

# Download all the project dependencies
RUN mvn dependency:go-offline

# Copy the rest of the application's source code
COPY src ./src

# Build the application, skipping the tests since we've already run them.
# This creates the executable .jar file.
RUN mvn package -DskipTests


# --- STAGE 2: Runtime Stage ---
# Use a minimal, secure Java runtime image. This image does not contain Maven or the JDK,
# making it much smaller and more secure than the builder image.
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy only the built .jar file from the 'builder' stage into our new, clean stage.
# The JAR file is located in the 'target' directory of the builder.
COPY --from=builder /app/target/*.jar app.jar

# Expose the port that the Spring Boot application will run on
EXPOSE 8080

# The command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]