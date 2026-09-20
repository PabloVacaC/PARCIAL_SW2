package com.codeforge.generador.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeforge.generador.service.BackendGeneratorService;

@RestController
@RequestMapping("/api/generador")
public class GeneradorController {

    private final BackendGeneratorService backendGeneratorService;

    public GeneradorController(
            BackendGeneratorService backendGeneratorService
    ) {

        this.backendGeneratorService =
                backendGeneratorService;
    }

    @PostMapping(
            "/modelos/{idModelo}/backend"
    )
    public ResponseEntity<Resource> generarBackend(
            @PathVariable("idModelo")
            Long idModelo
    ) {

        /*
         * ============================================================
         * GENERAR PROYECTO
         * ============================================================
         */

        Path carpeta =
                backendGeneratorService
                        .generarBackend(
                                idModelo
                        );

        /*
         * ============================================================
         * LOCALIZAR ZIP
         * ============================================================
         *
         * Ejemplo:
         *
         * carpeta:
         * codeforge-backend-123456
         *
         * ZIP:
         * codeforge-backend-123456.zip
         *
         */

        Path archivoZip =
                carpeta
                        .getParent()
                        .resolve(
                                carpeta.getFileName()
                                        + ".zip"
                        );

        /*
         * ============================================================
         * VERIFICAR ZIP
         * ============================================================
         */

        if (!Files.exists(archivoZip)) {

            return ResponseEntity
                    .internalServerError()
                    .build();
        }

        /*
         * ============================================================
         * CREAR RECURSO
         * ============================================================
         */

        FileSystemResource recurso =
                new FileSystemResource(
                        archivoZip
                );

        /*
         * ============================================================
         * NOMBRE DEL ARCHIVO
         * ============================================================
         */

        String nombreArchivo =
                archivoZip
                        .getFileName()
                        .toString();

        /*
         * ============================================================
         * RESPUESTA DE DESCARGA
         * ============================================================
         */

        return ResponseEntity
                .ok()
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                nombreArchivo +
                                "\""
                )
                .body(
                        recurso
                );
    }
}