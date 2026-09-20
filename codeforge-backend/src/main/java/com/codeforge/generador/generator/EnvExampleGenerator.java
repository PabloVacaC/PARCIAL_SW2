package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class EnvExampleGenerator {

    public Path generar(Path proyecto) {

        try {

            Path archivo =
                    proyecto.resolve(".env.example");

            String contenido =
                    """
                    # ============================================================
                    # CONFIGURACION DE BASE DE DATOS
                    # ============================================================

                    DB_URL=jdbc:postgresql://localhost:5432/codeforge_generated
                    DB_USERNAME=postgres
                    DB_PASSWORD=1234

                    # ============================================================
                    # PUERTO DEL BACKEND
                    # ============================================================

                    SERVER_PORT=8081
                    """;

            Files.writeString(
                    archivo,
                    contenido,
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "GENERADOR: .env.example creado: "
                            + archivo
            );

            return archivo;

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo generar .env.example.",
                    e
            );
        }
    }
}