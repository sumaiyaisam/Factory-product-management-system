#FROM maven:3.9-eclipse-temurin-21 AS build
#
#WORKDIR /app
#
#COPY pom.xml .
#COPY .mvn .mvn
#COPY mvnw .
#
#RUN chmod +x mvnw
#
#COPY src src
#
#RUN ./mvnw -B clean package -DskipTests
#
#FROM eclipse-temurin:21-jre
#
#WORKDIR /app
#
#RUN useradd --system --uid 10001 appuser
#
#COPY --from=build /app/target/*.jar app.jar
#
#USER appuser
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]
