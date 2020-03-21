FROM openjdk:8-jdk-alpine AS builder
WORKDIR /chat
ADD .mvn .mvn
ADD mvnw mvnw
ADD pom.xml pom.xml
ADD ./src /chat/src
RUN ./mvnw -Dmaven.test.skip=true package

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=builder /chat/target/*-0.0.1-SNAPSHOT.jar .
CMD exec java -jar *.jar