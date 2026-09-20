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
public class ControllerGenerator {

    public List<Path> generar(
            Path proyecto,
            Map<String, Object> contenido
    ) {

        List<Path> archivosGenerados = new ArrayList<>();

        Object elementosObjeto =
                contenido.get("elementos");

        if (!(elementosObjeto instanceof List<?> elementos)) {
            return archivosGenerados;
        }

        Path carpetaControllers =
                proyecto.resolve(
                        "src/main/java/com/codeforge/generated/controller"
                );

        try {

            Files.createDirectories(
                    carpetaControllers
            );

            for (Object elementoObjeto : elementos) {

                if (!(elementoObjeto instanceof Map<?, ?> elemento)) {
                    continue;
                }

                String tipo =
                        texto(elemento.get("tipo"));

                if (!"CLASE".equalsIgnoreCase(tipo)) {
                    continue;
                }

                String nombreClase =
                        convertirNombreClase(
                                texto(elemento.get("nombre"))
                        );

                if (
                        nombreClase == null ||
                        nombreClase.isBlank()
                ) {
                    continue;
                }

                List<Map<String, Object>> atributos =
                        convertirListaMapas(
                                elemento.get("atributos")
                        );

                String tipoId =
                        obtenerTipoId(atributos);

                Path archivo =
                        generarController(
                                carpetaControllers,
                                nombreClase,
                                tipoId
                        );

                archivosGenerados.add(
                        archivo
                );
            }

            System.out.println(
                    "GENERADOR: controladores generados = "
                            + archivosGenerados.size()
            );

            return archivosGenerados;

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudieron generar los controladores.",
                    e
            );
        }
    }

    private Path generarController(
            Path carpeta,
            String nombreClase,
            String tipoId
    ) throws IOException {

        String nombreController =
                nombreClase + "Controller";

        String nombreService =
                nombreClase + "Service";

        String nombreDto =
                nombreClase + "DTO";

        String ruta =
                convertirNombreRuta(nombreClase);

        StringBuilder codigo =
                new StringBuilder();

        codigo.append(
                "package com.codeforge.generated.controller;\n\n"
        );

        codigo.append(
                "import java.util.List;\n\n"
        );

        codigo.append(
                "import org.springframework.http.ResponseEntity;\n"
        );

        codigo.append(
                "import org.springframework.web.bind.annotation.CrossOrigin;\n"
        );

        codigo.append(
                "import org.springframework.web.bind.annotation.DeleteMapping;\n"
        );

        codigo.append(
                "import org.springframework.web.bind.annotation.GetMapping;\n"
        );

        codigo.append(
                "import org.springframework.web.bind.annotation.PathVariable;\n"
        );

        codigo.append(
                "import org.springframework.web.bind.annotation.PostMapping;\n"
        );

        codigo.append(
                "import org.springframework.web.bind.annotation.PutMapping;\n"
        );

        codigo.append(
                "import org.springframework.web.bind.annotation.RequestBody;\n"
        );

        codigo.append(
                "import org.springframework.web.bind.annotation.RequestMapping;\n"
        );

        codigo.append(
                "import org.springframework.web.bind.annotation.RestController;\n\n"
        );

        codigo.append(
                "import com.codeforge.generated.dto."
        );

        codigo.append(nombreDto);
        codigo.append(";\n");

        codigo.append(
                "import com.codeforge.generated.service."
        );

        codigo.append(nombreService);
        codigo.append(";\n\n");

        codigo.append("@CrossOrigin(origins = \"*\")\n");
        codigo.append("@RestController\n");

        codigo.append("@RequestMapping(\"/api/");

        codigo.append(ruta);

        codigo.append("\")\n");

        codigo.append("public class ");

        codigo.append(nombreController);

        codigo.append(" {\n\n");

        codigo.append("    private final ");

        codigo.append(nombreService);

        codigo.append(" service;\n\n");

        codigo.append("    public ");

        codigo.append(nombreController);

        codigo.append("(");

        codigo.append(nombreService);

        codigo.append(" service) {\n");

        codigo.append(
                "        this.service = service;\n"
        );

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * CREATE
         * ============================================================
         */

        codigo.append("    @PostMapping\n");

        codigo.append("    public ResponseEntity<");

        codigo.append(nombreDto);

        codigo.append("> crear(\n");

        codigo.append("            @RequestBody ");

        codigo.append(nombreDto);

        codigo.append(" dto) {\n");

        codigo.append(
                "        return ResponseEntity.ok(\n"
        );

        codigo.append(
                "                service.crear(dto)\n"
        );

        codigo.append("        );\n");

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * GET ALL
         * ============================================================
         */

        codigo.append("    @GetMapping\n");

        codigo.append("    public ResponseEntity<List<");

        codigo.append(nombreDto);

        codigo.append(">> obtenerTodos() {\n");

        codigo.append(
                "        return ResponseEntity.ok(\n"
        );

        codigo.append(
                "                service.obtenerTodos()\n"
        );

        codigo.append("        );\n");

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * GET BY ID
         * ============================================================
         */

        codigo.append("    @GetMapping(\"/{id}\")\n");

        codigo.append("    public ResponseEntity<");

        codigo.append(nombreDto);

        codigo.append("> obtenerPorId(\n");

        codigo.append("            @PathVariable ");

        codigo.append(tipoId);

        codigo.append(" id) {\n");

        codigo.append("        ");

        codigo.append(nombreDto);

        codigo.append(" dto = service.obtenerPorId(id);\n\n");

        codigo.append("        if (dto == null) {\n");

        codigo.append(
                "            return ResponseEntity.notFound().build();\n"
        );

        codigo.append("        }\n\n");

        codigo.append(
                "        return ResponseEntity.ok(dto);\n"
        );

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * UPDATE
         * ============================================================
         */

        codigo.append("    @PutMapping(\"/{id}\")\n");

        codigo.append("    public ResponseEntity<");

        codigo.append(nombreDto);

        codigo.append("> actualizar(\n");

        codigo.append(
                "            @PathVariable "
        );

        codigo.append(tipoId);

        codigo.append(" id,\n");

        codigo.append("            @RequestBody ");

        codigo.append(nombreDto);

        codigo.append(" dto) {\n");

        codigo.append("        ");

        codigo.append(nombreDto);

        codigo.append(
                " existente = service.obtenerPorId(id);\n\n"
        );

        codigo.append("        if (existente == null) {\n");

        codigo.append(
                "            return ResponseEntity.notFound().build();\n"
        );

        codigo.append("        }\n\n");

        codigo.append(
                "        return ResponseEntity.ok(\n"
        );

        codigo.append(
                "                service.actualizar(id, dto)\n"
        );

        codigo.append("        );\n");

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * DELETE
         * ============================================================
         */

        codigo.append("    @DeleteMapping(\"/{id}\")\n");

        codigo.append(
                "    public ResponseEntity<Void> eliminar(\n"
        );

        codigo.append(
                "            @PathVariable "
        );

        codigo.append(tipoId);

        codigo.append(" id) {\n");

        codigo.append("        ");

        codigo.append(nombreDto);

        codigo.append(
                " existente = service.obtenerPorId(id);\n\n"
        );

        codigo.append("        if (existente == null) {\n");

        codigo.append(
                "            return ResponseEntity.notFound().build();\n"
        );

        codigo.append("        }\n\n");

        codigo.append(
                "        service.eliminar(id);\n"
        );

        codigo.append(
                "        return ResponseEntity.noContent().build();\n"
        );

        codigo.append("    }\n\n");

        codigo.append("}\n");

        Path archivo =
                carpeta.resolve(
                        nombreController + ".java"
                );

        Files.writeString(
                archivo,
                codigo.toString(),
                StandardCharsets.UTF_8
        );

        System.out.println(
                "GENERADOR: controlador creado: "
                        + archivo
        );

        return archivo;
    }

    private String obtenerTipoId(
            List<Map<String, Object>> atributos
    ) {

        if (atributos.isEmpty()) {
            return "Long";
        }

        return convertirTipoJava(
                texto(
                        atributos.get(0).get("tipo")
                )
        );
    }

    private String texto(Object valor) {

        if (valor == null) {
            return "";
        }

        return String.valueOf(valor).trim();
    }

    private List<Map<String, Object>> convertirListaMapas(
            Object valor
    ) {

        List<Map<String, Object>> resultado =
                new ArrayList<>();

        if (!(valor instanceof List<?> lista)) {
            return resultado;
        }

        for (Object elemento : lista) {

            if (elemento instanceof Map<?, ?> mapa) {

                @SuppressWarnings("unchecked")
                Map<String, Object> mapaConvertido =
                        (Map<String, Object>) mapa;

                resultado.add(
                        mapaConvertido
                );
            }
        }

        return resultado;
    }

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
            nombre.trim()
                    .split(
                            "[^a-zA-Z0-9]+"
                    );

    StringBuilder resultado =
            new StringBuilder();

    for (String parte : partes) {

        if (parte.isBlank()) {
            continue;
        }

        resultado.append(
                Character.toUpperCase(
                        parte.charAt(0)
                )
        );

        if (parte.length() > 1) {

            resultado.append(
                    parte.substring(1)
                            .toLowerCase()
            );
        }
    }

    return resultado.toString();
}

    private String convertirNombreRuta(
            String nombreClase
    ) {

        if (
                nombreClase == null ||
                nombreClase.isBlank()
        ) {
            return "";
        }

        return Character.toLowerCase(
                nombreClase.charAt(0)
        ) + nombreClase.substring(1);
    }

    private String convertirTipoJava(
            String tipo
    ) {

        if (
                tipo == null ||
                tipo.isBlank()
        ) {
            return "Long";
        }

        String tipoNormalizado =
                tipo.trim().toLowerCase();

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

            case "datetime", "localdatetime" ->
                    "LocalDateTime";

            case "time", "localtime" ->
                    "LocalTime";

            default ->
                    convertirNombreClase(tipo);
        };
    }
}