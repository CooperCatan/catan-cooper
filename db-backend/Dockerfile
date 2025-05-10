FROM maven:3.9.6-eclipse-temurin-21 AS build
ADD . /project
WORKDIR /project
RUN mvn -e package -DskipTests # skipping tests so i can run docker build, come back to this and fix the tests and remove this flag

FROM eclipse-temurin:latest
COPY --from=build /project/target/catan-1.0-SNAPSHOT.jar /app/catan.jar
COPY --from=build /project/firebase-service-account.json /app/firebase-service-account.json
ENTRYPOINT java -jar /app/catan.jar