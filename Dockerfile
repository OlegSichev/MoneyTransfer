FROM openjdk:17-alpine
EXPOSE 5500
ENV TZ Europe/Moscow
ADD /target/MoneyTransferService-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]