package com.codeforge.generador.generator.flutter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class FlutterExecutorGenerator {

    public void generar(
            Path flutterRoot,
            List<Map<String, Object>> elementos
    ) throws IOException {

        Path aiDir = flutterRoot
                .resolve("lib")
                .resolve("generated")
                .resolve("ai");

        Files.createDirectories(aiDir);

        String contenido = construirExecutor(elementos);

        Path archivo = aiDir.resolve("ai_tool_executor.dart");

        Files.writeString(archivo, contenido);

        System.out.println(
                "FLUTTER AI: executor creado: " + archivo
        );
    }

    private String construirExecutor(
            List<Map<String, Object>> elementos
    ) {

        StringBuilder sb = new StringBuilder();

        agregarImports(sb, elementos);

        sb.append("\n");

        sb.append(
                "class AiToolExecutor {\n"
        );

        sb.append(
                "  static Future<dynamic> ejecutar(\n"
        );

        sb.append(
                "    String toolName,\n"
        );

        sb.append(
                "    Map<String, dynamic> arguments,\n"
        );

        sb.append(
                "  ) async {\n"
        );

        sb.append(
                "    switch (toolName) {\n"
        );

        if (elementos != null) {

            for (Map<String, Object> elemento : elementos) {

                if (elemento == null) {
                    continue;
                }

                String tipo =
                        convertirString(elemento.get("tipo"));

                String nombre =
                        convertirString(elemento.get("nombre"));

                if (nombre == null || nombre.isBlank()) {
                    continue;
                }

                if (tipo == null ||
                        !"CLASE".equalsIgnoreCase(tipo)) {
                    continue;
                }

                String nombreFlutter =
                        convertirNombreFlutter(nombre);

                /*
                 * CREAR
                 */
                sb.append(
                        "      case '"
                )
                        .append(nombreFlutter)
                        .append("_crear':\n");

                sb.append(
                        "      case 'crear_"
                )
                        .append(nombreFlutter)
                        .append("':\n");

                sb.append(
                        "        return "
                )
                        .append(nombre)
                        .append("Api.crear(\n");

                sb.append(
                        "          "
                )
                        .append(construirModelo(
                                nombre,
                                elemento
                        ))
                        .append(",\n");

                sb.append(
                        "        );\n"
                );

                /*
                 * LISTAR
                 */
                sb.append(
                        "      case '"
                )
                        .append(nombreFlutter)
                        .append("_listar':\n");

                sb.append(
                        "      case 'listar_"
                )
                        .append(nombreFlutter)
                        .append("':\n");

                sb.append(
                        "        return "
                )
                        .append(nombre)
                        .append("Api.listar();\n"
                );

                /*
                 * OBTENER
                 */
                sb.append(
                        "      case '"
                )
                        .append(nombreFlutter)
                        .append("_obtener':\n");

                sb.append(
                        "      case 'obtener_"
                )
                        .append(nombreFlutter)
                        .append("':\n");

                sb.append(
                        "        return "
                )
                        .append(nombre)
                        .append("Api.obtenerPorId(\n");

                sb.append(
                        "          arguments['id'],\n"
                );

                sb.append(
                        "        );\n"
                );

                /*
                 * ACTUALIZAR
                 */
                sb.append(
                        "      case '"
                )
                        .append(nombreFlutter)
                        .append("_actualizar':\n");

                sb.append(
                        "      case 'actualizar_"
                )
                        .append(nombreFlutter)
                        .append("':\n");

                sb.append(
                        "        return "
                )
                        .append(nombre)
                        .append("Api.actualizar(\n");

                sb.append(
                        "          arguments['id'],\n"
                );

                sb.append(
                        "          "
                )
                        .append(construirModelo(
                                nombre,
                                elemento
                        ))
                        .append(",\n");

                sb.append(
                        "        );\n"
                );

                /*
                 * ELIMINAR
                 */
                sb.append(
                        "      case '"
                )
                        .append(nombreFlutter)
                        .append("_eliminar':\n");

                sb.append(
                        "      case 'eliminar_"
                )
                        .append(nombreFlutter)
                        .append("':\n");

                sb.append(
                        "        return "
                )
                        .append(nombre)
                        .append("Api.eliminar(\n");

                sb.append(
                        "          arguments['id'],\n"
                );

                sb.append(
                        "        );\n"
                );
            }
        }

        sb.append(
                "      default:\n"
        );

        sb.append(
                "        throw Exception(\n"
        );

        sb.append(
        "          'Herramienta de IA no soportada: $toolName',\n"
);

        sb.append(
                "        );\n"
        );

        sb.append(
                "    }\n"
        );

        sb.append(
                "  }\n"
        );

        sb.append(
                "}\n"
        );

        return sb.toString();
    }

    private void agregarImports(
            StringBuilder sb,
            List<Map<String, Object>> elementos
    ) {

        if (elementos == null) {
            return;
        }

        for (Map<String, Object> elemento : elementos) {

            if (elemento == null) {
                continue;
            }

            String tipo =
                    convertirString(elemento.get("tipo"));

            String nombre =
                    convertirString(elemento.get("nombre"));

            if (nombre == null || nombre.isBlank()) {
                continue;
            }

            if (tipo == null ||
                    !"CLASE".equalsIgnoreCase(tipo)) {
                continue;
            }

            String nombreArchivo =
                    convertirNombreArchivo(nombre);

            sb.append("import '../models/")
                    .append(nombreArchivo)
                    .append(".dart';\n");

            sb.append("import '../api/")
                    .append(nombreArchivo)
                    .append("_api.dart';\n");
        }
    }

    private String construirModelo(
            String nombreClase,
            Map<String, Object> elemento
    ) {

        StringBuilder sb = new StringBuilder();

        sb.append("new ")
                .append(nombreClase)
                .append("(\n");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> atributos =
                (List<Map<String, Object>>) elemento.get(
                        "atributos"
                );

        if (atributos != null) {

            for (Map<String, Object> atributo : atributos) {

                if (atributo == null) {
                    continue;
                }

                String nombre =
                        convertirString(
                                atributo.get("nombre")
                        );

                String tipo =
                        convertirString(
                                atributo.get("tipo")
                        );

                if (nombre == null || nombre.isBlank()) {
                    continue;
                }

                String nombreDart =
                        convertirNombreDart(nombre);

                sb.append("            ")
                        .append(nombreDart)
                        .append(": ")
                        .append(
                                construirValorArgumento(
                                        nombre,
                                        tipo
                                )
                        )
                        .append(",\n");
            }
        }

        sb.append("          )");

        return sb.toString();
    }

    private String construirValorArgumento(
            String nombre,
            String tipo
    ) {

        String tipoDart = mapearTipoDart(tipo);

        String argumento =
                "arguments['"
                        + escaparDart(nombre)
                        + "']";

        return switch (tipoDart) {

            case "int" ->
                    "("
                            + argumento
                            + " as num?)?.toInt()";

            case "double" ->
                    "("
                            + argumento
                            + " as num?)?.toDouble()";

            case "bool" ->
                    argumento
                            + " as bool?";

            case "String" ->
                    argumento
                            + "?.toString()";

            case "DateTime" ->
                    argumento
                            + " != null ? DateTime.tryParse("
                            + argumento
                            + ".toString()) : null";

            default ->
                    argumento;
        };
    }

    private String mapearTipoDart(
            String tipoUml
    ) {

        if (tipoUml == null ||
                tipoUml.isBlank()) {
            return "dynamic";
        }

        String tipo =
                tipoUml.trim().toLowerCase();

        return switch (tipo) {

            case "int",
                 "integer",
                 "long",
                 "short",
                 "bigint" ->
                    "int";

            case "double",
                 "float",
                 "decimal",
                 "number" ->
                    "double";

            case "bool",
                 "boolean" ->
                    "bool";

            case "string",
                 "char",
                 "varchar",
                 "text" ->
                    "String";

            case "date",
                 "localdate",
                 "datetime",
                 "localdatetime",
                 "timestamp" ->
                    "DateTime";

            default ->
                    convertirNombreTipoDart(
                            tipoUml
                    );
        };
    }

    private String convertirNombreDart(
            String nombre
    ) {

        if (nombre == null ||
                nombre.isBlank()) {
            return "campo";
        }

        String limpio = nombre.trim()
                .replaceAll(
                        "[^a-zA-Z0-9_]",
                        "_"
                );

        if (limpio.isEmpty()) {
            return "campo";
        }

        if (Character.isDigit(
                limpio.charAt(0)
        )) {
            limpio = "_" + limpio;
        }

        return Character.toLowerCase(
                limpio.charAt(0)
        ) + limpio.substring(1);
    }

    private String convertirNombreArchivo(
            String nombre
    ) {

        if (nombre == null ||
                nombre.isBlank()) {
            return "modelo";
        }

        String limpio = nombre.trim()
                .replaceAll(
                        "[^a-zA-Z0-9]",
                        "_"
                );

        limpio = limpio.replaceAll(
                "([a-z0-9])([A-Z])",
                "$1_$2"
        );

        return limpio.toLowerCase();
    }

    private String convertirNombreFlutter(
            String nombre
    ) {

        return convertirNombreArchivo(nombre);
    }

    private String convertirNombreTipoDart(
            String tipo
    ) {

        if (tipo == null ||
                tipo.isBlank()) {
            return "dynamic";
        }

        String limpio = tipo.trim()
                .replaceAll(
                        "[^a-zA-Z0-9]",
                        ""
                );

        if (limpio.isEmpty()) {
            return "dynamic";
        }

        return Character.toUpperCase(
                limpio.charAt(0)
        ) + limpio.substring(1);
    }

    private String escaparDart(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("'", "\\'");
    }

    private String convertirString(
            Object valor
    ) {

        if (valor == null) {
            return null;
        }

        return String.valueOf(valor);
    }
}