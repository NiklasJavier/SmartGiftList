# Stage 1: Build SmartGiftList.war with Gradle and JDK 21
FROM docker.io/library/gradle:8.14-jdk21 AS build
WORKDIR /workspace

COPY . .
RUN chmod +x ./gradlew && ./gradlew --no-daemon war test

# Stage 2: Deploy onto Apache Tomcat 10.1 (Jakarta EE / JDK 21)
FROM docker.io/library/tomcat:10.1-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /workspace/build/libs/SmartGiftList.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
