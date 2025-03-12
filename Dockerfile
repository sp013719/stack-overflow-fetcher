FROM openjdk:17-jdk-slim

WORKDIR /app

ADD target/stack-overflow-fetcher-0.0.1-SNAPSHOT.jar /app/stack-overflow-fetcher.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "stack-overflow-fetcher.jar"]
