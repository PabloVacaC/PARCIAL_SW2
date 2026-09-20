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
public class DtoGenerator {

        public List<Path> generar(
                        Path proyecto,
                        Map<String, Object> contenido) {

                List<Path> archivosGenerados = new ArrayList<>();

                Object elementosObjeto = contenido.get("elementos");

                if (!(elementosObjeto instanceof List<?> elementos)) {
                        return archivosGenerados;
                }

                Path carpetaDtos = proyecto.resolve(
                                "src/main/java/com/codeforge/generated/dto");

                try {

                        Files.createDirectories(
                                        carpetaDtos);

                        for (Object elementoObjeto : elementos) {

                                if (!(elementoObjeto instanceof Map<?, ?> elemento)) {
                                        continue;
                                }

                                String tipo = texto(elemento.get("tipo"));

                                if (!"CLASE".equalsIgnoreCase(tipo)) {
                                        continue;
                                }

                                String nombreClase = convertirNombreClase(
                                                texto(elemento.get("nombre")));

                                if (nombreClase == null ||
                                                nombreClase.isBlank()) {
                                        continue;
                                }

                                Object atributosObjeto = elemento.get("atributos");

                                List<Map<String, Object>> atributos = convertirListaMapas(
                                                atributosObjeto);

                                Path archivo = generarDto(
                                                carpetaDtos,
                                                nombreClase,
                                                atributos);

                                archivosGenerados.add(
                                                archivo);
                        }

                        System.out.println(
                                        "GENERADOR: DTOs generados = "
                                                        + archivosGenerados.size());

                        return archivosGenerados;

                } catch (IOException e) {

                        throw new RuntimeException(
                                        "No se pudieron generar los DTOs.",
                                        e);
                }
        }

    private Path generarDto(
            Path carpeta,
            String nombreClase,
            List<Map<String, Object>> atributos
    ) throws IOException {

        String nombreDto =
                nombreClase + "DTO";

        StringBuilder codigo =
                new StringBuilder();

        codigo.append(
                "package com.codeforge.generated.dto;\n\n"
        );

        /*
         * Importaciones de tipos de fecha
         */
        for (Map<String, Object> atributo : atributos) {

            String tipoJava =
                    convertirTipoJava(
                            texto(atributo.get("tipo"))
                    );

            if ("LocalDate".equals(tipoJava)) {

                codigo.append(
                        "import java.time.LocalDate;\n"
                );
            }

            if ("LocalDateTime".equals(tipoJava)) {

                codigo.append(
                        "import java.time.LocalDateTime;\n"
                );
            }

            if ("LocalTime".equals(tipoJava)) {

                codigo.append(
                        "import java.time.LocalTime;\n"
                );
            }
        }

        /*
         * Importación de Jackson.
         *
         * Se utiliza para conservar exactamente el nombre
         * del atributo UML en el JSON.
         */
        codigo.append(
                "import com.fasterxml.jackson.annotation.JsonProperty;\n"
        );

        if (
                atributos.stream()
                        .map(
                                atributo ->
                                        convertirTipoJava(
                                                texto(
                                                        atributo.get("tipo")
                                                )
                                        )
                        )
                        .anyMatch(
                                tipo ->
                                        tipo.equals("LocalDate")
                                                || tipo.equals("LocalDateTime")
                                                || tipo.equals("LocalTime")
                        )
        ) {

            codigo.append("\n");
        }

        codigo.append("public class ");
        codigo.append(nombreDto);
        codigo.append(" {\n\n");

        /*
         * ============================================================
         * ATRIBUTOS
         * ============================================================
         */

        if (atributos.isEmpty()) {
            codigo.append(
                    "    @JsonProperty(\"id\")\n"
            );
            codigo.append("    private Long id;\n\n");
        } else {
            for (Map<String, Object> atributo : atributos) {

                /*
                 * Nombre original del UML.
                 *
                 * Ejemplo:
                 * CI -> CI
                 * DNI -> DNI
                 * nombre -> nombre
                 */
                String nombreOriginal =
                        texto(
                                atributo.get("nombre")
                        );

                /*
                 * Nombre válido para Java.
                 *
                 * Ejemplo:
                 * CI -> cI
                 * nombre -> nombre
                 */
                String nombreAtributo =
                        convertirNombreAtributo(
                                nombreOriginal
                        );

                String tipoJava =
                        convertirTipoJava(
                                texto(
                                        atributo.get("tipo")
                                )
                        );

                /*
                 * El JSON conserva exactamente el nombre
                 * definido en UML.
                 */
                codigo.append(
                        "    @JsonProperty(\""
                );

                codigo.append(
                        escaparJson(
                                nombreOriginal
                        )
                );

                codigo.append(
                        "\")\n"
                );

                codigo.append("    private ");
                codigo.append(tipoJava);
                codigo.append(" ");
                codigo.append(nombreAtributo);
                codigo.append(";\n\n");
            }
        }

        /*
         * ============================================================
         * CONSTRUCTOR VACÍO
         * ============================================================
         */

        codigo.append("    public ");
        codigo.append(nombreDto);
        codigo.append("() {\n");
        codigo.append("    }\n\n");

        /*
         * ============================================================
         * CONSTRUCTOR COMPLETO
         * ============================================================
         */

        if (!atributos.isEmpty()) {

            codigo.append("    public ");
            codigo.append(nombreDto);
            codigo.append("(\n");

            for (int i = 0; i < atributos.size(); i++) {

                Map<String, Object> atributo =
                        atributos.get(i);

                String nombreAtributo =
                        convertirNombreAtributo(
                                texto(
                                        atributo.get("nombre")
                                )
                        );

                String tipoJava =
                        convertirTipoJava(
                                texto(
                                        atributo.get("tipo")
                                )
                        );

                codigo.append("            ");
                codigo.append(tipoJava);
                codigo.append(" ");
                codigo.append(nombreAtributo);

                if (i < atributos.size() - 1) {
                    codigo.append(",\n");
                } else {
                    codigo.append("\n");
                }
            }

            codigo.append("    ) {\n");

            for (Map<String, Object> atributo : atributos) {

                String nombreAtributo =
                        convertirNombreAtributo(
                                texto(
                                        atributo.get("nombre")
                                )
                        );

                codigo.append("        this.");
                codigo.append(nombreAtributo);
                codigo.append(" = ");
                codigo.append(nombreAtributo);
                codigo.append(";\n");
            }

            codigo.append("    }\n\n");
        }

        /*
         * ============================================================
         * GETTERS Y SETTERS
         * ============================================================
         */

        if (atributos.isEmpty()) {
            codigo.append("    public Long getId() {\n");
            codigo.append("        return id;\n");
            codigo.append("    }\n\n");
            codigo.append("    public void setId(Long id) {\n");
            codigo.append("        this.id = id;\n");
            codigo.append("    }\n\n");
        } else {
            for (Map<String, Object> atributo : atributos) {

            String nombreAtributo =
                    convertirNombreAtributo(
                            texto(
                                    atributo.get("nombre")
                            )
                    );

            String tipoJava =
                    convertirTipoJava(
                            texto(
                                    atributo.get("tipo")
                            )
                    );

            String nombreMetodo =
                    convertirNombreMetodo(
                            nombreAtributo
                    );

            /*
             * ========================================================
             * GET
             * ========================================================
             */

            codigo.append("    public ");
            codigo.append(tipoJava);
            codigo.append(" get");
            codigo.append(nombreMetodo);
            codigo.append("() {\n");

            codigo.append("        return ");
            codigo.append(nombreAtributo);
            codigo.append(";\n");

            codigo.append("    }\n\n");

            /*
             * ========================================================
             * SET
             * ========================================================
             */

            codigo.append("    public void set");
            codigo.append(nombreMetodo);
            codigo.append("(");
            codigo.append(tipoJava);
            codigo.append(" ");
            codigo.append(nombreAtributo);
            codigo.append(") {\n");

            codigo.append("        this.");
            codigo.append(nombreAtributo);
            codigo.append(" = ");
            codigo.append(nombreAtributo);
            codigo.append(";\n");
            codigo.append("    }\n\n");
        }
    }

        codigo.append("}\n");

        Path archivo =
                carpeta.resolve(
                        nombreDto + ".java"
                );

        Files.writeString(
                archivo,
                codigo.toString(),
                StandardCharsets.UTF_8
        );

        System.out.println(
                "GENERADOR: DTO creado: " +
                        archivo
        );

        return archivo;
    }

