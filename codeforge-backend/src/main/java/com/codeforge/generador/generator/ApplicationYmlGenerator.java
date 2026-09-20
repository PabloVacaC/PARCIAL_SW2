package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class ApplicationYmlGenerator {

    public Path generar(
            Path proyecto
    ) {

        try {

            Path resources =
                    proyecto.resolve(
                            "src/main/resources"
                    );

            Files.createDirectories(
                    resources
            );

            Path archivoYml =
                    resources.resolve(
                            "application.yml"
                    );

            String contenidoYml = """
                    spring:
                      application:
                        name: codeforge-generated-backend

                      datasource:
                        url: ${DB_URL:jdbc:postgresql://localhost:5432/codeforge_generated}
                        username: ${DB_USERNAME:postgres}
                        password: ${DB_PASSWORD:1234}
                        driver-class-name: org.postgresql.Driver

                      jpa:
                        hibernate:
                          ddl-auto: update
                        open-in-view: false
                        show-sql: true

                    server:
                      port: 8081
                    """;

            Files.writeString(
                    archivoYml,
                    contenidoYml,
                    StandardCharsets.UTF_8
            );

            Path archivoProperties =
                    resources.resolve(
                            "application.properties"
                    );

            String contenidoProperties = """
                    spring.application.name=codeforge-generated-backend

                    spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/codeforge_generated}
                    spring.datasource.username=${DB_USERNAME:postgres}
                    spring.datasource.password=${DB_PASSWORD:1234}
                    spring.datasource.driver-class-name=org.postgresql.Driver

                    spring.jpa.hibernate.ddl-auto=update
                    spring.jpa.open-in-view=false
                    spring.jpa.show-sql=true

                    server.port=8081
                    """;

            Files.writeString(
                    archivoProperties,
                    contenidoProperties,
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "GENERADOR: application.yml y application.properties creados en: " +
                            resources
            );

            return archivoProperties;

        } catch (
                IOException e
        ) {

            throw new RuntimeException(
                    "No se pudo generar application.yml.",
                    e
            );
        }
    }
}