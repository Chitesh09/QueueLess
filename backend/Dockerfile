# Stage 1: Build Java Application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime Container
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S queueless && adduser -S queueless -G queueless

COPY --from=builder /app/target/*.jar app.jar

USER queueless

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseG1GC","-XX:MaxRAMPercentage=75.0","-Dspring.profiles.active=prod","-jar","app.jar"]