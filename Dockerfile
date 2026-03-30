# Step 1: Build the application
FROM maven:3.8.5-openjdk-21 AS build
COPY . .
RUN ./mvnw clean package -DskipTests

# Step 2: Run the application
FROM openjdk:21-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]