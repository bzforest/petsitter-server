# Step 1: Build the application (ใช้ Maven 3.9 ซึ่งรองรับ Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
# ให้สิทธิ์การรันไฟล์ mvnw
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:21-jdk-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]