FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /project
COPY pom.xml .
COPY src ./src
RUN mvn -e package

FROM eclipse-temurin:17-jre
COPY --from=build /project/target /app/target
ENTRYPOINT java -jar /app/target/catan-1.0-SNAPSHOT.jar