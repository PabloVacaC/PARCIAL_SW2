package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class EntityGenerator {

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

        /*
         * ============================================================
         * MAPA DE ID UML -> NOMBRE DE CLASE
         * ============================================================
         */

        Map<String, String> nombresClasesPorId =
                construirMapaClases(elementos);

        /*
         * ============================================================
         * RELACIONES UML
         * ============================================================
         */

        List<Map<String, Object>> relaciones =
                convertirListaMapas(
                        contenido.get("relaciones")
                );

        Path carpetaEntities =
                proyecto.resolve(
                        "src/main/java/com/codeforge/generated/entity"
                );

        try {

            Files.createDirectories(
                    carpetaEntities
            );

            for (Object elementoObjeto : elementos) {

                if (!(elementoObjeto instanceof Map<?, ?> elemento)) {
                    continue;
                }

                String tipo =
                        texto(
                                elemento.get("tipo")
                        );

                /*
                 * Solo las CLASES generan entidades JPA.
                 */

                if (!"CLASE".equalsIgnoreCase(tipo)) {
                    continue;
                }

                String idClase =
                        texto(
                                elemento.get("id")
                        );

                String nombreClase =
                        convertirNombreClase(
                                texto(
                                        elemento.get("nombre")
                                )
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

                List<RelacionJPA> relacionesClase =
                        obtenerRelacionesParaClase(
                                idClase,
                                nombreClase,
                                relaciones,
                                nombresClasesPorId
                        );

                Path archivo =
                        generarEntidad(
                                carpetaEntities,
                                nombreClase,
                                atributos,
                                relacionesClase
                        );

                archivosGenerados.add(
                        archivo
                );
            }

            System.out.println(
                    "GENERADOR: entidades generadas = "
                            + archivosGenerados.size()
            );

            return archivosGenerados;

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudieron generar las entidades JPA.",
                    e
            );
        }
    }

    private Path generarEntidad(
            Path carpeta,
            String nombreClase,
            List<Map<String, Object>> atributos,
            List<RelacionJPA> relaciones
    ) throws IOException {

        StringBuilder codigo =
                new StringBuilder();

        codigo.append(
                "package com.codeforge.generated.entity;\n\n"
        );

        /*
         * ============================================================
         * IMPORTS BASE
         * ============================================================
         */

        codigo.append(
                "import jakarta.persistence.Entity;\n"
        );

        codigo.append(
                "import jakarta.persistence.GeneratedValue;\n"
        );

        codigo.append(
                "import jakarta.persistence.GenerationType;\n"
        );

        codigo.append(
                "import jakarta.persistence.Id;\n"
        );

        codigo.append(
                "import jakarta.persistence.Table;\n"
        );

        /*
         * ============================================================
         * IMPORTS DE RELACIONES
         * ============================================================
         */

        boolean necesitaList =
                false;

        boolean necesitaOneToMany =
                false;

        boolean necesitaManyToOne =
                false;

        boolean necesitaOneToOne =
                false;

        boolean necesitaManyToMany =
                false;

        boolean necesitaJoinColumn =
                false;

        for (RelacionJPA relacion : relaciones) {

            if ("ONE_TO_MANY".equals(relacion.tipoJPA())) {
                necesitaList = true;
                necesitaOneToMany = true;
                necesitaManyToOne = true;
                necesitaJoinColumn = true;
            }

            if ("MANY_TO_ONE".equals(relacion.tipoJPA())) {
                necesitaManyToOne = true;
                necesitaJoinColumn = true;
            }

            if ("ONE_TO_ONE".equals(relacion.tipoJPA())) {
                necesitaOneToOne = true;
                necesitaJoinColumn = true;
            }

            if ("MANY_TO_MANY".equals(relacion.tipoJPA())) {
                necesitaList = true;
                necesitaManyToMany = true;
            }
        }

        if (necesitaList) {
            codigo.append(
                    "import java.util.List;\n"
            );

            codigo.append(
                    "import java.util.ArrayList;\n"
            );
        }

        if (necesitaManyToOne) {
            codigo.append(
                    "import jakarta.persistence.ManyToOne;\n"
            );
        }

        if (necesitaOneToMany) {
            codigo.append(
                    "import jakarta.persistence.OneToMany;\n"
            );
        }

        if (necesitaOneToOne) {
            codigo.append(
                    "import jakarta.persistence.OneToOne;\n"
            );
        }

        if (necesitaManyToMany) {
            codigo.append(
                    "import jakarta.persistence.ManyToMany;\n"
            );
        }

        if (necesitaJoinColumn) {
            codigo.append(
                    "import jakarta.persistence.JoinColumn;\n"
            );
        }

        codigo.append("\n");

        /*
         * ============================================================
         * IMPORTS DE TIPOS JAVA
         * ============================================================
         */

        boolean necesitaLocalDate = false;
        boolean necesitaLocalDateTime = false;
        boolean necesitaLocalTime = false;

        for (Map<String, Object> atributo : atributos) {

            String tipoJava =
                    convertirTipoJava(
                            texto(
                                    atributo.get("tipo")
                            )
                    );

            if ("LocalDate".equals(tipoJava)) {
                necesitaLocalDate = true;
            }

            if ("LocalDateTime".equals(tipoJava)) {
                necesitaLocalDateTime = true;
            }

            if ("LocalTime".equals(tipoJava)) {
                necesitaLocalTime = true;
            }
        }

        if (necesitaLocalDate) {
            codigo.append(
                    "import java.time.LocalDate;\n"
            );
        }

        if (necesitaLocalDateTime) {
            codigo.append(
                    "import java.time.LocalDateTime;\n"
            );
        }

        if (necesitaLocalTime) {
            codigo.append(
                    "import java.time.LocalTime;\n"
            );
        }

        if (
                necesitaLocalDate ||
                necesitaLocalDateTime ||
                necesitaLocalTime
        ) {
            codigo.append("\n");
        }

        /*
         * ============================================================
         * ENTITY
         * ============================================================
         */

        codigo.append(
                "@Entity\n"
        );

        codigo.append(
                "@Table(name = \""
        );

        codigo.append(
                convertirNombreTabla(
                        nombreClase
                )
        );

        codigo.append(
                "\")\n"
        );

        codigo.append(
                "public class "
        );

        codigo.append(
                nombreClase
        );

        codigo.append(
                " {\n\n"
        );

        /*
         * ============================================================
         * ATRIBUTOS NORMALES
         * ============================================================
         */

        if (atributos.isEmpty()) {
            codigo.append(
                    "    @Id\n"
            );
            codigo.append(
                    "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n"
            );
            codigo.append(
                    "    private Long id;\n\n"
            );
        } else {
            for (
                    int i = 0;
                    i < atributos.size();
                    i++
            ) {

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

                /*
                 * REGLA CODEFORGE:
                 * primer atributo = PK
                 */

                if (i == 0) {

                    codigo.append(
                            "    @Id\n"
                    );
                }

                codigo.append(
                        "    private "
                );

                codigo.append(
                        tipoJava
                );

                codigo.append(
                        " "
                );

                codigo.append(
                        nombreAtributo
                );

                codigo.append(
                        ";\n\n"
                );
            }
        }

        /*
         * ============================================================
         * RELACIONES JPA
         * ============================================================
         */

        for (RelacionJPA relacion : relaciones) {

            agregarRelacion(
                    codigo,
                    relacion,
                    nombreClase
            );
        }

        /*
         * ============================================================
         * CONSTRUCTOR VACÍO
         * ============================================================
         */

        codigo.append(
                "    public "
        );

        codigo.append(
                nombreClase
        );

        codigo.append(
                "() {\n"
        );

        codigo.append(
                "    }\n\n"
        );

        /*
         * ============================================================
         * CONSTRUCTOR ATRIBUTOS NORMALES
         * ============================================================
         */

        if (!atributos.isEmpty()) {

            codigo.append(
                    "    public "
            );

            codigo.append(
                    nombreClase
            );

            codigo.append(
                    "(\n"
            );

            for (
                    int i = 0;
                    i < atributos.size();
                    i++
            ) {

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

                codigo.append(
                        "            "
                );

                codigo.append(
                        tipoJava
                );

                codigo.append(
                        " "
                );

                codigo.append(
                        nombreAtributo
                );

                if (
                        i < atributos.size() - 1
                ) {

                    codigo.append(
                            ",\n"
                    );

                } else {

                    codigo.append(
                            "\n"
                    );
                }
            }

            codigo.append(
                    "    ) {\n"
            );

            for (
                    Map<String, Object> atributo :
                    atributos
            ) {

                String nombreAtributo =
                        convertirNombreAtributo(
                                texto(
                                        atributo.get("nombre")
                                )
                        );

                codigo.append(
                        "        this."
                );

                codigo.append(
                        nombreAtributo
                );

                codigo.append(
                        " = "
                );

                codigo.append(
                        nombreAtributo
                );

                codigo.append(
                        ";\n"
                );
            }

            codigo.append(
                    "    }\n\n"
            );
        }

        /*
         * ============================================================
         * GETTERS Y SETTERS
         * ============================================================
         */

        if (atributos.isEmpty()) {
            codigo.append(
                    "    public Long getId() {\n"
            );
            codigo.append(
                    "        return id;\n"
            );
            codigo.append(
                    "    }\n\n"
            );
            codigo.append(
                    "    public void setId(Long id) {\n"
            );
            codigo.append(
                    "        this.id = id;\n"
            );
            codigo.append(
                    "    }\n\n"
            );
        } else {
            for (
                    Map<String, Object> atributo :
                    atributos
            ) {

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
                 * GET
                 */

                codigo.append(
                        "    public "
                );

                codigo.append(
                        tipoJava
                );

                codigo.append(
                        " get"
                );

                codigo.append(
                        nombreMetodo
                );

                codigo.append(
                        "() {\n"
                );

                codigo.append(
                        "        return "
                );

                codigo.append(
                        nombreAtributo
                );

                codigo.append(
                        ";\n"
                );

                codigo.append(
                        "    }\n\n"
                );

                /*
                 * SET
                 */

                codigo.append(
                        "    public void set"
                );

                codigo.append(
                        nombreMetodo
                );

                codigo.append(
                        "("
                );

                codigo.append(
                        tipoJava
                );

                codigo.append(
                        " "
                );

                codigo.append(
                        nombreAtributo
                );

                codigo.append(
                        ") {\n"
                );

                codigo.append(
                        "        this."
                );

                codigo.append(
                        nombreAtributo
                );

                codigo.append(
                        " = "
                );

                codigo.append(
                        nombreAtributo
                );

                codigo.append(
                        ";\n"
                );

                codigo.append(
                        "    }\n\n"
                );
            }
        }

        /*
         * ============================================================
         * GETTERS Y SETTERS DE RELACIONES
         * ============================================================
         */

        for (RelacionJPA relacion : relaciones) {

            agregarGetterSetterRelacion(
                    codigo,
                    relacion
            );
        }

        codigo.append(
                "}\n"
        );

        Path archivo =
                carpeta.resolve(
                        nombreClase + ".java"
                );

        Files.writeString(
                archivo,
                codigo.toString(),
                StandardCharsets.UTF_8
        );

        System.out.println(
                "GENERADOR: entidad creada: " +
                        archivo
        );

        return archivo;
    }

    private void agregarRelacion(
            StringBuilder codigo,
            RelacionJPA relacion,
            String nombreClaseActual
    ) {

        String nombreCampo =
                convertirNombreAtributo(
                        relacion.nombreCampo()
                );

        if (
                "ONE_TO_MANY".equals(
                        relacion.tipoJPA()
                )
        ) {

            codigo.append(
                    "    @OneToMany(mappedBy = \""
            );

            codigo.append(
                    convertirNombreAtributo(
                            relacion.mappedBy()
                    )
            );

            codigo.append(
                    "\")\n"
            );

            codigo.append(
                    "    private List<"
            );

            codigo.append(
                    relacion.claseDestino()
            );

            codigo.append(
                    "> "
            );

            codigo.append(
                    nombreCampo
            );

            codigo.append(
                    " = new ArrayList<>();\n\n"
            );

            return;
        }

        if (
                "MANY_TO_ONE".equals(
                        relacion.tipoJPA()
                )
        ) {

            codigo.append(
                    "    @ManyToOne\n"
            );

            codigo.append(
                    "    @JoinColumn(name = \""
            );

            codigo.append(
                    convertirNombreTabla(
                            relacion.nombreCampo()
                    )
            );

            codigo.append(
                    "_id\")\n"
            );

            codigo.append(
                    "    private "
            );

            codigo.append(
                    relacion.claseDestino()
            );

            codigo.append(
                    " "
            );

            codigo.append(
                    nombreCampo
            );

            codigo.append(
                    ";\n\n"
            );

            return;
        }

        if (
                "ONE_TO_ONE".equals(
                        relacion.tipoJPA()
                )
        ) {

            codigo.append(
                    "    @OneToOne\n"
            );

            codigo.append(
                    "    @JoinColumn(name = \""
            );

            codigo.append(
                    convertirNombreTabla(
                            relacion.nombreCampo()
                    )
            );

            codigo.append(
                    "_id\")\n"
            );

            codigo.append(
                    "    private "
            );

            codigo.append(
                    relacion.claseDestino()
            );

            codigo.append(
                    " "
            );

            codigo.append(
                    nombreCampo
            );

            codigo.append(
                    ";\n\n"
            );

            return;
        }

        if (
                "MANY_TO_MANY".equals(
                        relacion.tipoJPA()
                )
        ) {

            if (relacion.mappedBy() != null && !relacion.mappedBy().isBlank()) {
                codigo.append(
                        "    @ManyToMany(mappedBy = \""
                );
                codigo.append(
                        convertirNombreAtributo(
                                relacion.mappedBy()
                        )
                );
                codigo.append(
                        "\")\n"
                );
            } else {
                codigo.append(
                        "    @ManyToMany\n"
                );
            }

            codigo.append(
                    "    private List<"
            );

            codigo.append(
                    relacion.claseDestino()
            );

            codigo.append(
                    "> "
            );

            codigo.append(
                    nombreCampo
            );

            codigo.append(
                    " = new ArrayList<>();\n\n"
            );

            return;
        }
    }

    private void agregarGetterSetterRelacion(
            StringBuilder codigo,
            RelacionJPA relacion
    ) {

        String nombreCampo =
                convertirNombreAtributo(
                        relacion.nombreCampo()
                );

        String nombreMetodo =
                convertirNombreMetodo(
                        nombreCampo
                );

        String tipo;

        if (
                "ONE_TO_MANY".equals(
                        relacion.tipoJPA()
                ) ||
                "MANY_TO_MANY".equals(
                        relacion.tipoJPA()
                )
        ) {

            tipo =
                    "List<" +
                            relacion.claseDestino() +
                            ">";

        } else {

            tipo =
                    relacion.claseDestino();
        }

        codigo.append(
                "    public "
        );

        codigo.append(
                tipo
        );

        codigo.append(
                " get"
        );

        codigo.append(
                nombreMetodo
        );

        codigo.append(
                "() {\n"
        );

        codigo.append(
                "        return "
        );

        codigo.append(
                nombreCampo
        );

        codigo.append(
                ";\n"
        );

        codigo.append(
                "    }\n\n"
        );

        codigo.append(
                "    public void set"
        );

        codigo.append(
                nombreMetodo
        );

        codigo.append(
                "("
        );

        codigo.append(
                tipo
        );

        codigo.append(
                " "
        );

        codigo.append(
                nombreCampo
        );

        codigo.append(
                ") {\n"
        );

        codigo.append(
                "        this."
        );

        codigo.append(
                nombreCampo
        );

        codigo.append(
                " = "
        );

        codigo.append(
                nombreCampo
        );

        codigo.append(
                ";\n"
        );

        codigo.append(
                "    }\n\n"
        );
    }

    private List<RelacionJPA> obtenerRelacionesParaClase(
            String idClase,
            String nombreClase,
            List<Map<String, Object>> relaciones,
            Map<String, String> nombresClasesPorId
    ) {

        List<RelacionJPA> resultado =
                new ArrayList<>();

        for (Map<String, Object> relacion :
                relaciones) {

            String tipo =
                    texto(
                            relacion.get("tipo")
                    );

            /*
             * Asociaciones, agregaciones, composiciones y recursivas generan relaciones JPA.
             */
            if (
                    tipo == null ||
                    (!tipo.equalsIgnoreCase("ASOCIACION") &&
                     !tipo.equalsIgnoreCase("AGREGACION") &&
                     !tipo.equalsIgnoreCase("COMPOSICION") &&
                     !tipo.equalsIgnoreCase("RECURSIVA"))
            ) {
                continue;
            }

            String origen =
                    texto(
                            relacion.get("origen")
                    );

            String destino =
                    texto(
                            relacion.get("destino")
                    );

            String multiplicidadOrigen =
                    texto(
                            relacion.get(
                                    "multiplicidadOrigen"
                            )
                    );

            String multiplicidadDestino =
                    texto(
                            relacion.get(
                                    "multiplicidadDestino"
                            )
                    );

            String claseOrigen =
                    nombresClasesPorId.get(
                            origen
                    );

            String claseDestino =
                    nombresClasesPorId.get(
                            destino
                    );

            if (
                    claseOrigen == null ||
                    claseDestino == null
            ) {
                continue;
            }

            /*
             * ============================================================
             * CLASE ORIGEN
             * ============================================================
             */

            if (
                    idClase.equals(
                            origen
                    )
            ) {

                resultado.add(
                        construirRelacion(
                                claseOrigen,
                                claseDestino,
                                multiplicidadOrigen,
                                multiplicidadDestino,
                                true
                        )
                );
            }

            /*
             * ============================================================
             * CLASE DESTINO
             * ============================================================
             */

            if (
                    idClase.equals(
                            destino
                    )
            ) {

                resultado.add(
                        construirRelacion(
                                claseDestino,
                                claseOrigen,
                                multiplicidadDestino,
                                multiplicidadOrigen,
                                false
                        )
                );
            }
        }

        return resultado;
    }

    private RelacionJPA construirRelacion(
            String claseActual,
            String claseRelacionado,
            String multiplicidadActual,
            String multiplicidadRelacionado,
            boolean esOrigen
    ) {

        boolean actualEsColeccion =
                esColeccion(
                        multiplicidadRelacionado
                );

        boolean relacionadoEsColeccion =
                esColeccion(
                        multiplicidadActual
                );

        /*
         * Ejemplo:
         *
         * factura 1..* ---- 1 Clientenuevo
         *
         * Desde factura:
         * Clientenuevo es una sola entidad
         * => MANY_TO_ONE
         *
         * Desde Clientenuevo:
         * Factura es una colección
         * => ONE_TO_MANY
         */

        if (
                relacionadoEsColeccion &&
                !actualEsColeccion
        ) {

            String campo =
                    convertirNombreAtributo(
                            claseRelacionado
                    );

            return new RelacionJPA(
                    "MANY_TO_ONE",
                    claseActual,
                    claseRelacionado,
                    campo,
                    null
            );
        }

        if (
                !relacionadoEsColeccion &&
                actualEsColeccion
        ) {

            String campo =
                    convertirNombreAtributoPlural(
                            claseRelacionado
                    );

            /*
             * El mappedBy necesita conocer
             * el atributo del lado MANY_TO_ONE.
             */

            String mappedBy =
                    convertirNombreAtributo(
                            claseActual
                    );

            return new RelacionJPA(
                    "ONE_TO_MANY",
                    claseActual,
                    claseRelacionado,
                    campo,
                    mappedBy
            );
        }

        if (
                !relacionadoEsColeccion &&
                !actualEsColeccion
        ) {

            String campo =
                    convertirNombreAtributo(
                            claseRelacionado
                    );

            return new RelacionJPA(
                    "ONE_TO_ONE",
                    claseActual,
                    claseRelacionado,
                    campo,
                    null
            );
        }

        /*
         * Caso muchos a muchos.
         */
        if (esOrigen) {
            String campo =
                    convertirNombreAtributoPlural(
                            claseRelacionado
                    );

            return new RelacionJPA(
                    "MANY_TO_MANY",
                    claseActual,
                    claseRelacionado,
                    campo,
                    null
            );
        } else {
            String campo =
                    convertirNombreAtributoPlural(
                            claseRelacionado
                    );

            String mappedBy =
                    convertirNombreAtributoPlural(
                            claseActual
                    );

            return new RelacionJPA(
                    "MANY_TO_MANY",
                    claseActual,
                    claseRelacionado,
                    campo,
                    mappedBy
            );
        }
    }

    private boolean esColeccion(
            String multiplicidad
    ) {

        if (
                multiplicidad == null ||
                multiplicidad.isBlank()
        ) {
            return false;
        }

        String valor =
                multiplicidad.trim();

        return valor.equals("*")
                || valor.equals("0..*")
                || valor.equals("1..*")
                || valor.endsWith("..*");
    }

    private Map<String, String> construirMapaClases(
            List<?> elementos
    ) {

        Map<String, String> resultado =
                new HashMap<>();

        for (Object elementoObjeto :
                elementos) {

            if (
                    !(elementoObjeto instanceof Map<?, ?> elemento)
            ) {
                continue;
            }

            String tipo =
                    texto(
                            elemento.get("tipo")
                    );

            if (
                    !"CLASE".equalsIgnoreCase(
                            tipo
                    )
            ) {
                continue;
            }

            String id =
                    texto(
                            elemento.get("id")
                    );

            String nombre =
                    convertirNombreClase(
                            texto(
                                    elemento.get("nombre")
                            )
                    );

            if (
                    !id.isBlank() &&
                    nombre != null &&
                    !nombre.isBlank()
            ) {

                resultado.put(
                        id,
                        nombre
                );
            }
        }

        return resultado;
    }

    private String convertirNombreAtributoPlural(
            String nombre
    ) {

        String base =
                convertirNombreAtributo(
                        nombre
                );

        if (
                base.endsWith("s")
        ) {
            return base;
        }

        return base + "s";
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

        for (Object item : lista) {

            if (
                    item instanceof Map<?, ?> mapa
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
                nombre.trim()
                        .replaceAll(
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

    private String convertirNombreTabla(
            String nombreClase
    ) {

        if (
                nombreClase == null ||
                nombreClase.isBlank()
        ) {
            return "tabla";
        }

        return nombreClase
                .replaceAll(
                        "([a-z])([A-Z])",
                        "$1_$2"
                )
                .toLowerCase();
    }

    private String convertirTipoJava(
            String tipo
    ) {

        if (
                tipo == null ||
                tipo.isBlank()
        ) {
            return "Object";
        }

        String normalizado =
                tipo.trim()
                        .toLowerCase();

        return switch (normalizado) {

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

            case "date",
                 "localdate" ->
                    "LocalDate";

            case "localdatetime",
                 "datetime" ->
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

    private record RelacionJPA(
            String tipoJPA,
            String claseActual,
            String claseDestino,
            String nombreCampo,
            String mappedBy
    ) {
    }
}