package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PostmanGenerator {

    public Path generar(
            Path proyecto,
            Map<String, Object> contenido
    ) {

        try {

            Path carpetaPostman =
                    proyecto.resolve(
                            "postman"
                    );

            Files.createDirectories(
                    carpetaPostman
            );

            List<Map<String, Object>> elementos =
                    convertirListaMapas(
                            contenido.get("elementos")
                    );

            StringBuilder json =
                    new StringBuilder();

            json.append("{\n");

            /*
             * ============================================================
             * INFO
             * ============================================================
             */

            json.append("  \"info\": {\n");

            json.append(
                    "    \"name\": \"CodeForge Generated API\",\n"
            );

            json.append(
                    "    \"description\": \"Colección generada automáticamente por CodeForge.\",\n"
            );

            json.append(
                    "    \"schema\": \"https://schema.getpostman.com/json/collection/v2.1.0/collection.json\"\n"
            );

            json.append("  },\n");

            /*
             * ============================================================
             * VARIABLE BASE URL
             * ============================================================
             */

            json.append("  \"variable\": [\n");

            json.append("    {\n");

            json.append(
                    "      \"key\": \"baseUrl\",\n"
            );

            json.append(
                    "      \"value\": \"http://localhost:8081\"\n"
            );

            json.append("    }\n");

            json.append("  ],\n");

            /*
             * ============================================================
             * CLASES UML
             * ============================================================
             */

            json.append("  \"item\": [\n");

            List<Map<String, Object>> clases =
                    new ArrayList<>();

            for (
                    Map<String, Object> elemento :
                    elementos
            ) {

                String tipo =
                        texto(
                                elemento.get("tipo")
                        );

                if (
                        !"CLASE".equalsIgnoreCase(tipo)
                ) {
                    continue;
                }

                clases.add(
                        elemento
                );
            }

            for (
                    int i = 0;
                    i < clases.size();
                    i++
            ) {

                Map<String, Object> clase =
                        clases.get(i);

                String nombreClase =
                        convertirNombreClase(
                                texto(
                                        clase.get("nombre")
                                )
                        );

                if (
                        nombreClase == null ||
                        nombreClase.isBlank()
                ) {
                    continue;
                }

                String recurso =
                        convertirNombreRecurso(
                                nombreClase
                        );

                String body =
                        construirBodyDesdeAtributos(
                                clase.get("atributos")
                        );

                json.append(
                        construirCarpetaCrud(
                                nombreClase,
                                recurso,
                                body
                        )
                );

                if (
                        i < clases.size() - 1
                ) {
                    json.append(",\n");
                }
            }

            json.append("\n  ]\n");

            json.append("}\n");

            /*
             * ============================================================
             * GUARDAR ARCHIVO
             * ============================================================
             */

            Path archivo =
                    carpetaPostman.resolve(
                            "CodeForge-API.postman_collection.json"
                    );

            Files.writeString(
                    archivo,
                    json.toString(),
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "GENERADOR: Postman Collection creada: "
                            + archivo
            );

            return archivo;

        } catch (
                IOException e
        ) {

            throw new RuntimeException(
                    "No se pudo generar la colección de Postman.",
                    e
            );
        }
    }

    /*
     * ================================================================
     * CARPETA CRUD
     * ================================================================
     */

    private String construirCarpetaCrud(
            String nombreClase,
            String recurso,
            String body
    ) {

        StringBuilder json =
                new StringBuilder();

        json.append("    {\n");

        json.append(
                "      \"name\": \""
                        + escaparJson(nombreClase)
                        + "\",\n"
        );

        json.append("      \"item\": [\n");

        /*
         * POST
         */

        json.append(
                construirRequest(
                        "Crear " + nombreClase,
                        "POST",
                        "{{baseUrl}}/api/" + recurso,
                        body
                )
        );

        json.append(",\n");

        /*
         * GET
         */

        json.append(
                construirRequest(
                        "Listar " + nombreClase,
                        "GET",
                        "{{baseUrl}}/api/" + recurso,
                        null
                )
        );

        json.append(",\n");

        /*
         * GET POR ID
         */

        json.append(
                construirRequest(
                        "Obtener " + nombreClase + " por ID",
                        "GET",
                        "{{baseUrl}}/api/" + recurso + "/1",
                        null
                )
        );

        json.append(",\n");

        /*
         * PUT
         */

        json.append(
                construirRequest(
                        "Actualizar " + nombreClase,
                        "PUT",
                        "{{baseUrl}}/api/" + recurso + "/1",
                        body
                )
        );

        json.append(",\n");

        /*
         * DELETE
         */

        json.append(
                construirRequest(
                        "Eliminar " + nombreClase,
                        "DELETE",
                        "{{baseUrl}}/api/" + recurso + "/1",
                        null
                )
        );

        json.append("\n");

        json.append("      ]\n");

        json.append("    }");

        return json.toString();
    }

    /*
     * ================================================================
     * REQUEST
     * ================================================================
     */

    private String construirRequest(
            String nombre,
            String metodo,
            String url,
            String body
    ) {

        StringBuilder json =
                new StringBuilder();

        json.append("        {\n");

        json.append(
                "          \"name\": \""
                        + escaparJson(nombre)
                        + "\",\n"
        );

        json.append(
                "          \"request\": {\n"
        );

        json.append(
                "            \"method\": \""
                        + metodo
                        + "\",\n"
        );

        /*
         * HEADERS
         */

        json.append(
                "            \"header\": [\n"
        );

        json.append(
                "              {\n"
        );

        json.append(
                "                \"key\": \"Content-Type\",\n"
        );

        json.append(
                "                \"value\": \"application/json\"\n"
        );

        json.append(
                "              }\n"
        );

        json.append(
                "            ],\n"
        );

        /*
         * URL
         */

        json.append(
                "            \"url\": \""
                        + escaparJson(url)
                        + "\""
        );

        /*
         * BODY
         */

        if (body != null) {

            json.append(",\n");

            json.append(
                    "            \"body\": {\n"
            );

            json.append(
                    "              \"mode\": \"raw\",\n"
            );

            json.append(
                    "              \"raw\": "
                            + convertirTextoJson(body)
                            + ",\n"
            );

            json.append(
                    "              \"options\": {\n"
            );

            json.append(
                    "                \"raw\": {\n"
            );

            json.append(
                    "                  \"language\": \"json\"\n"
            );

            json.append(
                    "                }\n"
            );

            json.append(
                    "              }\n"
            );

            json.append(
                    "            }\n"
            );

        } else {

            json.append("\n");
        }

        json.append(
                "          }\n"
        );

        json.append(
                "        }"
        );

        return json.toString();
    }

    /*
     * ================================================================
     * GENERAR BODY SEGÚN ATRIBUTOS UML
     * ================================================================
     */

    private String construirBodyDesdeAtributos(
            Object atributosObjeto
    ) {

        List<Map<String, Object>> atributos =
                convertirListaMapas(
                        atributosObjeto
                );

        StringBuilder json =
                new StringBuilder();

        json.append("{\n");

        for (
                int i = 0;
                i < atributos.size();
                i++
        ) {

            Map<String, Object> atributo =
                    atributos.get(i);

            String nombre =
                    texto(
                            atributo.get("nombre")
                    );

            String tipo =
                    texto(
                            atributo.get("tipo")
                    );

            if (
                    nombre.isBlank()
            ) {
                continue;
            }

            json.append(
                    "  \""
                            + escaparJson(nombre)
                            + "\": "
            );

            json.append(
                    generarValorEjemplo(tipo)
            );

            if (
                    i < atributos.size() - 1
            ) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("}");

        return json.toString();
    }

    /*
     * ================================================================
     * VALORES DE EJEMPLO SEGÚN TIPO UML
     * ================================================================
     */

    private String generarValorEjemplo(
            String tipo
    ) {

        if (tipo == null || tipo.isBlank()) {
            return "\"\"";
        }

        return switch (
                tipo.trim().toLowerCase()
        ) {

            case "string" ->
                    "\"ejemplo\"";

            case "integer",
                 "int" ->
                    "1";

            case "long" ->
                    "1";

            case "double" ->
                    "1.0";

            case "float" ->
                    "1.0";

            case "boolean",
                 "bool" ->
                    "true";

            case "date",
                 "localdate" ->
                    "\"2026-01-01\"";

            case "datetime",
                 "localdatetime" ->
                    "\"2026-01-01T10:00:00\"";

            case "time",
                 "localtime" ->
                    "\"10:00:00\"";

            default ->
                    "\"ejemplo\"";
        };
    }

    /*
     * ================================================================
     * LISTA DE MAPAS
     * ================================================================
     */

    private List<Map<String, Object>> convertirListaMapas(
            Object valor
    ) {

        List<Map<String, Object>> resultado =
                new ArrayList<>();

        if (
                !(valor instanceof List<?> lista)
        ) {
            return resultado;
        }

        for (Object item : lista) {

            if (
                    item instanceof Map<?, ?> mapa
            ) {

                @SuppressWarnings("unchecked")
                Map<String, Object> convertido =
                        (Map<String, Object>) mapa;

                resultado.add(
                        convertido
                );
            }
        }

        return resultado;
    }

    /*
     * ================================================================
     * TEXTO
     * ================================================================
     */

    private String texto(
            Object valor
    ) {

        if (valor == null) {
            return "";
        }

        return String.valueOf(
                valor
        ).trim();
    }

    /*
     * ================================================================
     * NOMBRE DE CLASE
     * ================================================================
     */

    private String convertirNombreClase(
            String nombre
    ) {

        if (
                nombre == null ||
                nombre.isBlank()
        ) {
            return null;
        }

        String[] partes =
                nombre
                        .trim()
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

        return resultado.toString();
    }

    /*
     * ================================================================
     * NOMBRE DEL RECURSO REST
     * ================================================================
     */

    private String convertirNombreRecurso(
            String nombreClase
    ) {

        if (
                nombreClase == null ||
                nombreClase.isBlank()
        ) {
            return "recurso";
        }

        return nombreClase
                .replaceAll(
                        "([a-z])([A-Z])",
                        "$1-$2"
                )
                .toLowerCase();
    }

    /*
     * ================================================================
     * ESCAPAR JSON
     * ================================================================
     */

    private String escaparJson(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    /*
     * ================================================================
     * TEXTO MULTILÍNEA → STRING JSON
     * ================================================================
     */

    private String convertirTextoJson(
            String texto
    ) {

        return "\""
                + escaparJson(texto)
                + "\"";
    }
}