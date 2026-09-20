package com.codeforge.ia.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeforge.ia.service.CodeForgeAiService;

@RestController
@RequestMapping("/api/ia/chat")
@CrossOrigin(origins = "http://localhost:4200")
public class CodeForgeAiController {

    private final CodeForgeAiService codeForgeAiService;

    public CodeForgeAiController(
            CodeForgeAiService codeForgeAiService
    ) {
        this.codeForgeAiService = codeForgeAiService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> preguntar(
            @RequestBody Map<String, String> body
    ) {

        try {

            String mensaje = body.get("mensaje");

            String respuesta =
                    codeForgeAiService.responder(mensaje);

            return ResponseEntity.ok(
                    Map.of(
                            "respuesta",
                            respuesta
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    "No se pudo procesar la pregunta."
                            )
                    );
        }
    }
}