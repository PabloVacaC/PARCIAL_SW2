package com.codeforge.ia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UmlImageAiService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String model;

    public UmlImageAiService(
            @Value("${codeforge.ai.gemini.api-key:}") String apiKey,
            @Value("${codeforge.ai.gemini.model:gemini-3.6-flash}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    // =========================================================
    // ANALIZAR IMAGEN
    // =========================================================

    public JsonNode analizarImagen(MultipartFile imagen) throws IOException {

        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException(
                    "La imagen es obligatoria."
            );
        }

        String mimeType = imagen.getContentType();

        if (mimeType == null) {
            throw new IllegalArgumentException(
                    "No se pudo determinar el tipo de imagen."
            );
        }

        if (!esImagenSoportada(mimeType)) {
            throw new IllegalArgumentException(
                    "Solo se permiten imágenes PNG, JPG, JPEG o WEBP."
            );
        }

        validarApiKey();

        String prompt = construirPromptImagen();

        return ejecutarGemini(
                prompt,
                imagen.getBytes(),
                mimeType
        );
    }

    // =========================================================
    // ANALIZAR AUDIO
    // =========================================================

    public JsonNode analizarAudio(MultipartFile audio) throws IOException {

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException(
                    "El audio es obligatorio."
            );
        }

        String mimeType = audio.getContentType();

        if (mimeType == null) {
            throw new IllegalArgumentException(
                    "No se pudo determinar el tipo de audio."
            );
        }

        if (!esAudioSoportado(mimeType)) {
            throw new IllegalArgumentException(
                    "Formato de audio no soportado. "
                            + "Usa MP3, WAV, M4A, AAC, OGG, FLAC u OPUS."
            );
        }

        validarApiKey();

        String prompt = construirPromptAudio();

        return ejecutarGemini(
                prompt,
                audio.getBytes(),
                mimeType
        );
    }

    // =========================================================
    // LLAMADA COMÚN A GEMINI
    // =========================================================

    private JsonNode ejecutarGemini(
            String prompt,
            byte[] archivo,
            String mimeType
    ) throws IOException {

        try (Client client = Client.builder()
                .apiKey(apiKey)
                .build()) {

            Content content = Content.fromParts(
                    Part.fromText(prompt),
                    Part.fromBytes(
                            archivo,
                            mimeType
                    )
            );

            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .responseMimeType("application/json")
                            .build();

            GenerateContentResponse response =
                    client.models.generateContent(
                            model,
                            content,
                            config
                    );

            String texto = response.text();

            if (texto == null || texto.isBlank()) {
                throw new IllegalStateException(
                        "Gemini no devolvió una respuesta."
                );
            }

            return objectMapper.readTree(
                    limpiarRespuesta(texto)
            );
        }
    }

    // =========================================================
    // PROMPT PARA IMAGEN
    // =========================================================

    private String construirPromptImagen() {

        return """
                Analiza la imagen que te estoy enviando.

                La imagen contiene un diseño conceptual,
                diagrama UML o diagrama de clases.

                Identifica las clases que aparecen,
                sus atributos, operaciones y las relaciones visibles.

                Debes devolver únicamente JSON válido.
                NO escribas explicaciones.
                NO uses Markdown.
                NO uses bloques ```json.

                Usa exactamente esta estructura:

                {
                  "versionUML": "2.5",
                  "elementos": [
                    {
                      "id": "",
                      "tipo": "CLASE",
                      "nombre": "",
                      "visibilidad": "PUBLIC",
                      "estereotipo": null,
                      "abstracto": false,
                      "finalElemento": false,
                      "posicion": {
                        "x": 100,
                        "y": 100
                      },
                      "tamaño": {
                        "ancho": 260,
                        "alto": 180
                      },
                      "atributos": [],
                      "operaciones": []
                    }
                  ],
                  "relaciones": []
                }

                REGLAS:

                1. Identifica únicamente los elementos que realmente
                   aparezcan en la imagen.

                2. Usa CLASE, INTERFACE o ENUM según corresponda.

                3. Identifica atributos visibles.

                4. Identifica operaciones visibles.

                5. Identifica parámetros de las operaciones
                   cuando sean visibles.

                6. Identifica las relaciones visibles.

                7. Los tipos de relación permitidos son:

                   ASOCIACION
                   GENERALIZACION
                   REALIZACION
                   AGREGACION
                   COMPOSICION
                   DEPENDENCIA

                8. Identifica multiplicidades cuando sean visibles:

                   1
                   0..1
                   *
                   0..*
                   1..*

                9. No inventes información que no pueda verse.

                10. Cada elemento debe tener un ID único.

                11. Cada atributo debe tener un ID único.

                12. Cada operación debe tener un ID único.

                13. Cada parámetro debe tener un ID único.

                14. Ubica aproximadamente los elementos según
                    su posición en la imagen.

                15. Utiliza versionUML = 2.5.

                16. Devuelve únicamente JSON válido.
                """;
    }

    // =========================================================
    // PROMPT PARA AUDIO
    // =========================================================

    private String construirPromptAudio() {

        return """
                Analiza cuidadosamente el audio que te estoy enviando.

                El audio contiene una descripción hablada de un
                diseño conceptual, sistema, diagrama UML o modelo
                de clases.

                La persona puede describir:

                - nombres de clases
                - atributos
                - tipos de atributos
                - métodos u operaciones
                - relaciones entre clases
                - herencia
                - composición
                - agregación
                - dependencias
                - multiplicidades
                - interfaces
                - enumeraciones

                Convierte toda la información relevante del audio
                en un modelo UML estructurado para CodeForge.

                Debes devolver únicamente JSON válido.

                NO escribas explicaciones.
                NO uses Markdown.
                NO uses bloques ```json.

                Utiliza exactamente esta estructura:

                {
                  "versionUML": "2.5",
                  "elementos": [
                    {
                      "id": "",
                      "tipo": "CLASE",
                      "nombre": "",
                      "visibilidad": "PUBLIC",
                      "estereotipo": null,
                      "abstracto": false,
                      "finalElemento": false,
                      "posicion": {
                        "x": 100,
                        "y": 100
                      },
                      "tamaño": {
                        "ancho": 260,
                        "alto": 180
                      },
                      "atributos": [
                        {
                          "id": "",
                          "nombre": "",
                          "tipo": "String",
                          "visibilidad": "PRIVATE",
                          "multiplicidad": "1",
                          "derivado": false,
                          "estatico": false,
                          "valorInicial": null
                        }
                      ],
                      "operaciones": [
                        {
                          "id": "",
                          "nombre": "",
                          "visibilidad": "PUBLIC",
                          "estatica": false,
                          "abstracta": false,
                          "tipoRetorno": "void",
                          "parametros": [
                            {
                              "id": "",
                              "nombre": "",
                              "tipo": "String",
                              "direccion": "IN",
                              "valorInicial": null
                            }
                          ]
                        }
                      ]
                    }
                  ],
                  "relaciones": [
                    {
                      "id": "",
                      "tipo": "ASOCIACION",
                      "origen": "",
                      "destino": "",
                      "rolOrigen": null,
                      "rolDestino": null,
                      "multiplicidadOrigen": null,
                      "multiplicidadDestino": null
                    }
                  ]
                }

                REGLAS:

                1. Crea únicamente las clases que la persona
                   realmente menciona.

                2. Crea INTERFACE únicamente cuando la persona
                   indique explícitamente que es una interfaz.

                3. Crea ENUM únicamente cuando la persona
                   indique explícitamente una enumeración.

                4. Extrae los atributos mencionados.

                5. Extrae el tipo del atributo cuando sea indicado.

                6. Si no se especifica el tipo de un atributo,
                   utiliza String.

                7. Extrae las operaciones o métodos mencionados.

                8. Extrae los parámetros de las operaciones
                   cuando estén descritos.

                9. Extrae las relaciones entre clases.

                10. Los tipos de relación permitidos son:

                    ASOCIACION
                    GENERALIZACION
                    REALIZACION
                    AGREGACION
                    COMPOSICION
                    DEPENDENCIA

                11. Extrae las multiplicidades si son indicadas.

                12. No inventes relaciones que no sean descritas.

                13. No inventes atributos que no sean mencionados.

                14. No inventes operaciones que no sean mencionadas.

                15. Cada elemento debe tener un ID único.

                16. Cada atributo debe tener un ID único.

                17. Cada operación debe tener un ID único.

                18. Cada parámetro debe tener un ID único.

                19. Coloca las clases en posiciones ordenadas
                    para que el diagrama sea fácil de visualizar.

                20. Utiliza versionUML = 2.5.

                21. Devuelve únicamente JSON válido.
                """;
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarApiKey() {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "La variable GEMINI_API_KEY no está configurada."
            );
        }
    }

    private boolean esImagenSoportada(String mimeType) {

        return mimeType.equals("image/png")
                || mimeType.equals("image/jpeg")
                || mimeType.equals("image/jpg")
                || mimeType.equals("image/webp");
    }

    private boolean esAudioSoportado(String mimeType) {

        String tipo = mimeType.toLowerCase();

        return tipo.startsWith("audio/wav")
                || tipo.startsWith("audio/x-wav")
                || tipo.startsWith("audio/wave")
                || tipo.startsWith("audio/mp3")
                || tipo.startsWith("audio/mpeg")
                || tipo.startsWith("audio/mp4")
                || tipo.startsWith("audio/m4a")
                || tipo.startsWith("audio/aac")
                || tipo.startsWith("audio/ogg")
                || tipo.startsWith("audio/flac")
                || tipo.startsWith("audio/opus")
                || tipo.startsWith("audio/webm");
    }

    // =========================================================
    // LIMPIAR RESPUESTA
    // =========================================================

    private String limpiarRespuesta(String texto) {

        texto = texto.trim();

        if (texto.startsWith("```json")) {
            texto = texto.substring(7);
        } else if (texto.startsWith("```")) {
            texto = texto.substring(3);
        }

        if (texto.endsWith("```")) {
            texto = texto.substring(
                    0,
                    texto.length() - 3
            );
        }

        return texto.trim();
    }
}