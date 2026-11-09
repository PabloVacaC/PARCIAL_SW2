package com.codeforge.generador.generator.flutter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class FlutterAiToolsGenerator {

        public void generar(
                        Path flutterRoot,
                        List<Map<String, Object>> elementos) throws IOException {

                Path aiDir = flutterRoot
                                .resolve("lib")
                                .resolve("generated")
                                .resolve("ai");

                Files.createDirectories(aiDir);

                if (elementos == null) {
                        generarArchivoVacio(aiDir);
                        return;
                }

                String contenido = construirArchivo(elementos);

                Path archivo = aiDir.resolve("ai_tools.dart");

                Files.writeString(archivo, contenido);

                System.out.println(
                                "FLUTTER AI: tools creadas: " + archivo);
        }

        private String construirArchivo(
                        List<Map<String, Object>> elementos) {

                StringBuilder sb = new StringBuilder();

                sb.append("/// Herramientas generadas automáticamente por CodeForge.\n");
                sb.append("///\n");
                sb.append("/// Estas definiciones representan las operaciones que\n");
                sb.append("/// puede solicitar la IA local de Flutter.\n");
                sb.append("\n");

                sb.append("class AiToolDefinition {\n");
                sb.append("  final String name;\n");
                sb.append("  final String description;\n");
                sb.append("  final Map<String, dynamic> parameters;\n\n");

                sb.append("  const AiToolDefinition({\n");
                sb.append("    required this.name,\n");
                sb.append("    required this.description,\n");
                sb.append("    required this.parameters,\n");
                sb.append("  });\n\n");

                sb.append("  Map<String, dynamic> toMap() => {\n");
                sb.append("    'name': name,\n");
                sb.append("    'description': description,\n");
                sb.append("    'parameters': parameters,\n");
                sb.append("  };\n");
                sb.append("}\n\n");

                sb.append("final List<AiToolDefinition> codeForgeAiTools = [\n");

                int herramientas = 0;

                for (Map<String, Object> elemento : elementos) {

                        if (elemento == null) {
                                continue;
                        }

                        String tipo = convertirString(elemento.get("tipo"));
                        String nombre = convertirString(elemento.get("nombre"));

                        if (nombre == null || nombre.isBlank()) {
                                continue;
                        }

                        if (tipo == null || !"CLASE".equalsIgnoreCase(tipo)) {
                                continue;
                        }

                        String nombreFlutter = convertirNombreFlutter(nombre);

                        String descripcionCrear = "Crear un registro de "
                                        + nombre
                                        + " en el backend generado por CodeForge.";

                        String descripcionListar = "Listar todos los registros de "
                                        + nombre
                                        + ".";

                        String descripcionObtener = "Obtener un registro de "
                                        + nombre
                                        + " por su identificador.";

                        String descripcionActualizar = "Actualizar un registro de "
                                        + nombre
                                        + ".";

                        String descripcionEliminar = "Eliminar un registro de "
                                        + nombre
                                        + " por su identificador.";

                        /*
                         * CREAR
                         */
                        sb.append("  AiToolDefinition(\n");
                        sb.append("    name: '")
                                        .append(nombreFlutter)
                                        .append("_crear',\n");
                        sb.append("    description: '")
                                        .append(escaparDart(descripcionCrear))
                                        .append("',\n");
                        sb.append("    parameters: ");

                        construirParametrosCrear(sb, elemento);

                        sb.append(",\n");
                        sb.append("  ),\n");

                        /*
                         * LISTAR
                         */
                        sb.append("  AiToolDefinition(\n");
                        sb.append("    name: '")
                                        .append(nombreFlutter)
                                        .append("_listar',\n");
                        sb.append("    description: '")
                                        .append(escaparDart(descripcionListar))
                                        .append("',\n");
                        sb.append("    parameters: <String, dynamic>{},\n");
                        sb.append("  ),\n");

                        /*
                         * OBTENER
                         */
                        sb.append("  AiToolDefinition(\n");
                        sb.append("    name: '")
                                        .append(nombreFlutter)
                                        .append("_obtener',\n");
                        sb.append("    description: '")
                                        .append(escaparDart(descripcionObtener))
                                        .append("',\n");
                        sb.append("    parameters: <String, dynamic>{\n");
                        sb.append("      'id': {\n");
                        sb.append("        'type': 'integer',\n");
                        sb.append("        'description': 'Identificador del registro.',\n");
                        sb.append("      },\n");
                        sb.append("    },\n");
                        sb.append("  ),\n");

                        /*
                         * ACTUALIZAR
                         */
                        sb.append("  AiToolDefinition(\n");
                        sb.append("    name: '")
                                        .append(nombreFlutter)
                                        .append("_actualizar',\n");
                        sb.append("    description: '")
                                        .append(escaparDart(descripcionActualizar))
                                        .append("',\n");
                        sb.append("    parameters: ");

                        construirParametrosActualizar(sb, elemento);

                        sb.append(",\n");
                        sb.append("  ),\n");

                        /*
                         * ELIMINAR
                         */
                        sb.append("  AiToolDefinition(\n");
                        sb.append("    name: '")
                                        .append(nombreFlutter)
                                        .append("_eliminar',\n");
                        sb.append("    description: '")
                                        .append(escaparDart(descripcionEliminar))
                                        .append("',\n");
                        sb.append("    parameters: <String, dynamic>{\n");
                        sb.append("      'id': {\n");
                        sb.append("        'type': 'integer',\n");
                        sb.append("        'description': 'Identificador del registro.',\n");
                        sb.append("      },\n");
                        sb.append("    },\n");
                        sb.append("  ),\n");

                        herramientas += 5;
                }

                sb.append("];\n\n");

                sb.append("int get codeForgeAiToolCount => ")
                                .append(herramientas)
                                .append(";\n");

                return sb.toString();
        }

        private void construirParametrosCrear(
                        StringBuilder sb,
                        Map<String, Object> elemento) {

                sb.append("<String, dynamic>{\n");

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> atributos = (List<Map<String, Object>>) elemento.get("atributos");

                if (atributos != null) {

                        for (Map<String, Object> atributo : atributos) {

                                if (atributo == null) {
                                        continue;
                                }

                                String nombre = convertirString(atributo.get("nombre"));

                                String tipo = convertirString(atributo.get("tipo"));

                                if (nombre == null || nombre.isBlank()) {
                                        continue;
                                }

                                sb.append("      '")
                                                .append(escaparDart(nombre))
                                                .append("': {\n");

                                sb.append("        'type': '")
                                                .append(mapearTipoIA(tipo))
                                                .append("',\n");

                                sb.append("        'description': 'Valor del atributo ")
                                                .append(escaparDart(nombre))
                                                .append(".',\n");

                                sb.append("      },\n");
                        }
                }

                sb.append("    }");
        }

        private void construirParametrosActualizar(
                        StringBuilder sb,
                        Map<String, Object> elemento) {

                sb.append("<String, dynamic>{\n");

                sb.append("      'id': {\n");
                sb.append("        'type': 'integer',\n");
                sb.append("        'description': 'Identificador del registro.',\n");
                sb.append("      },\n");

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> atributos = (List<Map<String, Object>>) elemento.get("atributos");

                if (atributos != null) {

                        for (Map<String, Object> atributo : atributos) {

                                if (atributo == null) {
                                        continue;
                                }

                                String nombre = convertirString(atributo.get("nombre"));

                                String tipo = convertirString(atributo.get("tipo"));

                                if (nombre == null || nombre.isBlank()) {
                                        continue;
                                }

                                /*
                                 * El primer atributo normalmente representa la PK.
                                 * No necesitamos repetirlo como parámetro de datos.
                                 */
                                sb.append("      '")
                                                .append(escaparDart(nombre))
                                                .append("': {\n");

                                sb.append("        'type': '")
                                                .append(mapearTipoIA(tipo))
                                                .append("',\n");

                                sb.append("        'description': 'Valor del atributo ")
                                                .append(escaparDart(nombre))
                                                .append(".',\n");

                                sb.append("      },\n");
                        }
                }

                sb.append("    }");
        }

        private String mapearTipoIA(String tipo) {

                if (tipo == null || tipo.isBlank()) {
                        return "string";
                }

                String valor = tipo.trim().toLowerCase();

                return switch (valor) {

                        case "int",
                                        "integer",
                                        "long",
                                        "short",
                                        "bigint" ->
                                "integer";

                        case "double",
                                        "float",
                                        "decimal",
                                        "number" ->
                                "number";

                        case "bool",
                                        "boolean" ->
                                "boolean";

                        case "date",
                                        "localdate",
                                        "datetime",
                                        "localdatetime",
                                        "timestamp" ->
                                "string";

                        default ->
                                "string";
                };
        }

        private String convertirNombreFlutter(String nombre) {

                if (nombre == null || nombre.isBlank()) {
                        return "modelo";
                }

                String limpio = nombre.trim()
                                .replaceAll("[^a-zA-Z0-9]", "_");

                limpio = limpio.replaceAll(
                                "([a-z0-9])([A-Z])",
                                "$1_$2");

                return limpio.toLowerCase();
        }

        private String escaparDart(String texto) {

                if (texto == null) {
                        return "";
                }

                return texto
                                .replace("\\", "\\\\")
                                .replace("'", "\\'");
        }

        private String convertirString(Object valor) {

                if (valor == null) {
                        return null;
                }

                return String.valueOf(valor);
        }

        private void generarArchivoVacio(
                        Path aiDir) throws IOException {

                String contenido = """
                                /// Herramientas de IA generadas por CodeForge.

                                class AiToolDefinition {
                                  final String name;
                                  final String description;
                                  final Map<String, dynamic> parameters;

                                  const AiToolDefinition({
                                    required this.name,
                                    required this.description,
                                    required this.parameters,
                                  });

                                  Map<String, dynamic> toMap() => {
                                    'name': name,
                                    'description': description,
                                    'parameters': parameters,
                                  };
                                }

                                final List<AiToolDefinition> codeForgeAiTools = [];

                                int get codeForgeAiToolCount => 0;
                                """;

                Files.writeString(
                                aiDir.resolve("ai_tools.dart"),
                                contenido);
        }
}

