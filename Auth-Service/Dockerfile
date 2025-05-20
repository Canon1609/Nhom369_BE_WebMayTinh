# Dockerfile (đặt ở thư mục Auth-Service)
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/Authen-Service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]