# Etapa 1: Compilación de Spring Boot
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src ./src
RUN ./mvnw clean package -DskipTests && rm -rf /root/.m2

# Etapa 2: Imagen de ejecución liviana
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/.mvn ./.mvn
COPY --from=build /app/mvnw ./mvnw
COPY --from=build /app/mvnw.cmd ./mvnw.cmd
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

