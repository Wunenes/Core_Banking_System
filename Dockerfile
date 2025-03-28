FROM openjdk:23-jdk-slim
WORKDIR /app
COPY target/spring-example-1.0-SNAPSHOT.jar /app/spring-example-1.0-SNAPSHOT.jar
EXPOSE 8080
CMD ["java", "-jar", "spring-example-1.0-SNAPSHOT.jar"]