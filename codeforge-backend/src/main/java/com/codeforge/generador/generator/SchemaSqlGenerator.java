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
public class SchemaSqlGenerator {

    public Path generar(
            Path proyecto,
            Map<String, Object> contenido
    ) {

        Path carpetaDatabase =
                proyecto.resolve(
                        "src/main/resources/database"
                );

        try {

            Files.createDirectories(
                    carpetaDatabase
            );

            StringBuilder sql =
                    new StringBuilder();

            sql.append(
                    "-- ============================================================\n"
            );

            sql.append(
                    "-- SCHEMA SQL GENERADO POR CODEFORGE\n"
            );

            sql.append(
                    "-- ============================================================\n\n"
            );

            Object elementosObjeto =
                    contenido.get("elementos");

            if (!(elementosObjeto instanceof List<?> elementos)) {

                Path archivo =
                        carpetaDatabase.resolve(
                                "schema.sql"
                        );

                Files.writeString(
                        archivo,
                        sql.toString(),
                        StandardCharsets.UTF_8
                );

                return archivo;
            }

            /*
             * ============================================================
             * MAPA DE CLASES
             * ============================================================
             */

            Map<String, String> nombresClasesPorId =
                    construirMapaClases(elementos);

            /*
             * ============================================================
             * MAPA DE PK POR ID DE CLASE
             * ============================================================
             */

            Map<String, String> columnasPkPorIdClase =
                    construirMapaPk(elementos);

            /*
             * ============================================================
             * MAPA DE TIPOS DE PK POR ID DE CLASE
             *
             * Ejemplo:
             *
             * Cliente -> String -> VARCHAR(255)
             * Factura -> Long   -> BIGINT
             * ============================================================
             */

            Map<String, String> tiposPkPorIdClase =
                    construirMapaTipoPk(elementos);

            /*
             * ============================================================
             * RELACIONES
             * ============================================================
             */

            List<Map<String, Object>> relaciones =
                    convertirListaMapas(
                            contenido.get("relaciones")
                    );

            /*
             * ============================================================
             * CREAR TABLAS
             * ============================================================
             */

            for (Object elementoObjeto : elementos) {

                if (!(elementoObjeto instanceof Map<?, ?> elemento)) {
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

                sql.append(
                        generarCreateTable(
                                nombreClase,
                                atributos
                        )
                );

                sql.append("\n");
            }

            /*
             * ============================================================
             * FOREIGN KEYS
             * ============================================================
             */

            for (
                    Map<String, Object> relacion :
                    relaciones
            ) {

                String tipo =
                        texto(
                                relacion.get("tipo")
                        );

                if (
                        !"ASOCIACION".equalsIgnoreCase(
                                tipo
                        )
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
                 * ========================================================
                 * ORIGEN MUCHOS -> DESTINO UNO
                 *
                 * Factura 1..* ---- 1 Clientenuevo
                 *
                 * FK en factura.
                 * ========================================================
                 */

                if (
                        esColeccion(
                                multiplicidadOrigen
                        )
                        &&
                        !esColeccion(
                                multiplicidadDestino
                        )
                ) {

                    String pkDestino =
                            columnasPkPorIdClase.get(
                                    destino
                            );

                    String tipoPkDestino =
                            tiposPkPorIdClase.get(
                                    destino
                            );

                    if (
                            pkDestino == null ||
                            pkDestino.isBlank()
                    ) {
                        continue;
                    }

                    sql.append(
                            generarForeignKey(
                                    claseOrigen,
                                    claseDestino,
                                    pkDestino,
                                    tipoPkDestino
                            )
                    );

                    sql.append("\n");
                }

                /*
                 * ========================================================
                 * ORIGEN UNO -> DESTINO MUCHOS
                 *
                 * FK en destino.
                 * ========================================================
                 */

                else if (
                        !esColeccion(
                                multiplicidadOrigen
                        )
                        &&
                        esColeccion(
                                multiplicidadDestino
                        )
                ) {

                    String pkOrigen =
                            columnasPkPorIdClase.get(
                                    origen
                            );

                    String tipoPkOrigen =
                            tiposPkPorIdClase.get(
                                    origen
                            );

                    if (
                            pkOrigen == null ||
                            pkOrigen.isBlank()
                    ) {
                        continue;
                    }

                    sql.append(
                            generarForeignKey(
                                    claseDestino,
                                    claseOrigen,
                                    pkOrigen,
                                    tipoPkOrigen
                            )
                    );

                    sql.append("\n");
                }

                /*
                 * ========================================================
                 * UNO A UNO
                 * ========================================================
                 */

                else if (
                        !esColeccion(
                                multiplicidadOrigen
                        )
                        &&
                        !esColeccion(
                                multiplicidadDestino
                        )
                ) {

                    String pkDestino =
                            columnasPkPorIdClase.get(
                                    destino
                            );

                    String tipoPkDestino =
                            tiposPkPorIdClase.get(
                                    destino
                            );

                    if (
                            pkDestino == null ||
                            pkDestino.isBlank()
                    ) {
                        continue;
                    }

                    sql.append(
                            generarForeignKey(
                                    claseOrigen,
                                    claseDestino,
                                    pkDestino,
                                    tipoPkDestino
                            )
                    );

                    sql.append("\n");
                }

                /*
                 * ========================================================
                 * MUCHOS A MUCHOS
                 * ========================================================
                 */

                else if (
                        esColeccion(
                                multiplicidadOrigen
                        )
                        &&
                        esColeccion(
                                multiplicidadDestino
                        )
                ) {

                    String pkOrigen =
                            columnasPkPorIdClase.get(
                                    origen
                            );

                    String tipoPkOrigen =
                            tiposPkPorIdClase.get(
                                    origen
                            );

                    String pkDestino =
                            columnasPkPorIdClase.get(
                                    destino
                            );

                    String tipoPkDestino =
                            tiposPkPorIdClase.get(
                                    destino
                            );

                    if (
                            pkOrigen != null && !pkOrigen.isBlank() &&
                            pkDestino != null && !pkDestino.isBlank()
                    ) {

                        sql.append(
                                generarTablaMuchosAMuchos(
                                        claseOrigen,
                                        claseDestino,
                                        pkOrigen,
                                        tipoPkOrigen,
                                        pkDestino,
                                        tipoPkDestino
                                )
                        );

                        sql.append("\n");
                    }
                }
            }

            Path archivo =
                    carpetaDatabase.resolve(
                            "schema.sql"
                    );

            Files.writeString(
                    archivo,
                    sql.toString(),
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "GENERADOR: schema.sql creado: "
                            + archivo
            );

            return archivo;

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo generar schema.sql.",
                    e
            );
        }
    }

    private String generarCreateTable(
            String nombreClase,
            List<Map<String, Object>> atributos
    ) {

        StringBuilder sql =
                new StringBuilder();

        String nombreTabla =
                convertirNombreTabla(
                        nombreClase
                );

        sql.append(
                "CREATE TABLE "
        );

        sql.append(
                nombreTabla
        );

        sql.append(
                " (\n"
        );

        if (atributos.isEmpty()) {

            sql.append(
                    "    id BIGSERIAL PRIMARY KEY\n"
            );

        } else {

            for (
                    int i = 0;
                    i < atributos.size();
                    i++
            ) {

                Map<String, Object> atributo =
                        atributos.get(i);

                String nombre =
                        convertirNombreAtributoSql(
                                texto(
                                        atributo.get(
                                                "nombre"
                                        )
                                )
                        );

                String tipo =
                        convertirTipoSql(
                                texto(
                                        atributo.get(
                                                "tipo"
                                        )
                                )
                        );

                sql.append(
                        "    "
                );

                sql.append(
                        nombre
                );

                sql.append(
                        " "
                );

                sql.append(
                        tipo
                );

                /*
                 * Primer atributo = PK
                 */

                if (i == 0) {

                    sql.append(
                            " PRIMARY KEY"
                    );
                }

                if (
                        i <
                        atributos.size() - 1
                ) {

                    sql.append(
                            ",\n"
                    );

                } else {

                    sql.append(
                            "\n"
                    );
                }
            }
        }

        sql.append(
                ");\n"
        );

        return sql.toString();
    }

    /**
     * Genera la FK utilizando el mismo tipo SQL
     * que tiene la PK de la tabla padre.
     *
     * Ejemplo:
     *
     * PK padre = BIGINT
     * FK       = BIGINT
     *
     * PK padre = VARCHAR(255)
     * FK       = VARCHAR(255)
     */
    private String generarForeignKey(
            String tablaHija,
            String tablaPadre,
            String columnaPkPadre,
            String tipoPkPadre
    ) {

        String nombreTablaHija =
                convertirNombreTabla(
                        tablaHija
                );

        String nombreTablaPadre =
                convertirNombreTabla(
                        tablaPadre
                );

        String columnaFk =
                nombreTablaPadre +
                        "_id";

        String nombreConstraint =
                "fk_" +
                        nombreTablaHija +
                        "_" +
                        nombreTablaPadre;

        /*
         * Si por alguna razón no encontramos el tipo,
         * mantenemos VARCHAR(255) como respaldo.
         */
        if (
                tipoPkPadre == null ||
                tipoPkPadre.isBlank()
        ) {
            tipoPkPadre =
                    "VARCHAR(255)";
        }

        StringBuilder sql =
                new StringBuilder();

        /*
         * La FK usa el mismo tipo que la PK padre.
         */

        sql.append(
                "ALTER TABLE "
        );

        sql.append(
                nombreTablaHija
        );

        sql.append(
                " ADD COLUMN IF NOT EXISTS "
        );

        sql.append(
                columnaFk
        );

        sql.append(
                " "
        );

        sql.append(
                tipoPkPadre
        );

        sql.append(
                ";\n"
        );

        sql.append(
                "ALTER TABLE "
        );

        sql.append(
                nombreTablaHija
        );

        sql.append(
                " ADD CONSTRAINT "
        );

        sql.append(
                nombreConstraint
        );

        sql.append(
                " FOREIGN KEY ("
        );

        sql.append(
                columnaFk
        );

        sql.append(
                ") REFERENCES "
        );

        sql.append(
                nombreTablaPadre
        );

        sql.append(
                "("
        );

        sql.append(
                columnaPkPadre
        );

        sql.append(
                ");\n"
        );

        return sql.toString();
    }

    private String generarTablaMuchosAMuchos(
            String claseOrigen,
            String claseDestino,
            String pkOrigen,
            String tipoPkOrigen,
            String pkDestino,
            String tipoPkDestino
    ) {

        String tablaOrigen = convertirNombreTabla(claseOrigen);
        String tablaDestino = convertirNombreTabla(claseDestino);
        String tablaIntermedia = tablaOrigen + "_" + tablaDestino;

        String colOrigen = tablaOrigen + "_id";
        String colDestino = tablaDestino + "_id";

        if (tipoPkOrigen == null || tipoPkOrigen.isBlank()) {
            tipoPkOrigen = "VARCHAR(255)";
        }
        if (tipoPkDestino == null || tipoPkDestino.isBlank()) {
            tipoPkDestino = "VARCHAR(255)";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tablaIntermedia).append(" (\n");
        sql.append("    ").append(colOrigen).append(" ").append(tipoPkOrigen).append(" NOT NULL,\n");
        sql.append("    ").append(colDestino).append(" ").append(tipoPkDestino).append(" NOT NULL,\n");
        sql.append("    PRIMARY KEY (").append(colOrigen).append(", ").append(colDestino).append("),\n");
        sql.append("    CONSTRAINT fk_").append(tablaIntermedia).append("_").append(tablaOrigen)
           .append(" FOREIGN KEY (").append(colOrigen).append(") REFERENCES ").append(tablaOrigen).append(" (").append(convertirNombreAtributoSql(pkOrigen)).append("),\n");
        sql.append("    CONSTRAINT fk_").append(tablaIntermedia).append("_").append(tablaDestino)
           .append(" FOREIGN KEY (").append(colDestino).append(") REFERENCES ").append(tablaDestino).append(" (").append(convertirNombreAtributoSql(pkDestino)).append(")\n");
        sql.append(");\n");

        return sql.toString();
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
                            elemento.get(
                                    "tipo"
                            )
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
                            elemento.get(
                                    "id"
                            )
                    );

            String nombre =
                    convertirNombreClase(
                            texto(
                                    elemento.get(
                                            "nombre"
                                    )
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

    private Map<String, String> construirMapaPk(
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
                            elemento.get(
                                    "tipo"
                            )
                    );

            if (
                    !"CLASE".equalsIgnoreCase(
                            tipo
                    )
            ) {
                continue;
            }

            String idClase =
                    texto(
                            elemento.get(
                                    "id"
                            )
                    );

            List<Map<String, Object>> atributos =
                    convertirListaMapas(
                            elemento.get(
                                    "atributos"
                            )
                    );

            if (
                    idClase.isBlank() ||
                    atributos.isEmpty()
            ) {
                continue;
            }

            /*
             * Primer atributo = PK
             */

            String nombrePk =
                    convertirNombreAtributoSql(
                            texto(
                                    atributos
                                            .get(0)
                                            .get("nombre")
                            )
                    );

            resultado.put(
                    idClase,
                    nombrePk
            );
        }

        return resultado;
    }

    /**
     * Construye un mapa:
     *
     * ID de clase UML -> tipo SQL de su PK
     *
     * Ejemplo:
     *
     * cliente -> VARCHAR(255)
     * factura -> BIGINT
     */
    private Map<String, String> construirMapaTipoPk(
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
                            elemento.get(
                                    "tipo"
                            )
                    );

            if (
                    !"CLASE".equalsIgnoreCase(
                            tipo
                    )
            ) {
                continue;
            }

            String idClase =
                    texto(
                            elemento.get(
                                    "id"
                            )
                    );

            List<Map<String, Object>> atributos =
                    convertirListaMapas(
                            elemento.get(
                                    "atributos"
                            )
                    );

            if (
                    idClase.isBlank() ||
                    atributos.isEmpty()
            ) {
                continue;
            }

            /*
             * Primer atributo = PK
             */

            String tipoPk =
                    convertirTipoSql(
                            texto(
                                    atributos
                                            .get(0)
                                            .get("tipo")
                            )
                    );

            resultado.put(
                    idClase,
                    tipoPk
            );
        }

        return resultado;
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

    private String convertirNombreAtributoSql(
            String nombre
    ) {

        if (
                nombre == null ||
                nombre.isBlank()
        ) {
            return "id";
        }

        return nombre
                .trim()
                .replaceAll(
                        "[^a-zA-Z0-9_]",
                        ""
                )
                .toLowerCase();
    }

    private String convertirTipoSql(
            String tipo
    ) {

        if (
                tipo == null ||
                tipo.isBlank()
        ) {
            return "VARCHAR(255)";
        }

        return switch (
                tipo.trim().toLowerCase()
        ) {

            case "string" ->
                    "VARCHAR(255)";

            case "integer",
                 "int" ->
                    "INTEGER";

            case "long" ->
                    "BIGINT";

            case "double" ->
                    "DOUBLE PRECISION";

            case "float" ->
                    "REAL";

            case "boolean",
                 "bool" ->
                    "BOOLEAN";

            case "date",
                 "localdate" ->
                    "DATE";

            case "datetime",
                 "localdatetime" ->
                    "TIMESTAMP";

            case "time",
                 "localtime" ->
                    "TIME";

            default ->
                    "VARCHAR(255)";
        };
    }
}