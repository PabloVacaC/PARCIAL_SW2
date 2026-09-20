package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class ApplicationGenerator {

    public Path generar(
            Path proyecto,
            String nombreAplicacion
    ) {

        try {

            String nombreClase =
                    convertirAClaseJava(
                            nombreAplicacion
                    );

            Path carpetaJava =
                    proyecto.resolve(
                            "src/main/java/com/codeforge/generated"
                    );

            Files.createDirectories(
                    carpetaJava
            );

            Path archivo =
                    carpetaJava.resolve(
                            nombreClase +
                                    "Application.java"
                    );

            String contenido =
                    """
                    package com.codeforge.generated;

                    import org.springframework.boot.SpringApplication;
                    import org.springframework.boot.autoconfigure.SpringBootApplication;

                    @SpringBootApplication
                    public class %sApplication {

                        public static void main(String[] args) {

                            SpringApplication.run(
                                    %sApplication.class,
                                    args
                            );
                        }
                    }
                    """
                    .formatted(
                            nombreClase,
                            nombreClase
                    );

            Files.writeString(
                    archivo,
                    contenido,
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "GENERADOR: clase principal creada: " +
                            archivo
            );

            return archivo;

        } catch (
                IOException e
        ) {

            throw new RuntimeException(
                    "No se pudo generar la clase principal de Spring Boot.",
                    e
            );
        }
    }

    private String convertirAClaseJava(
            String nombre
    ) {

        if (
                nombre == null ||
                nombre.isBlank()
        ) {

            return "Generated";
        }

        /*
         * Conservamos exactamente el nombre recibido
         * cuando ya viene en formato PascalCase.
         *
         * Ejemplo:
         *
         * CodeForgeGenerated
         *        ↓
         * CodeForgeGenerated
         */
        String nombreLimpio =
                nombre.trim();

        if (
                nombreLimpio.matches(
                        "[a-zA-Z][a-zA-Z0-9]*"
                )
        ) {
            return nombreLimpio;
        }

        /*
         * Si contiene espacios, guiones u otros separadores,
         * construimos correctamente el nombre de clase.
         *
         * Ejemplo:
         *
         * mi proyecto
         *        ↓
         * MiProyecto
         */

        String[] partes =
                nombreLimpio
                        .split(
                                "[^a-zA-Z0-9]+"
                        );

        StringBuilder resultado =
                new StringBuilder();

        for (
                String parte :
                partes
        ) {

            if (
                    parte.isBlank()
            ) {
                continue;
            }

            resultado.append(
                    Character.toUpperCase(
                            parte.charAt(0)
                    )
            );

            if (
                    parte.length() > 1
            ) {

                resultado.append(
                        parte.substring(1)
                );
            }
        }

        if (
                resultado.isEmpty()
        ) {

            return "Generated";
        }

        return resultado.toString();
    }
}