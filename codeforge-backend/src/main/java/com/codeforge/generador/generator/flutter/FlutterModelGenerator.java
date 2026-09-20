package com.codeforge.generador.generator.flutter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class FlutterModelGenerator {

    public void generar(
            Path flutterRoot,
            List<Map<String, Object>> elementos
    ) throws IOException {

        Path modelsDir = flutterRoot
                .resolve("lib")
                .resolve("generated")
                .resolve("models");

        Files.createDirectories(modelsDir);

        if (elementos == null) {
            return;
        }

        int generados = 0;

        for (Map<String, Object> elemento : elementos) {

            if (elemento == null) {
                continue;
            }

            String tipo = convertirString(elemento.get("tipo"));
            String nombre = convertirString(elemento.get("nombre"));

            if (nombre == null || nombre.isBlank()) {
                continue;
            }

            // Solo las clases generan modelos Dart
            if (tipo == null || !"CLASE".equalsIgnoreCase(tipo)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> atributos =
                    (List<Map<String, Object>>) elemento.get("atributos");

            String contenido = construirModelo(nombre, atributos);

            String nombreArchivo = convertirNombreArchivo(nombre);

            Path archivo = modelsDir.resolve(nombreArchivo + ".dart");

            Files.writeString(archivo, contenido);

            System.out.println(
                    "FLUTTER MODEL: creado: " + archivo
            );

            generados++;
        }

        System.out.println(
                "FLUTTER MODEL: modelos generados = " + generados
        );
    }

    private String construirModelo(
            String nombreClase,
            List<Map<String, Object>> atributos
    ) {

        StringBuilder sb = new StringBuilder();

        sb.append("class ")
                .append(nombreClase)
                .append(" {\n");

        if (atributos == null || atributos.isEmpty()) {
            sb.append("  final int? id;\n\n");
            sb.append("  ").append(nombreClase).append("({this.id});\n\n");
            sb.append("  factory ").append(nombreClase).append(".fromJson(Map<String, dynamic> json) {\n");
            sb.append("    return ").append(nombreClase).append("(\n");
            sb.append("      id: json['id'] as int?,\n");
            sb.append("    );\n");
            sb.append("  }\n\n");
            sb.append("  Map<String, dynamic> toJson() {\n");
            sb.append("    return {\n");
            sb.append("      'id': id,\n");
            sb.append("    };\n");
            sb.append("  }\n");
            sb.append("}\n");
            return sb.toString();
        }

        for (Map<String, Object> atributo : atributos) {

            if (atributo == null) {
                continue;
            }

            String nombreAtributo =
                    convertirString(atributo.get("nombre"));

            String tipoUml =
                    convertirString(atributo.get("tipo"));

            if (nombreAtributo == null || nombreAtributo.isBlank()) {
                continue;
            }

            String tipoDart =
                    mapearTipoDart(tipoUml);

            sb.append("  final ")
                    .append(tipoDart)
                    .append("? ")
                    .append(convertirNombreDart(nombreAtributo))
                    .append(";\n");
        }

        sb.append("\n");

        // Constructor
        sb.append("  ")
                .append(nombreClase)
                .append("({\n");

        for (Map<String, Object> atributo : atributos) {

            if (atributo == null) {
                continue;
            }

            String nombreAtributo =
                    convertirString(atributo.get("nombre"));

            if (nombreAtributo == null || nombreAtributo.isBlank()) {
                continue;
            }

            sb.append("    this.")
                    .append(convertirNombreDart(nombreAtributo))
                    .append(",\n");
        }

        sb.append("  });\n\n");

        // fromJson
        sb.append("  factory ")
                .append(nombreClase)
                .append(".fromJson(Map<String, dynamic> json) {\n");

        sb.append("    return ")
                .append(nombreClase)
                .append("(\n");

        for (Map<String, Object> atributo : atributos) {

            if (atributo == null) {
                continue;
            }

            String nombreAtributo =
                    convertirString(atributo.get("nombre"));

            if (nombreAtributo == null || nombreAtributo.isBlank()) {
                continue;
            }

            String nombreDart =
                    convertirNombreDart(nombreAtributo);

            String tipoUml =
                    convertirString(atributo.get("tipo"));

            String tipoDart =
                    mapearTipoDart(tipoUml);

            sb.append("      ")
                    .append(nombreDart)
                    .append(": ")
                    .append(convertirDesdeJson(
                            nombreDart,
                            tipoDart
                    ))
                    .append(",\n");
        }

        sb.append("    );\n");
        sb.append("  }\n\n");

        // toJson
        sb.append("  Map<String, dynamic> toJson() {\n");
        sb.append("    return {\n");

        for (Map<String, Object> atributo : atributos) {

            if (atributo == null) {
                continue;
            }

            String nombreAtributo =
                    convertirString(atributo.get("nombre"));

            if (nombreAtributo == null || nombreAtributo.isBlank()) {
                continue;
            }

            String nombreDart =
                    convertirNombreDart(nombreAtributo);

            sb.append("      '")
                    .append(nombreAtributo)
                    .append("': ")
                    .append(nombreDart)
                    .append(",\n");
        }

        sb.append("    };\n");
        sb.append("  }\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String mapearTipoDart(String tipoUml) {

        if (tipoUml == null || tipoUml.isBlank()) {
            return "dynamic";
        }

        String tipo = tipoUml.trim().toLowerCase();

        return switch (tipo) {

            case "int",
                 "integer",
                 "long",
                 "short",
                 "bigint" ->
                    "int";

            case "double",
                 "decimal",
                 "float",
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
                    convertirNombreTipoDart(tipoUml);
        };
    }

    private String convertirDesdeJson(
            String nombre,
            String tipoDart
    ) {

        return switch (tipoDart) {

            case "int" ->
                    "json['" + nombre + "'] as int?";

            case "double" ->
                    "(json['" + nombre + "'] as num?)?.toDouble()";

            case "bool" ->
                    "json['" + nombre + "'] as bool?";

            case "String" ->
                    "json['" + nombre + "'] as String?";

            case "DateTime" ->
                    "json['" + nombre + "'] != null "
                            + "? DateTime.tryParse(json['" + nombre + "'].toString()) "
                            + ": null";

            default ->
                    "json['" + nombre + "']";
        };
    }

    private String convertirNombreDart(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return "campo";
        }

        String limpio = nombre.trim()
                .replaceAll("[^a-zA-Z0-9_]", "_");

        if (limpio.isEmpty()) {
            return "campo";
        }

        char primera = limpio.charAt(0);

        if (Character.isDigit(primera)) {
            limpio = "_" + limpio;
        }

        return Character.toLowerCase(limpio.charAt(0))
                + limpio.substring(1);
    }

    private String convertirNombreArchivo(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return "modelo";
        }

        String limpio = nombre.trim()
                .replaceAll("[^a-zA-Z0-9]", "_");

        limpio = limpio.replaceAll("([a-z0-9])([A-Z])", "$1_$2");

        return limpio.toLowerCase();
    }

    private String convertirNombreTipoDart(String tipo) {

        if (tipo == null || tipo.isBlank()) {
            return "dynamic";
        }

        String limpio = tipo.trim()
                .replaceAll("[^a-zA-Z0-9]", "");

        if (limpio.isEmpty()) {
            return "dynamic";
        }

        return Character.toUpperCase(limpio.charAt(0))
                + limpio.substring(1);
    }

    private String convertirString(Object valor) {

        if (valor == null) {
            return null;
        }

        return String.valueOf(valor);
    }
}