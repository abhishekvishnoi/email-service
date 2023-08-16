FROM openjdk:17-slim-buster
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java" , "-jar" , "/app.jar" ]
ENTRYPOINT ["java","-jar","/app.jar" , "--spring.config.location=file:///etc/config/application.properties"]
EXPOSE 8080