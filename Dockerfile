#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY pom.xml /home/app/pom.xml
RUN mvn -f /home/app/pom.xml verify --fail-never
COPY src /home/app/src
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/mainModule-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/app.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]
