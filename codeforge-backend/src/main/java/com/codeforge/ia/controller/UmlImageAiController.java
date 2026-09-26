package com.codeforge.ia.controller;

import com.codeforge.ia.service.UmlImageAiService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ia/uml")
@CrossOrigin(origins = "http://localhost:4200")
public class UmlImageAiController {

    private final UmlImageAiService umlImageAiService;

    public UmlImageAiController(
            UmlImageAiService umlImageAiService
    ) {
        this.umlImageAiService = umlImageAiService;
    }

    // =========================================================
    // GENERAR UML DESDE IMAGEN
    // =========================================================

    @PostMapping(
            value = "/imagen",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> analizarImagen(
            @RequestParam("imagen") MultipartFile imagen
    ) {

        try {

            JsonNode resultado =
                    umlImageAiService.analizarImagen(imagen);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resultado.toString());

        } catch (IllegalArgumentException | IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "{\"error\":\""
                                    + escapar(e.getMessage())
                                    + "\"}"
                    );

        } catch (Exception e) {

            e.printStackTrace();

            String msg = e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage()
                    : "Error procesando la imagen con Gemini.";

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "{\"error\":\""
                                    + escapar(msg)
                                    + "\"}"
                    );
        }
    }

    // =========================================================
    // GENERAR UML DESDE AUDIO
    // =========================================================

    @PostMapping(
            value = "/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> analizarAudio(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "contexto", required = false) String contexto
    ) {

        try {

            JsonNode resultado =
                    umlImageAiService.analizarAudio(audio, contexto);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resultado.toString());

        } catch (IllegalArgumentException | IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "{\"error\":\""
                                    + escapar(e.getMessage())
                                    + "\"}"
                    );

        } catch (Exception e) {

            e.printStackTrace();

            String msg = e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage()
                    : "Error procesando el audio con Gemini.";

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "{\"error\":\""
                                    + escapar(msg)
                                    + "\"}"
                    );
        }
    }

    // =========================================================
    // ESCAPAR MENSAJES JSON
    // =========================================================

    private String escapar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}