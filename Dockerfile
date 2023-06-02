FROM eclipse-temurin:17-jdk-alpine

COPY . .
RUN ./mvnw package -DskipTests
RUN cp /target/messaging-*.jar /messaging-app.jar
EXPOSE 8080 8086
ENTRYPOINT ["java", "-jar","/messaging-app.jar"]
