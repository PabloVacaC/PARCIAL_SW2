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
            @Value("${codeforge.ai.gemini.model:gemini-3.5-flash}") String model
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getProperty("GEMINI_API_KEY");
        }
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

        if (mimeType.contains(";")) {
            mimeType = mimeType.split(";")[0].trim();
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
        return analizarAudio(audio, null);
    }

    public JsonNode analizarAudio(MultipartFile audio, String contexto) throws IOException {

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

        if (mimeType.contains(";")) {
            mimeType = mimeType.split(";")[0].trim();
        }

        if (!esAudioSoportado(mimeType)) {
            throw new IllegalArgumentException(
                    "Formato de audio no soportado. "
                            + "Usa MP3, WAV, M4A, AAC, OGG, FLAC u OPUS."
            );
        }

        validarApiKey();

        String prompt = construirPromptAudio(contexto);

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

            String[] modelosCandidatos = {
                    (model != null && !model.isBlank()) ? model : "gemini-3.5-flash",
                    "gemini-3.5-flash-lite",
                    "gemini-3.8-flash"
            };

            Exception ultimoError = null;
            for (String modeloIntento : modelosCandidatos) {
                try {
                    GenerateContentResponse response =
                            client.models.generateContent(
                                    modeloIntento,
                                    content,
                                    config
                            );

                    String texto = response.text();

                    if (texto != null && !texto.isBlank()) {
                        return objectMapper.readTree(
                                limpiarRespuesta(texto)
                        );
                    }
                } catch (Exception e) {
                    ultimoError = e;
                    System.err.println("CODEFORGE IA (IMAGEN/AUDIO): Falló intento con " + modeloIntento + " (" + e.getMessage() + "). Probando modelo de respaldo...");
                }
            }

            throw new IllegalStateException(
                    "No se pudo procesar la solicitud con Gemini: "
                            + (ultimoError != null ? ultimoError.getMessage() : "sin respuesta"),
                    ultimoError
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

                Identifica minuciosamente todas las clases que aparecen,
                sus atributos (con visibilidad +, -, # y tipo si existe),
                sus operaciones/métodos, y TODAS las relaciones visibles.

                Debes devolver únicamente JSON válido.
                NO escribas explicaciones.
                NO uses Markdown.
                NO uses bloques ```json.

                Usa exactamente esta estructura:

                {
                  "versionUML": "2.5",
                  "elementos": [
                    {
                      "id": "elem-1",
                      "tipo": "CLASE",
                      "nombre": "NombreClase",
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
                          "id": "atr-1",
                          "nombre": "id",
                          "tipo": "Long",
                          "visibilidad": "PUBLIC",
                          "multiplicidad": "1",
                          "derivado": false,
                          "estatico": false,
                          "valorInicial": null
                        }
                      ],
                      "operaciones": [
                        {
                          "id": "op-1",
                          "nombre": "metodoEjemplo",
                          "visibilidad": "PUBLIC",
                          "estatica": false,
                          "abstracta": false,
                          "tipoRetorno": "void",
                          "parametros": []
                        }
                      ]
                    }
                  ],
                  "relaciones": [
                    {
                      "id": "rel-1",
                      "tipo": "ASOCIACION",
                      "origen": "elem-1",
                      "destino": "elem-2",
                      "rolOrigen": null,
                      "rolDestino": null,
                      "multiplicidadOrigen": "1",
                      "multiplicidadDestino": "1"
                    }
                  ]
                }

                REGLAS IMPORTANTES:

                1. Identifica todos los elementos UML que aparezcan en la imagen (CLASE, INTERFACE o ENUM).
                   Si una clase tiene estereotipo como <<interface>> o <<enumeration>> asigna tipo INTERFACE o ENUM.
                   Si una clase está ligada como clase de asociación intermedia, asigna tipo CLASE y estereotipo "association".

                2. Identifica atributos visibles:
                   - Si empieza con +, visibilidad = "PUBLIC"
                   - Si empieza con -, visibilidad = "PRIVATE"
                   - Si empieza con #, visibilidad = "PROTECTED"
                   - Si empieza con ~, visibilidad = "PACKAGE"
                   - Si no tiene tipo especificado, infiere el tipo adecuado (String, Long, Integer, Double, Boolean, LocalDate, LocalDateTime) o usa String.

                3. Identifica operaciones visibles con sus tipos de retorno y parámetros si están presentes.

                4. IDENTIFICACIÓN PRECISA DE TODAS LAS RELACIONES UML:
                   Los tipos de relación permitidos son:
                   - GENERALIZACION: Herencia. Línea continua con flecha de triángulo hueco/blanco sólido en el extremo que apunta a la clase padre o superclase.
                   - REALIZACION: Implementación. Línea discontinua o punteada con flecha de triángulo hueco apuntando a la interface.
                   - AGREGACION: Línea con rombo hueco/blanco (vacío) en el extremo de la clase contenedora/todo.
                   - COMPOSICION: Línea con rombo relleno/sólido negro o coloreado en el extremo de la clase contenedora/todo.
                   - DEPENDENCIA: Línea punteada o discontinua con flecha abierta simple (-->).
                   - ASOCIACION: Línea sólida continua simple o con flecha abierta ordinaria entre dos clases distintas.
                   - CLASE_ASOCIACION: Una clase asociada conectada mediante una línea discontinua/punteada a una línea de relación entre otras dos clases.
                     * Crea la clase intermedia en "elementos" (con tipo "CLASE", su nombre y estereotipo "association").
                     * Crea la relación en "relaciones" con tipo "CLASE_ASOCIACION", origen: ID de la primera clase, destino: ID de la segunda clase, y "rolDestino": nombre de la clase intermedia.
                   - RECURSIVA: Relación recursiva, reflexiva o auto-asociación.
                     * Es una línea que SALE de una clase y VUELVE A ENTRAR en la misma clase formando un bucle, lazo o rectángulo (por ejemplo: un Comentario que tiene subcomentarios consigo mismo, un Empleado con jefe Empleado, una Categoría con subcategorías, etc.).
                     * En este caso, "tipo": "RECURSIVA", "origen": ID de la clase, "destino": el MISMO ID de la clase (origen == destino).
                     * Registra multiplicidades en ambos extremos (ej: "multiplicidadOrigen": "1", "multiplicidadDestino": "0..*") y los roles en cada extremo si están presentes (ej: rolOrigen: "es", rolDestino: "subcomentario" o "tiene").

                5. Identifica multiplicidades en los extremos cuando aparezcan (1, 0..1, *, 0..*, 1..*).

                6. Ubica los elementos en el lienzo respetando su disposición espacial relativa en la imagen (posicion x, y).

                7. Cada elemento, atributo, operación y relación debe tener un ID único (ej: elem-1, atr-1, rel-1).

                8. Utiliza versionUML = 2.5.

                9. Devuelve ÚNICAMENTE el JSON solicitado sin ningún comentario ni texto antes o después.
                """;
    }

    // =========================================================
    // PROMPT PARA AUDIO
    // =========================================================

    private String construirPromptAudio() {
        return construirPromptAudio(null);
    }

    private String construirPromptAudio(String contexto) {

        String bloqueContexto = "";
        if (contexto != null && !contexto.isBlank()) {
            bloqueContexto = """
                    
                    ESTADO ACTUAL DEL MODELO UML (ELEMENTOS EN EL DIAGRAMA):
                    %s
                    
                    Usa esta lista para identificar exactamente qué clase, relación, atributo o método se solicita eliminar o modificar.
                    """.formatted(contexto);
        }

        return """
                Analiza cuidadosamente el audio que te estoy enviando.

                El audio contiene una descripción hablada con instrucciones
                sobre un diseño conceptual, sistema o diagrama de clases UML.

                La persona puede solicitar:

                1. CREAR O AGREGAR (palabras como "crear", "agrega", "añadir", "nuevo", "insertar"):
                   - Clases, interfaces o enumeraciones
                   - Atributos (con su tipo y multiplicidad)
                   - Métodos u operaciones (con parámetros y tipo de retorno)
                   - Relaciones entre clases (asociación, herencia/generalización, composición,
                     agregación, dependencia, clase de asociación)
                   - Multiplicidades

                2. ELIMINAR O BORRAR (palabras como "eliminar", "elimina", "borrar", "borra", "quitar", "quita", "sacar", "saca", "suprimir"):
                   - Clases existentes (ej: "elimina la clase Estudiante", "borra la clase X", "quita el elemento Y")
                   - Relaciones existentes (ej: "elimina la relación entre Cliente y Pedido", "borra la asociación entre A y B", "quita la herencia de A a B")
                   - Atributos existentes (ej: "elimina el atributo email de Usuario", "borra el campo telefono de Cliente", "quita edad de Alumno")
                   - Métodos u operaciones existentes (ej: "elimina el método calcularTotal de Factura", "borra la función pagar de Pago", "quita el método registrar")
                %s
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
                  ],
                  "eliminaciones": {
                    "clases": [],
                    "relaciones": [],
                    "atributos": [],
                    "operaciones": []
                  }
                }

                REGLAS:

                1. CREACIÓN:
                   - Si la persona describe clases nuevas, agrégalas a "elementos".
                   - Si la persona describe atributos nuevos para una clase, agrégalos a sus "atributos".
                   - Si la persona describe métodos u operaciones nuevas, agrégalos a sus "operaciones".
                   - Si la persona describe relaciones nuevas, agrégalas a "relaciones".
                   - CRÍTICO: Si la persona SOLO pide eliminar cosas y NO pide crear nada nuevo, "elementos" DEBE ser [] y "relaciones" DEBE ser []. NUNCA inventes elementos vacíos ni dejes el ejemplo en la respuesta.

                2. ELIMINACIÓN (IDENTIFICACIÓN EXACTA):
                   - Si la persona pide eliminar una clase (ej: "elimina la clase Estudiante", "borra Estudiante"):
                     "eliminaciones": { "clases": ["Estudiante"], ... }
                   - Si la persona pide eliminar una relación (ej: "elimina la relación entre Cliente y Factura"):
                     "eliminaciones": { "relaciones": [{"origen": "Cliente", "destino": "Factura"}], ... }
                   - Si la persona pide eliminar un atributo (ej: "elimina el atributo email de Usuario"):
                     "eliminaciones": { "atributos": [{"clase": "Usuario", "nombre": "email"}], ... }
                     (Si no especifica la clase, busca en el contexto a qué clase pertenece ese atributo y pon su nombre en "clase").
                   - Si la persona pide eliminar una operación o método (ej: "elimina el método calcularTotal de Factura"):
                     "eliminaciones": { "operaciones": [{"clase": "Factura", "nombre": "calcularTotal"}], ... }
                     (Si no especifica la clase, busca en el contexto a qué clase pertenece ese método y pon su nombre en "clase").
                   - Si no se solicita eliminar alguna de estas categorías, deja sus respectivas listas vacías ([]).
                   - NUNCA omitas la clave "eliminaciones" ni pongas null; siempre debe tener las cuatro listas: "clases", "relaciones", "atributos" y "operaciones".

                3. OPERACIONES COMBINADAS:
                   Si la persona pide al mismo tiempo crear y eliminar cosas en el mismo audio (ej: "elimina la clase Persona y crea la clase Cliente"),
                   procesa ambas: las creaciones en "elementos" / "relaciones", y las eliminaciones en "eliminaciones".

                4. Tipos de relación permitidos:
                   ASOCIACION
                   GENERALIZACION
                   REALIZACION
                   AGREGACION
                   COMPOSICION
                   DEPENDENCIA
                   CLASE_ASOCIACION
                   RECURSIVA

                5. Para clases de asociación nuevas:
                   - Crea la clase intermedia en "elementos" (con tipo "CLASE", su nombre y estereotipo "association").
                   - Crea la relación en "relaciones" con tipo "CLASE_ASOCIACION", origen: ID de la primera clase, destino: ID de la segunda clase, y "rolDestino": nombre de la clase de asociación.

                6. Para relaciones recursivas (auto-asociación, reflexivas):
                   - Si la persona describe que una clase se relaciona consigo misma (ej: "un Comentario tiene subcomentarios que son otros comentarios", "un Empleado tiene un jefe que es otro Empleado"):
                     Crea la relación en "relaciones" con tipo "RECURSIVA", origen: ID de la clase, destino: el MISMO ID de la clase, con sus respectivas multiplicidades y roles.

                6. Si no se especifica el tipo de un atributo nuevo, usa String.
                7. Si no se especifica el tipo de retorno de un método nuevo, usa void.

                8. Cada elemento nuevo en "elementos", así como sus atributos y operaciones,
                   debe tener un ID único (ej: "elem-1", "atr-1", "op-1").

                9. Ubica las clases en posiciones legibles sin superponerse.

                10. Utiliza versionUML = 2.5.

                11. Devuelve ÚNICAMENTE el JSON solicitado sin ningún comentario ni texto adicional.
                """.formatted(bloqueContexto);
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