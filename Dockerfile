FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/notification-service-0.0.1-SNAPSHOT.jar /app/notification.jar

EXPOSE 8084

CMD ["java", "-jar", "notification.jar"]
