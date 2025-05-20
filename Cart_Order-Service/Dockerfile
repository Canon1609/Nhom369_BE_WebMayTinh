FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY build/libs/Cart_Order-Service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]