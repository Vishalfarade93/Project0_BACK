FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8989

ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8989"]
