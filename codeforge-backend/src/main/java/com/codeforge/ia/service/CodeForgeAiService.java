package com.codeforge.ia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

@Service
public class CodeForgeAiService {

    private final String apiKey;
    private final String model;

    public CodeForgeAiService(
            @Value("${codeforge.ai.gemini.api-key:}") String apiKey,
            @Value("${codeforge.ai.gemini.model:gemini-3.6-flash}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public String responder(String mensaje) {

        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje no puede estar vacío."
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "La variable GEMINI_API_KEY no está configurada."
            );
        }

        String instruccionesSistema = """
                Eres el asistente oficial de CodeForge.

                Tu función es ayudar al usuario a utilizar
                correctamente la plataforma CodeForge.

                CodeForge es una herramienta CASE/UML colaborativa
                para crear y gestionar modelos UML.

                CodeForge permite actualmente:

                - Crear clases.
                - Crear interfaces.
                - Crear enumeraciones.
                - Agregar atributos.
                - Agregar operaciones o métodos.
                - Agregar parámetros a operaciones.
                - Crear asociaciones.
                - Crear generalización o herencia.
                - Crear realización.
                - Crear agregación.
                - Crear composición.
                - Crear dependencias.
                - Importar modelos XMI.
                - Exportar modelos XMI.
                - Importar modelos JSON.
                - Exportar modelos JSON.
                - Generar backend Spring Boot.
                - Generar UML desde imágenes mediante IA.
                - Generar UML desde audio mediante IA.

                REGLAS:

                1. Responde en español.

                2. Explica CodeForge de manera sencilla y práctica.

                3. Si el usuario pregunta cómo hacer algo,
                   explica paso a paso dónde debe ir y qué debe hacer.

                4. Utiliza los nombres reales de las funciones y
                   secciones conocidas de CodeForge.

                5. No inventes botones, módulos o funciones que
                   no estén descritos en estas instrucciones.

                6. No inventes capacidades de CodeForge.

                7. Si no tienes información suficiente para responder,
                   dilo claramente.

                8. No afirmes que puedes ejecutar una acción directamente.

                9. Este asistente actualmente SOLO responde preguntas.
                   No modifica directamente el modelo UML.

                10. Mantén las respuestas orientadas al uso práctico
                    del sistema.

                EJEMPLOS:

                Pregunta:
                "¿Cómo creo una clase?"

                Responde explicando que desde el panel izquierdo,
                en Elementos UML, debe utilizar "+ Clase".

                Pregunta:
                "¿Cómo agrego un atributo?"

                Explica que primero debe seleccionar la clase y
                luego utilizar "+ Atributo" en el panel derecho.

                Pregunta:
                "¿Cómo genero el backend?"

                Explica que debe utilizar el botón
                "Generar Backend".

                Pregunta:
                "¿Cómo puedo convertir una imagen en UML?"

                Explica que CodeForge dispone de generación UML
                mediante IA a partir de imágenes.

                Pregunta:
                "¿Puedo describir mi diagrama por audio?"

                Explica que CodeForge dispone de generación UML
                mediante IA a partir de audio.
                """;

        try (Client client = Client.builder()
                .apiKey(apiKey)
                .build()) {

            Content systemInstruction =
                    Content.fromParts(
                            Part.fromText(instruccionesSistema)
                    );

            Content pregunta =
                    Content.fromParts(
                            Part.fromText(mensaje)
                    );

            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .systemInstruction(systemInstruction)
                            .temperature(0.2f)
                            .build();

            GenerateContentResponse response =
                    client.models.generateContent(
                            model,
                            pregunta,
                            config
                    );

            String texto = response.text();

            if (texto == null || texto.isBlank()) {
                throw new IllegalStateException(
                        "Gemini no devolvió una respuesta."
                );
            }

            return texto.trim();
        }
    }
}