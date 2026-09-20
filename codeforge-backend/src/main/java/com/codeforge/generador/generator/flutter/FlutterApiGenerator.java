package com.codeforge.generador.generator.flutter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class FlutterApiGenerator {

    public void generar(
            Path flutterRoot,
            List<Map<String, Object>> elementos
    ) throws IOException {

        Path apiDir = flutterRoot
                .resolve("lib")
                .resolve("generated")
                .resolve("api");

        Files.createDirectories(apiDir);

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

            // Solo las clases generan API CRUD
            if (tipo == null || !"CLASE".equalsIgnoreCase(tipo)) {
                continue;
            }

            String nombreArchivo = convertirNombreArchivo(nombre);
            String nombreClaseApi = nombre + "Api";
            String ruta = convertirNombreRuta(nombre);

            String contenido = construirApi(
                    nombre,
                    nombreClaseApi,
                    nombreArchivo,
                    ruta
            );

            Path archivo = apiDir.resolve(
                    nombreArchivo + "_api.dart"
            );

            Files.writeString(archivo, contenido);

            System.out.println(
                    "FLUTTER API: creado: " + archivo
            );

            generados++;
        }

        System.out.println(
                "FLUTTER API: APIs generadas = " + generados
        );
    }

    private String construirApi(
            String nombreModelo,
            String nombreClaseApi,
            String nombreArchivoModelo,
            String ruta
    ) {

        StringBuilder sb = new StringBuilder();

        sb.append("import 'package:http/http.dart' as http;\n");
        sb.append("import 'dart:convert';\n");
        sb.append("\n");
        sb.append("import '../config/api_config.dart';\n");
        sb.append("import '../models/")
                .append(nombreArchivoModelo)
                .append(".dart';\n");
        sb.append("\n");

        sb.append("class ")
                .append(nombreClaseApi)
                .append(" {\n");

        /*
         * CREAR
         */
        sb.append("  static Future<")
                .append(nombreModelo)
                .append("> crear(")
                .append(nombreModelo)
                .append(" dato) async {\n");

        sb.append("    final response = await http.post(\n");

        sb.append("      Uri.parse('${ApiConfig.baseUrl}/api/")
                .append(ruta)
                .append("'),\n");

        sb.append("      headers: {\n");
        sb.append("        'Content-Type': 'application/json',\n");
        sb.append("        'Accept': 'application/json',\n");
        sb.append("      },\n");

        sb.append("      body: jsonEncode(dato.toJson()),\n");

        sb.append("    );\n\n");

        sb.append("    if (response.statusCode >= 200 && ")
                .append("response.statusCode < 300) {\n");

        sb.append("      return ")
                .append(nombreModelo)
                .append(".fromJson(\n");

        sb.append("        jsonDecode(response.body) ")
                .append("as Map<String, dynamic>,\n");

        sb.append("      );\n");
        sb.append("    }\n\n");

        sb.append("    throw Exception(\n");
        sb.append("      'Error al crear ")
                .append(nombreModelo)
                .append(": ${response.statusCode} ${response.body}',\n");
        sb.append("    );\n");

        sb.append("  }\n\n");

        /*
         * LISTAR
         */
        sb.append("  static Future<List<")
                .append(nombreModelo)
                .append(">> listar() async {\n");

        sb.append("    final response = await http.get(\n");

        sb.append("      Uri.parse('${ApiConfig.baseUrl}/api/")
                .append(ruta)
                .append("'),\n");

        sb.append("      headers: {\n");
        sb.append("        'Accept': 'application/json',\n");
        sb.append("      },\n");

        sb.append("    );\n\n");

        sb.append("    if (response.statusCode >= 200 && ")
                .append("response.statusCode < 300) {\n");

        sb.append("      final List<dynamic> datos = ")
                .append("jsonDecode(response.body);\n\n");

        sb.append("      return datos\n");
        sb.append("          .map(\n");
        sb.append("            (item) => ")
                .append(nombreModelo)
                .append(".fromJson(\n");
        sb.append("              item as Map<String, dynamic>,\n");
        sb.append("            ),\n");
        sb.append("          )\n");
        sb.append("          .toList();\n");

        sb.append("    }\n\n");

        sb.append("    throw Exception(\n");
        sb.append("      'Error al listar ")
                .append(nombreModelo)
                .append(": ${response.statusCode} ${response.body}',\n");
        sb.append("    );\n");

        sb.append("  }\n\n");

        /*
         * OBTENER POR ID
         */
        sb.append("  static Future<")
                .append(nombreModelo)
                .append("> obtenerPorId(dynamic id) async {\n");

        sb.append("    final response = await http.get(\n");

        sb.append("      Uri.parse('${ApiConfig.baseUrl}/api/")
                .append(ruta)
                .append("/$id'),\n");

        sb.append("      headers: {\n");
        sb.append("        'Accept': 'application/json',\n");
        sb.append("      },\n");

        sb.append("    );\n\n");

        sb.append("    if (response.statusCode >= 200 && ")
                .append("response.statusCode < 300) {\n");

        sb.append("      return ")
                .append(nombreModelo)
                .append(".fromJson(\n");

        sb.append("        jsonDecode(response.body) ")
                .append("as Map<String, dynamic>,\n");

        sb.append("      );\n");
        sb.append("    }\n\n");

        sb.append("    throw Exception(\n");
        sb.append("      'Error al obtener ")
                .append(nombreModelo)
                .append(": ${response.statusCode} ${response.body}',\n");
        sb.append("    );\n");

        sb.append("  }\n\n");

        /*
         * ACTUALIZAR
         */
        sb.append("  static Future<")
                .append(nombreModelo)
                .append("> actualizar(\n");

        sb.append("    dynamic id,\n");
        sb.append("    ")
                .append(nombreModelo)
                .append(" dato,\n");

        sb.append("  ) async {\n");

        sb.append("    final response = await http.put(\n");

        sb.append("      Uri.parse('${ApiConfig.baseUrl}/api/")
                .append(ruta)
                .append("/$id'),\n");

        sb.append("      headers: {\n");
        sb.append("        'Content-Type': 'application/json',\n");
        sb.append("        'Accept': 'application/json',\n");
        sb.append("      },\n");

        sb.append("      body: jsonEncode(dato.toJson()),\n");

        sb.append("    );\n\n");

        sb.append("    if (response.statusCode >= 200 && ")
                .append("response.statusCode < 300) {\n");

        sb.append("      return ")
                .append(nombreModelo)
                .append(".fromJson(\n");

        sb.append("        jsonDecode(response.body) ")
                .append("as Map<String, dynamic>,\n");

        sb.append("      );\n");
        sb.append("    }\n\n");

        sb.append("    throw Exception(\n");
        sb.append("      'Error al actualizar ")
                .append(nombreModelo)
                .append(": ${response.statusCode} ${response.body}',\n");
        sb.append("    );\n");

        sb.append("  }\n\n");

        /*
         * ELIMINAR
         */
        sb.append("  static Future<void> eliminar(dynamic id) async {\n");

        sb.append("    final response = await http.delete(\n");

        sb.append("      Uri.parse('${ApiConfig.baseUrl}/api/")
                .append(ruta)
                .append("/$id'),\n");

        sb.append("      headers: {\n");
        sb.append("        'Accept': 'application/json',\n");
        sb.append("      },\n");

        sb.append("    );\n\n");

        sb.append("    if (response.statusCode >= 200 && ")
                .append("response.statusCode < 300) {\n");

        sb.append("      return;\n");
        sb.append("    }\n\n");

        sb.append("    throw Exception(\n");
        sb.append("      'Error al eliminar ")
                .append(nombreModelo)
                .append(": ${response.statusCode} ${response.body}',\n");
        sb.append("    );\n");

        sb.append("  }\n");

        sb.append("}\n");

        return sb.toString();
    }

    private String pluralizar(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return "recursos";
        }

        String palabra = nombre.trim().toLowerCase();

        if (palabra.endsWith("s")) {
            return palabra;
        }

        if (palabra.endsWith("x")
                || palabra.endsWith("z")
                || palabra.endsWith("ch")
                || palabra.endsWith("sh")) {

            return palabra + "es";
        }

        if (palabra.endsWith("y")
                && palabra.length() > 1) {

            char anterior = palabra.charAt(palabra.length() - 2);

            if (!"aeiou".contains(
                    String.valueOf(anterior)
            )) {

                return palabra.substring(
                        0,
                        palabra.length() - 1
                ) + "ies";
            }
        }

        return palabra + "s";
    }

    private String convertirNombreArchivo(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return "modelo";
        }

        String limpio = nombre.trim()
                .replaceAll("[^a-zA-Z0-9]", "_");

        limpio = limpio.replaceAll(
                "([a-z0-9])([A-Z])",
                "$1_$2"
        );

        return limpio.toLowerCase();
    }

    private String convertirNombreRuta(String nombreClase) {
        if (nombreClase == null || nombreClase.isBlank()) {
            return "";
        }
        return Character.toLowerCase(nombreClase.charAt(0)) + nombreClase.substring(1);
    }

    private String convertirString(Object valor) {
        if (valor == null) {
            return null;
        }

        return String.valueOf(valor);
    }
}