        private String texto(Object valor) {

                if (valor == null) {
                        return "";
                }

                return String.valueOf(valor).trim();
        }

        private List<Map<String, Object>> convertirListaMapas(
                        Object valor) {

                List<Map<String, Object>> resultado = new ArrayList<>();

                if (!(valor instanceof List<?> lista)) {
                        return resultado;
                }

                for (Object elemento : lista) {

                        if (elemento instanceof Map<?, ?> mapa) {

                                @SuppressWarnings("unchecked")
                                Map<String, Object> mapaConvertido = (Map<String, Object>) mapa;

                                resultado.add(
                                                mapaConvertido);
                        }
                }

                return resultado;
        }

        private String convertirNombreClase(
                        String nombre) {

                if (nombre == null ||
                                nombre.isBlank()) {
                        return null;
                }

                String[] partes = nombre.trim()
                                .split(
                                                "[^a-zA-Z0-9]+");

                StringBuilder resultado = new StringBuilder();

                for (String parte : partes) {

                        if (parte.isBlank()) {
                                continue;
                        }

                        resultado.append(
                                        Character.toUpperCase(
                                                        parte.charAt(0)));

                        if (parte.length() > 1) {

                                resultado.append(
                                                parte.substring(1)
                                                                .toLowerCase());
                        }
                }

                return resultado.toString();
        }

        private String convertirNombreAtributo(
                        String nombre) {

                if (nombre == null ||
                                nombre.isBlank()) {
                        return "atributo";
                }

                String limpio = nombre.replaceAll(
                                "[^a-zA-Z0-9_]",
                                "");

                if (limpio.isBlank()) {
                        return "atributo";
                }

                return Character.toLowerCase(
                                limpio.charAt(0)) + limpio.substring(1);
        }

        private String convertirNombreMetodo(
                        String nombre) {

                if (nombre == null ||
                                nombre.isBlank()) {
                        return "Atributo";
                }

                return Character.toUpperCase(
                                nombre.charAt(0)) + nombre.substring(1);
        }

        private String convertirTipoJava(
                        String tipo) {

                if (tipo == null ||
                                tipo.isBlank()) {
                        return "Object";
                }

                String tipoNormalizado = tipo.trim()
                                .toLowerCase();

                return switch (tipoNormalizado) {

                        case "string" ->
                                "String";

                        case "integer", "int" ->
                                "Integer";

                        case "long" ->
                                "Long";

                        case "double" ->
                                "Double";

                        case "float" ->
                                "Float";

                        case "boolean", "bool" ->
                                "Boolean";

                        case "date", "localdate" ->
                                "LocalDate";

                        case "datetime",
                                        "localdatetime" ->
                                "LocalDateTime";

                        case "time", "localtime" ->
                                "LocalTime";

                        case "object" ->
                                "Object";

                        default ->
                                convertirNombreClase(tipo);
                };
        }

        private String escaparJson(
                        String texto) {

                if (texto == null) {
                        return "";
                }

                return texto
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"");
        }
}