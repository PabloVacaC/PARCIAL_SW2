package com.codeforge.generador.generator;

import java.io.IOException;
import java.io.InputStream;
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

            Path wrapperDestino =
                    proyectoGenerado.resolve(
                            ".mvn/wrapper"
                    );

            Files.createDirectories(
                    wrapperDestino
            );

            Path mvnwDestino = proyectoGenerado.resolve("mvnw");
            Path mvnwCmdDestino = proyectoGenerado.resolve("mvnw.cmd");
            Path propsDestino = wrapperDestino.resolve("maven-wrapper.properties");

            // 1. Intentar copiar desde disco si existen
            boolean copiadoDesdeDisco = false;
            if (proyectoOrigen != null) {
                Path mvnwOrigen = proyectoOrigen.resolve("mvnw");
                Path mvnwCmdOrigen = proyectoOrigen.resolve("mvnw.cmd");
                Path propsOrigen = proyectoOrigen.resolve(".mvn/wrapper/maven-wrapper.properties");

                if (Files.exists(mvnwOrigen) && Files.exists(mvnwCmdOrigen) && Files.exists(propsOrigen)) {
                    Files.copy(mvnwOrigen, mvnwDestino, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(mvnwCmdOrigen, mvnwCmdDestino, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(propsOrigen, propsDestino, StandardCopyOption.REPLACE_EXISTING);
                    copiadoDesdeDisco = true;
                }
            }

            // 2. Fallback: Copiar desde resources (embebidos en el JAR de la app)
            if (!copiadoDesdeDisco) {
                copiarRecurso("/wrapper/mvnw", mvnwDestino);
                copiarRecurso("/wrapper/mvnw.cmd", mvnwCmdDestino);
                copiarRecurso("/wrapper/maven-wrapper.properties", propsDestino);
            }

            // Dar permisos de ejecución a mvnw para Linux/macOS
            try {
                mvnwDestino.toFile().setExecutable(true, false);
            } catch (Exception ignored) {
            }

            System.out.println(
                    "GENERADOR: Maven Wrapper generado correctamente (desde disco=" + copiadoDesdeDisco + ")."
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo generar el Maven Wrapper.",
                    e
            );
        }
    }

    private void copiarRecurso(String rutaRecurso, Path destino) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(rutaRecurso)) {
            if (is != null) {
                Files.copy(is, destino, StandardCopyOption.REPLACE_EXISTING);
            } else {
                System.err.println("GENERADOR: Recurso no encontrado: " + rutaRecurso);
            }
        }
    }
}
