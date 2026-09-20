package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

@Component
public class MavenWrapperGenerator {

    public void generar(
            Path proyectoGenerado,
            Path proyectoOrigen
    ) {

        try {

            Path wrapperOrigen =
                    proyectoOrigen.resolve(
                            ".mvn/wrapper"
                    );

            Path wrapperDestino =
                    proyectoGenerado.resolve(
                            ".mvn/wrapper"
                    );

            Files.createDirectories(
                    wrapperDestino
            );

            Files.copy(
                    proyectoOrigen.resolve("mvnw"),
                    proyectoGenerado.resolve("mvnw"),
                    StandardCopyOption.REPLACE_EXISTING
            );

            Files.copy(
                    proyectoOrigen.resolve("mvnw.cmd"),
                    proyectoGenerado.resolve("mvnw.cmd"),
                    StandardCopyOption.REPLACE_EXISTING
            );

            Files.copy(
                    wrapperOrigen.resolve(
                            "maven-wrapper.properties"
                    ),
                    wrapperDestino.resolve(
                            "maven-wrapper.properties"
                    ),
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println(
                    "GENERADOR: Maven Wrapper copiado correctamente."
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo copiar el Maven Wrapper.",
                    e
            );
        }
    }
}