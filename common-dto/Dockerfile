FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY . .

RUN mvn clean package -pl common-dto -am -DskipTests

FROM openjdk:17-slim
WORKDIR /app

COPY --from=builder /app/common-dto/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]