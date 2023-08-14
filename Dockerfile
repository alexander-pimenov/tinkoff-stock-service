FROM bellsoft/liberica-openjdk-alpine:11.0.11
ARG JAR_FILE=target/*.jar
#COPY ./target/TinkoffStockService-0.0.1-SNAPSHOT.jar .
COPY ${JAR_FILE} app.jar
#CMD ["java","-jar","TinkoffStockService-0.0.1-SNAPSHOT.jar"]
CMD ["java", "-jar", "/app.jar"]