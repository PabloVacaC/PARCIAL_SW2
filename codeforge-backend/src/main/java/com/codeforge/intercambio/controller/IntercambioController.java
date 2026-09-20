package com.codeforge.intercambio.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeforge.intercambio.service.IntercambioService;
import com.codeforge.intercambio.service.XmiExportService;
import com.codeforge.intercambio.service.XmiImportService;

@RestController
@RequestMapping("/api/intercambio")
public class IntercambioController {

    private final IntercambioService intercambioService;

    private final XmiExportService xmiExportService;

    private final XmiImportService xmiImportService;

    public IntercambioController(
            IntercambioService intercambioService,
            XmiExportService xmiExportService,
            XmiImportService xmiImportService
    ) {

        this.intercambioService =
                intercambioService;

        this.xmiExportService =
                xmiExportService;

        this.xmiImportService =
                xmiImportService;
    }

    /*
     * ============================================================
     * JSON
     * ============================================================
     */

    @GetMapping(
            value = "/modelos/{idModelo}/json",
            produces =
                    MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> exportarJson(
            @PathVariable("idModelo")
            Long idModelo
    ) {

        Map<String, Object> contenido =
                intercambioService.exportarJson(
                        idModelo
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"modelo-" +
                                idModelo +
                                ".json\""
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(
                        contenido
                );
    }

    @PostMapping(
            value = "/modelos/{idModelo}/json",
            consumes =
                    MediaType.APPLICATION_JSON_VALUE,
            produces =
                    MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> importarJson(
            @PathVariable("idModelo")
            Long idModelo,
            @RequestBody
            Map<String, Object> contenido
    ) {

        Map<String, Object> resultado =
                intercambioService.importarJson(
                        idModelo,
                        contenido
                );

        return ResponseEntity.ok(
                resultado
        );
    }

    /*
     * ============================================================
     * XMI
     * ============================================================
     */

    @GetMapping(
            value = "/modelos/{idModelo}/xmi",
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<byte[]> exportarXmi(
            @PathVariable("idModelo")
            Long idModelo
    ) {

        byte[] contenido =
                xmiExportService.exportar(
                        idModelo
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"modelo-" +
                                idModelo +
                                ".xmi\""
                )
                .contentType(
                        MediaType.APPLICATION_XML
                )
                .body(
                        contenido
                );
    }

    @PostMapping(
            value = "/modelos/{idModelo}/xmi",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces =
                    MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> importarXmi(
            @PathVariable("idModelo")
            Long idModelo,
            @RequestBody
            String xml
    ) {

        Map<String, Object> contenido =
                xmiImportService.importar(
                        xml
                );

        Map<String, Object> resultado =
                intercambioService.importarJson(
                        idModelo,
                        contenido
                );

        return ResponseEntity.ok(
                resultado
        );
    }
}