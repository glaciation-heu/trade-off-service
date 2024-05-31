FROM maven:3-eclipse-temurin-21-alpine as builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ ./src/
RUN mvn package -DskipTests

FROM maven:3-eclipse-temurin-21-alpine
COPY --from=builder /app/target/tradeoff-service-0.0.1-SNAPSHOT.jar tradeoff-service.jar
ENTRYPOINT ["java", "-Dspring.config.location=/conf/", "-Dspring.profiles.active=dev", "-jar", "/tradeoff-service.jar"]
