package com.codeforge.generador.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

@Component
public class ZipGenerator {

    public Path generar(
            Path proyectoGenerado
    ) {

        try {

            Path archivoZip =
                    proyectoGenerado
                            .getParent()
                            .resolve(
                                    proyectoGenerado.getFileName()
                                            + ".zip"
                            );

            try (
                    OutputStream outputStream =
                            Files.newOutputStream(
                                    archivoZip,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING
                            );

                    ZipOutputStream zipOutputStream =
                            new ZipOutputStream(
                                    outputStream
                            )
            ) {

                comprimirCarpeta(
                        proyectoGenerado,
                        proyectoGenerado,
                        zipOutputStream
                );
            }

            System.out.println(
                    "GENERADOR: ZIP creado: "
                            + archivoZip
            );

            return archivoZip;

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo generar el archivo ZIP.",
                    e
            );
        }
    }

    private void comprimirCarpeta(
            Path carpetaRaiz,
            Path rutaActual,
            ZipOutputStream zipOutputStream
    ) throws IOException {

        try (
                var stream =
                        Files.list(rutaActual)
        ) {

            for (Path elemento : stream.toList()) {

                String nombreEntrada =
                        carpetaRaiz
                                .relativize(
                                        elemento
                                )
                                .toString()
                                .replace(
                                        "\\",
                                        "/"
                                );

                if (Files.isDirectory(elemento)) {

                    comprimirCarpeta(
                            carpetaRaiz,
                            elemento,
                            zipOutputStream
                    );

                } else {

                    ZipEntry entrada =
                            new ZipEntry(
                                    nombreEntrada
                            );

                    zipOutputStream.putNextEntry(
                            entrada
                    );

                    Files.copy(
                            elemento,
                            zipOutputStream
                    );

                    zipOutputStream.closeEntry();
                }
            }
        }
    }
}