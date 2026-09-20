package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ServiceGenerator {

    public List<Path> generar(
            Path proyecto,
            Map<String, Object> contenido
    ) {

        List<Path> archivosGenerados =
                new ArrayList<>();

        Object elementosObjeto =
                contenido.get("elementos");

        if (!(elementosObjeto instanceof List<?> elementos)) {
            return archivosGenerados;
        }

        Path carpetaServices =
                proyecto.resolve(
                        "src/main/java/com/codeforge/generated/service"
                );

        try {

            Files.createDirectories(
                    carpetaServices
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

                List<Map<String, Object>> operaciones =
                        convertirListaMapas(
                                elemento.get("operaciones")
                        );

                String tipoId =
                        obtenerTipoId(atributos);

                Path archivo =
                        generarService(
                                carpetaServices,
                                nombreClase,
                                tipoId,
                                atributos,
                                operaciones
                        );

                archivosGenerados.add(
                        archivo
                );
            }

            System.out.println(
                    "GENERADOR: servicios generados = "
                            + archivosGenerados.size()
            );

            return archivosGenerados;

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudieron generar los servicios.",
                    e
            );
        }
    }

    private Path generarService(
            Path carpeta,
            String nombreClase,
            String tipoId,
            List<Map<String, Object>> atributos,
            List<Map<String, Object>> operaciones
    ) throws IOException {

        String nombreService =
                nombreClase + "Service";

        String nombreRepository =
                nombreClase + "Repository";

        String nombreDto =
                nombreClase + "DTO";

        StringBuilder codigo =
                new StringBuilder();

        /*
         * ============================================================
         * IMPORTS
         * ============================================================
         */

        codigo.append(
                "package com.codeforge.generated.service;\n\n"
        );

        codigo.append(
                "import java.util.List;\n"
        );

        /*
         * Imports para tipos de fecha usados
         * en atributos u operaciones.
         */
        Set<String> importsFecha =
                obtenerImportsFecha(
                        atributos,
                        operaciones
                );

        if (
                importsFecha.contains(
                        "LocalDate"
                )
        ) {
            codigo.append(
                    "import java.time.LocalDate;\n"
            );
        }

        if (
                importsFecha.contains(
                        "LocalDateTime"
                )
        ) {
            codigo.append(
                    "import java.time.LocalDateTime;\n"
            );
        }

        if (
                importsFecha.contains(
                        "LocalTime"
                )
        ) {
            codigo.append(
                    "import java.time.LocalTime;\n"
            );
        }

        codigo.append("\n");

        codigo.append(
                "import org.springframework.stereotype.Service;\n"
        );

        codigo.append(
                "import org.springframework.beans.factory.annotation.Autowired;\n"
        );

        codigo.append(
                "import com.codeforge.generated.entity."
        );

        codigo.append(nombreClase);
        codigo.append(";\n");

        codigo.append(
                "import com.codeforge.generated.dto."
        );

        codigo.append(nombreDto);
        codigo.append(";\n");

        codigo.append(
                "import com.codeforge.generated.repository."
        );

        codigo.append(nombreRepository);
        codigo.append(";\n\n");

        /*
         * ============================================================
         * SERVICE
         * ============================================================
         */

        codigo.append("@Service\n");

        codigo.append("public class ");
        codigo.append(nombreService);
        codigo.append(" {\n\n");

        codigo.append("    private final ");
        codigo.append(nombreRepository);
        codigo.append(" repository;\n\n");

        codigo.append("    @Autowired\n");

        codigo.append("    public ");
        codigo.append(nombreService);
        codigo.append("(");
        codigo.append(nombreRepository);
        codigo.append(" repository) {\n");

        codigo.append(
                "        this.repository = repository;\n"
        );

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * CREAR
         * ============================================================
         */

        codigo.append("    public ");
        codigo.append(nombreDto);
        codigo.append(" crear(");
        codigo.append(nombreDto);
        codigo.append(" dto) {\n");

        codigo.append("        ");
        codigo.append(nombreClase);
        codigo.append(" entidad = dtoAEntidad(dto);\n");

        /*
         * El primer atributo del UML es la clave primaria.
         *
         * Se vuelve a asignar explícitamente desde el DTO para
         * garantizar que JPA reciba el identificador manualmente.
         */
        if (!atributos.isEmpty()) {

            Map<String, Object> primerAtributo =
                    atributos.get(0);

            String nombreId =
                    convertirNombreAtributo(
                            texto(
                                    primerAtributo.get("nombre")
                            )
                    );

            String metodoId =
                    convertirNombreMetodo(
                            nombreId
                    );

            codigo.append(
                    "        entidad.set"
            );

            codigo.append(metodoId);

            codigo.append(
                    "(dto.get"
            );

            codigo.append(metodoId);

            codigo.append(
                    "());\n"
            );

            /*
             * Validación para evitar enviar una entidad sin PK
             * a repository.save().
             */
            codigo.append(
                    "        if (entidad.get"
            );

            codigo.append(metodoId);

            codigo.append(
                    "() == null) {\n"
            );

            codigo.append(
                    "            throw new IllegalArgumentException("
            );

            codigo.append(
                    "\"La clave primaria '"
            );

            codigo.append(nombreId);

            codigo.append(
                    "' es obligatoria.\");\n"
            );

            codigo.append(
                    "        }\n"
            );
        }

        codigo.append("        ");

        codigo.append(nombreClase);

        codigo.append(
                " guardada = repository.save(entidad);\n"
        );

        codigo.append(
                "        return entidadADto(guardada);\n"
        );

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * OBTENER TODOS
         * ============================================================
         */

        codigo.append("    public List<");
        codigo.append(nombreDto);
        codigo.append("> obtenerTodos() {\n");

        codigo.append("        return repository.findAll()\n");

        codigo.append(
                "                .stream()\n"
        );

        codigo.append(
                "                .map(this::entidadADto)\n"
        );

        codigo.append(
                "                .toList();\n"
        );

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * OBTENER POR ID
         * ============================================================
         */

        codigo.append("    public ");
        codigo.append(nombreDto);
        codigo.append(" obtenerPorId(");
        codigo.append(tipoId);
        codigo.append(" id) {\n");

        codigo.append(
                "        return repository.findById(id)\n"
        );

        codigo.append(
                "                .map(this::entidadADto)\n"
        );

        codigo.append(
                "                .orElse(null);\n"
        );

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * ACTUALIZAR
         * ============================================================
         */

        codigo.append("    public ");
        codigo.append(nombreDto);
        codigo.append(" actualizar(");
        codigo.append(tipoId);
        codigo.append(" id, ");
        codigo.append(nombreDto);
        codigo.append(" dto) {\n");

        codigo.append("        ");
        codigo.append(nombreClase);
        codigo.append(" entidad = dtoAEntidad(dto);\n");

        /*
         * El primer atributo es el ID.
         *
         * En UPDATE la fuente oficial del ID es la URL.
         */
        if (!atributos.isEmpty()) {

            Map<String, Object> primerAtributo =
                    atributos.get(0);

            String nombreId =
                    convertirNombreAtributo(
                            texto(
                                    primerAtributo.get("nombre")
                            )
                    );

            String metodoSetter =
                    convertirNombreMetodo(
                            nombreId
                    );

            codigo.append(
                    "        entidad.set"
            );

            codigo.append(metodoSetter);

            codigo.append("(id);\n");

            /*
             * Validación para evitar guardar una entidad
             * con identificador nulo.
             */
            codigo.append(
                    "        if (entidad.get"
            );

            codigo.append(metodoSetter);

            codigo.append(
                    "() == null) {\n"
            );

            codigo.append(
                    "            throw new IllegalArgumentException("
            );

            codigo.append(
                    "\"El ID es obligatorio para actualizar.\");\n"
            );

            codigo.append(
                    "        }\n"
            );
        } else {
            codigo.append(
                    "        entidad.setId(id);\n"
            );
        }

        codigo.append(
                "        "
        );

        codigo.append(nombreClase);

        codigo.append(
                " actualizada = repository.save(entidad);\n"
        );

        codigo.append(
                "        return entidadADto(actualizada);\n"
        );

        codigo.append("    }\n\n");

        /*
         * ============================================================
         * ELIMINAR
         * ============================================================
         */

        codigo.append(
                "    public void eliminar("
        );

        codigo.append(tipoId);

        codigo.append(
                " id) {\n"
        );

        codigo.append(
                "        repository.deleteById(id);\n"
        );

        codigo.append(
                "    }\n\n"
        );

        /*
         * ============================================================
         * OPERACIONES UML
         * ============================================================
         */

        generarOperacionesUml(
                codigo,
                operaciones,
                nombreClase
        );

        /*
         * ============================================================
         * DTO -> ENTITY
         * ============================================================
         */

        codigo.append(
                "    private "
        );

        codigo.append(nombreClase);

        codigo.append(
                " dtoAEntidad("
        );

        codigo.append(nombreDto);

        codigo.append(
                " dto) {\n"
        );

        codigo.append(
                "        "
        );

        codigo.append(nombreClase);

        codigo.append(
                " entidad = new "
        );

        codigo.append(nombreClase);

        codigo.append(
                "();\n"
        );

        /*
         * Copiar todos los atributos del DTO a la entidad.
         */
        if (atributos.isEmpty()) {
            codigo.append(
                    "        entidad.setId(dto.getId());\n"
            );
        } else {
            for (Map<String, Object> atributo : atributos) {

                String nombreAtributo =
                        convertirNombreAtributo(
                                texto(
                                        atributo.get("nombre")
                                )
                        );

                String nombreMetodo =
                        convertirNombreMetodo(
                                nombreAtributo
                        );

                codigo.append(
                        "        entidad.set"
                );

                codigo.append(
                        nombreMetodo
                );

                codigo.append(
                        "(dto.get"
                );

                codigo.append(
                        nombreMetodo
                );

                codigo.append(
                        "());\n"
                );
            }
        }

        codigo.append(
                "        return entidad;\n"
        );

        codigo.append(
                "    }\n\n"
        );

        /*
         * ============================================================
         * ENTITY -> DTO
         * ============================================================
         */

        codigo.append(
                "    private "
        );

        codigo.append(nombreDto);

        codigo.append(
                " entidadADto("
        );

        codigo.append(nombreClase);

        codigo.append(
                " entidad) {\n"
        );

        codigo.append(
                "        "
        );

        codigo.append(nombreDto);

        codigo.append(
                " dto = new "
        );

        codigo.append(nombreDto);

        codigo.append(
                "();\n"
        );

        if (atributos.isEmpty()) {
            codigo.append(
                    "        dto.setId(entidad.getId());\n"
            );
        } else {
            for (Map<String, Object> atributo : atributos) {

                String nombreAtributo =
                        convertirNombreAtributo(
                                texto(
                                        atributo.get("nombre")
                                )
                        );

                String nombreMetodo =
                        convertirNombreMetodo(
                                nombreAtributo
                        );

                codigo.append(
                        "        dto.set"
                );

                codigo.append(
                        nombreMetodo
                );

                codigo.append(
                        "(entidad.get"
                );

                codigo.append(
                        nombreMetodo
                );

                codigo.append(
                        "());\n"
                );
            }
        }

        codigo.append(
                "        return dto;\n"
        );

        codigo.append(
                "    }\n\n"
        );

        codigo.append("}\n");

        Path archivo =
                carpeta.resolve(
                        nombreService + ".java"
                );

        Files.writeString(
                archivo,
                codigo.toString(),
                StandardCharsets.UTF_8
        );

        System.out.println(
                "GENERADOR: servicio creado: "
                        + archivo
        );

        return archivo;
    }

    /*
     * ================================================================
     * GENERAR OPERACIONES UML
     * ================================================================
     */

    private void generarOperacionesUml(
            StringBuilder codigo,
            List<Map<String, Object>> operaciones,
            String nombreClase
    ) {

        if (
                operaciones == null ||
                operaciones.isEmpty()
        ) {
            return;
        }

        codigo.append(
                "    // ============================================================\n"
        );

        codigo.append(
                "    // OPERACIONES UML\n"
        );

        codigo.append(
                "    // ============================================================\n\n"
        );

        Set<String> nombresUtilizados =
                new HashSet<>();

        /*
         * Reservamos los nombres del CRUD para evitar
         * conflictos con las operaciones generadas.
         */
        nombresUtilizados.add("crear");
        nombresUtilizados.add("obtenerTodos");
        nombresUtilizados.add("obtenerPorId");
        nombresUtilizados.add("actualizar");
        nombresUtilizados.add("eliminar");

        for (
                Map<String, Object> operacion :
                operaciones
        ) {

            String nombreOriginal =
                    texto(
                            operacion.get("nombre")
                    );

            if (
                    nombreOriginal == null ||
                    nombreOriginal.isBlank()
            ) {
                continue;
            }

            String nombreOperacion =
                    convertirNombreOperacion(
                            nombreOriginal
                    );

            /*
             * Si entra en conflicto con CRUD se agrega Umla.
             *
             * Ejemplo:
             * actualizar -> actualizarUml
             */
            if (
                    nombresUtilizados.contains(
                            nombreOperacion
                    )
            ) {

                nombreOperacion =
                        nombreOperacion + "Uml";
            }

            /*
             * Si ya existe otra operación con ese nombre,
             * agregamos un contador.
             */
            String nombreBase =
                    nombreOperacion;

            int contador = 2;

            while (
                    nombresUtilizados.contains(
                            nombreOperacion
                    )
            ) {

                nombreOperacion =
                        nombreBase + contador;

                contador++;
            }

            nombresUtilizados.add(
                    nombreOperacion
            );

            String tipoRetorno =
                    texto(
                            operacion.get("tipoRetorno")
                    );

            if (
                    tipoRetorno == null ||
                    tipoRetorno.isBlank()
            ) {
                tipoRetorno = "void";
            }

            String tipoRetornoJava =
                    convertirTipoJava(
                            tipoRetorno
                    );

            boolean estatica =
                    booleano(
                            operacion.get("estatica")
                    );

            String visibilidad =
                    convertirVisibilidad(
                            texto(
                                    operacion.get(
                                            "visibilidad"
                                    )
                            )
                    );

            /*
             * ABSTRACTA:
             *
             * No marcamos el método como abstracto porque
             * el Service también contiene CRUD y Spring necesita
             * una clase concreta.
             *
             * Conservamos la información en el comentario.
             */
            boolean abstracta =
                    booleano(
                            operacion.get("abstracta")
                    );

            codigo.append(
                    "    "
            );

            codigo.append(
                    visibilidad
            );

            codigo.append(
                    " "
            );

            if (estatica) {

                codigo.append(
                        "static "
                );
            }

            codigo.append(
                    tipoRetornoJava
            );

            codigo.append(
                    " "
            );

            codigo.append(
                    nombreOperacion
            );

            codigo.append(
                    "("
            );

            generarParametros(
                    codigo,
                    operacion.get(
                            "parametros"
                    )
            );

            codigo.append(
                    ") {\n"
            );

            if (abstracta) {

                codigo.append(
                        "        // Operación UML abstracta: "
                );

                codigo.append(
                        nombreOriginal
                );

                codigo.append(
                        "\n"
                );
            } else {

                codigo.append(
                        "        // Operación UML generada por CodeForge\n"
                );
            }

            generarRetornoPorDefecto(
                    codigo,
                    tipoRetornoJava,
                    nombreClase
            );

            codigo.append(
                    "    }\n\n"
            );
        }
    }

    /*
     * ================================================================
     * PARÁMETROS DE OPERACIÓN
     * ================================================================
     */

    private void generarParametros(
            StringBuilder codigo,
            Object parametrosObjeto
    ) {

        List<Map<String, Object>> parametros =
                convertirListaMapas(
                        parametrosObjeto
                );

        for (
                int i = 0;
                i < parametros.size();
                i++
        ) {

            Map<String, Object> parametro =
                    parametros.get(i);

            String nombre =
                    convertirNombreParametro(
                            texto(
                                    parametro.get(
                                            "nombre"
                                    )
                            )
                    );

            String tipo =
                    texto(
                            parametro.get(
                                    "tipo"
                            )
                    );

            if (
                    tipo == null ||
                    tipo.isBlank()
            ) {
                tipo = "Object";
            }

            String tipoJava =
                    convertirTipoJava(
                            tipo
                    );

            codigo.append(
                    tipoJava
            );

            codigo.append(
                    " "
            );

            codigo.append(
                    nombre
            );

            if (
                    i <
                    parametros.size() - 1
            ) {

                codigo.append(
                        ", "
                );
            }
        }
    }

    /*
     * ================================================================
     * RETORNO POR DEFECTO
     * ================================================================
     */

    private void generarRetornoPorDefecto(
            StringBuilder codigo,
            String tipoRetorno,
            String nombreClase
    ) {

        if (
                tipoRetorno == null ||
                tipoRetorno.isBlank() ||
                "void".equals(tipoRetorno)
        ) {
            return;
        }

        switch (tipoRetorno) {

            case "boolean", "Boolean" ->
                    codigo.append(
                            "        return false;\n"
                    );

            case "byte", "Byte" ->
                    codigo.append(
                            "        return (byte) 0;\n"
                    );

            case "short", "Short" ->
                    codigo.append(
                            "        return (short) 0;\n"
                    );

            case "int", "Integer" ->
                    codigo.append(
                            "        return 0;\n"
                    );

            case "long", "Long" ->
                    codigo.append(
                            "        return 0L;\n"
                    );

            case "float", "Float" ->
                    codigo.append(
                            "        return 0.0f;\n"
                    );

            case "double", "Double" ->
                    codigo.append(
                            "        return 0.0;\n"
                    );

            case "char", "Character" ->
                    codigo.append(
                            "        return '\\0';\n"
                    );

            case "LocalDate", "LocalDateTime", "LocalTime" ->
                    codigo.append(
                            "        return null;\n"
                    );

            default ->
                    codigo.append(
                            "        return null;\n"
                    );
        }
    }

    /*
     * ================================================================
     * IMPORTS DE FECHAS
     * ================================================================
     */

    private Set<String> obtenerImportsFecha(
            List<Map<String, Object>> atributos,
            List<Map<String, Object>> operaciones
    ) {

        Set<String> resultado =
                new HashSet<>();

        /*
         * Atributos
         */
        for (Map<String, Object> atributo : atributos) {

            agregarImportFecha(
                    resultado,
                    texto(
                            atributo.get("tipo")
                    )
            );
        }

        /*
         * Operaciones
         */
        for (Map<String, Object> operacion : operaciones) {

            agregarImportFecha(
                    resultado,
                    texto(
                            operacion.get(
                                    "tipoRetorno"
                            )
                    )
            );

            List<Map<String, Object>> parametros =
                    convertirListaMapas(
                            operacion.get(
                                    "parametros"
                            )
                    );

            for (
                    Map<String, Object> parametro :
                    parametros
            ) {

                agregarImportFecha(
                        resultado,
                        texto(
                                parametro.get(
                                        "tipo"
                                )
                        )
                );
            }
        }

        return resultado;
    }

    private void agregarImportFecha(
            Set<String> imports,
            String tipo
    ) {

        if (tipo == null) {
            return;
        }

        String tipoJava =
                convertirTipoJava(
                        tipo
                );

        if (
                "LocalDate".equals(
                        tipoJava
                )
        ) {

            imports.add(
                    "LocalDate"
            );
        }

        if (
                "LocalDateTime".equals(
                        tipoJava
                )
        ) {

            imports.add(
                    "LocalDateTime"
            );
        }

        if (
                "LocalTime".equals(
                        tipoJava
                )
        ) {

            imports.add(
                    "LocalTime"
            );
        }
    }

    /*
     * ================================================================
     * UTILIDADES
     * ================================================================
     */

    private boolean booleano(
            Object valor
    ) {

        if (valor == null) {
            return false;
        }

        if (valor instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.parseBoolean(
                String.valueOf(valor)
        );
    }

    private String convertirVisibilidad(
            String visibilidad
    ) {

        if (
                visibilidad == null ||
                visibilidad.isBlank()
        ) {
            return "public";
        }

        return switch (
                visibilidad.toUpperCase()
        ) {

            case "PRIVATE" ->
                    "private";

            case "PROTECTED" ->
                    "protected";

            case "PACKAGE",
                    "DEFAULT" ->
                    "";

            default ->
                    "public";
        };
    }

    private String convertirNombreOperacion(
            String nombre
    ) {

        if (
                nombre == null ||
                nombre.isBlank()
        ) {
            return "operacion";
        }

        String limpio =
                nombre.trim()
                        .replaceAll(
                                "[^a-zA-Z0-9_]",
                                ""
                        );

        if (limpio.isBlank()) {
            return "operacion";
        }

        return Character.toLowerCase(
                limpio.charAt(0)
        ) + limpio.substring(1);
    }

    private String convertirNombreParametro(
            String nombre
    ) {

        if (
                nombre == null ||
                nombre.isBlank()
        ) {
            return "parametro";
        }

        String limpio =
                nombre.trim()
                        .replaceAll(
                                "[^a-zA-Z0-9_]",
                                ""
                        );

        if (limpio.isBlank()) {
            return "parametro";
        }

        return Character.toLowerCase(
                limpio.charAt(0)
        ) + limpio.substring(1);
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

        for (Object elemento : lista) {

            if (
                    elemento instanceof Map<?, ?> mapa
            ) {

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

    private String convertirNombreAtributo(
            String nombre
    ) {

        if (
                nombre == null ||
                nombre.isBlank()
        ) {
            return "atributo";
        }

        String limpio =
                nombre.replaceAll(
                        "[^a-zA-Z0-9_]",
                        ""
                );

        if (limpio.isBlank()) {
            return "atributo";
        }

        return Character.toLowerCase(
                limpio.charAt(0)
        ) + limpio.substring(1);
    }

    private String convertirNombreMetodo(
            String nombre
    ) {

        if (
                nombre == null ||
                nombre.isBlank()
        ) {
            return "Atributo";
        }

        return Character.toUpperCase(
                nombre.charAt(0)
        ) + nombre.substring(1);
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
                tipo.trim()
                        .toLowerCase();

        return switch (
                tipoNormalizado
        ) {

            case "void" ->
                    "void";

            case "string" ->
                    "String";

            case "integer",
                    "int" ->
                    "Integer";

            case "long" ->
                    "Long";

            case "double" ->
                    "Double";

            case "float" ->
                    "Float";

            case "boolean",
                    "bool" ->
                    "Boolean";

            case "byte" ->
                    "Byte";

            case "short" ->
                    "Short";

            case "char" ->
                    "Character";

            case "date",
                    "localdate" ->
                    "LocalDate";

            case "datetime",
                    "localdatetime" ->
                    "LocalDateTime";

            case "time",
                    "localtime" ->
                    "LocalTime";

            case "object" ->
                    "Object";

            default ->
                    convertirNombreClase(
                            tipo
                    );
        };
    }
}