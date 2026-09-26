package com.codeforge.intercambio.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Service
public class XmiImportService {

    private static final String XMI_NS =
            "http://schema.omg.org/spec/XMI/2.1";

    @SuppressWarnings("unchecked")
    public Map<String, Object> importar(
            String xml
    ) {

        if (
                xml == null ||
                xml.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El archivo XMI está vacío."
            );
        }

        try {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            /*
             * Seguridad XML
             */
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false
            );

            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false
            );

            factory.setXIncludeAware(false);

            factory.setExpandEntityReferences(false);

            try {
                factory.setAttribute(
                        XMLConstants.ACCESS_EXTERNAL_DTD,
                        ""
                );

                factory.setAttribute(
                        XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                        ""
                );
            } catch (Exception ignored) {
                /*
                 * Algunas implementaciones XML no soportan
                 * estos atributos. Las demás protecciones siguen activas.
                 */
            }

            Document document =
                    factory.newDocumentBuilder()
                            .parse(
                                    new ByteArrayInputStream(
                                            xml.getBytes(
                                                    StandardCharsets.UTF_8
                                            )
                                    )
                            );

            /*
             * ============================================================
             * RESULTADO CODEFORGE
             * ============================================================
             */

            Map<String, Object> resultado =
                    new HashMap<>();

            resultado.put(
                    "versionUML",
                    "2.5"
            );

            List<Map<String, Object>> elementos =
                    new ArrayList<>();

            List<Map<String, Object>> relaciones =
                    new ArrayList<>();

            resultado.put(
                    "elementos",
                    elementos
            );

            resultado.put(
                    "relaciones",
                    relaciones
            );

            /*
             * ============================================================
             * MAPAS AUXILIARES
             *
             * xmiId              -> nombre del tipo
             * xmiId clase        -> id interno CodeForge
             * xmiId clase        -> tipo UML
             * ============================================================
             */

            Map<String, String> nombrePorXmiId =
                    new HashMap<>();

            Map<String, String> elementoIdPorXmiId =
                    new HashMap<>();

            Map<String, String> tipoElementoPorXmiId =
                    new HashMap<>();

            /*
             * ============================================================
             * PRIMITIVE TYPES
             * ============================================================
             *
             * EA genera, por ejemplo:
             *
             * <packagedElement
             *     xmi:type="uml:PrimitiveType"
             *     xmi:id="EAID_..."
             *     name="String"/>
             *
             * ============================================================
             */

            List<Element> packagedElements =
                    obtenerElementos(
                            document,
                            "packagedElement"
                    );

            for (
                    Element element :
                    packagedElements
            ) {

                String xmiType =
                        obtenerXmiType(
                                element
                        );

                if (
                        "uml:PrimitiveType".equals(
                                xmiType
                        )
                ) {

                    String xmiId =
                            obtenerXmiId(
                                    element
                            );

                    String nombre =
                            element.getAttribute(
                                    "name"
                            );

                    if (
                            xmiId != null &&
                            !xmiId.isBlank() &&
                            nombre != null &&
                            !nombre.isBlank()
                    ) {

                        nombrePorXmiId.put(
                                xmiId,
                                nombre
                        );
                    }
                }
            }

            /*
             * También se soporta el String especial que EA coloca
             * dentro de <primitivetypes>.
             */
            List<Element> primitiveElements =
                    obtenerElementos(
                            document,
                            "packagedElement"
                    );

            for (
                    Element element :
                    primitiveElements
            ) {

                String xmiType =
                        obtenerXmiType(
                                element
                        );

                if (
                        !"uml:PrimitiveType".equals(
                                xmiType
                        )
                ) {
                    continue;
                }

                String xmiId =
                        obtenerXmiId(
                                element
                        );

                String nombre =
                        element.getAttribute(
                                "name"
                        );

                if (
                        xmiId != null &&
                        !xmiId.isBlank() &&
                        nombre != null &&
                        !nombre.isBlank()
                ) {

                    nombrePorXmiId.put(
                            xmiId,
                            nombre
                    );
                }
            }

            /*
             * ============================================================
             * PASADA 1: CLASES / INTERFACES / ENUMERACIONES
             * ============================================================
             */

            Set<String> nombresAssociationClasses = new HashSet<>();
            for (Element element : packagedElements) {
                String xmiType = obtenerXmiType(element);
                if ("uml:AssociationClass".equals(xmiType)) {
                    String nombre = element.getAttribute("name");
                    if (nombre != null && !nombre.isBlank()) {
                        nombresAssociationClasses.add(nombre.trim().toLowerCase());
                    }
                }
            }

            Map<String, Map<String, Object>> elementoPorNombre = new HashMap<>();

            for (
                    Element element :
                    packagedElements
            ) {

                String xmiType =
                        obtenerXmiType(
                                element
                        );

                if (
                        !esElementoUml(
                                xmiType
                        )
                ) {
                    continue;
                }

                String xmiId =
                        obtenerXmiId(
                                element
                        );

                if (
                        xmiId == null ||
                        xmiId.isBlank()
                ) {
                    xmiId =
                            UUID.randomUUID()
                                    .toString();
                }

                String nombre =
                        element.getAttribute(
                                "name"
                        );

                if (nombre == null || nombre.isBlank()) {
                    nombre = "SinNombre";
                }

                String nombreKey = nombre.trim().toLowerCase();

                // Si es uml:Class pero existe un uml:AssociationClass con el mismo nombre, omitimos el uml:Class
                if ("uml:Class".equals(xmiType) && nombresAssociationClasses.contains(nombreKey)) {
                    continue;
                }

                // Si ya fue procesado un elemento con este mismo nombre (por ejemplo dos AssociationClass o un duplicado de XMI)
                if (elementoPorNombre.containsKey(nombreKey)) {
                    Map<String, Object> existente = elementoPorNombre.get(nombreKey);
                    String idExistente = (String) existente.get("id");
                    elementoIdPorXmiId.put(xmiId, idExistente);
                    nombrePorXmiId.put(xmiId, nombre);

                    if ("uml:AssociationClass".equals(xmiType)) {
                        existente.put("estereotipo", "association");
                        List<Map<String, Object>> attrs = leerAtributos(element, nombrePorXmiId);
                        if (!attrs.isEmpty()) {
                            existente.put("atributos", attrs);
                        }
                        List<Map<String, Object>> ops = leerOperaciones(element, nombrePorXmiId);
                        if (!ops.isEmpty()) {
                            existente.put("operaciones", ops);
                        }
                    }
                    continue;
                }

                String idInterno =
                        UUID.randomUUID()
                                .toString();

                String tipo =
                        convertirTipoElemento(
                                xmiType
                        );

                Map<String, Object> modeloElemento =
                        new HashMap<>();

                modeloElemento.put(
                        "id",
                        idInterno
                );

                modeloElemento.put(
                        "tipo",
                        tipo
                );

                modeloElemento.put(
                        "nombre",
                        nombre
                );

                modeloElemento.put(
                        "final",
                        false
                );

                modeloElemento.put(
                        "abstracto",
                        "true".equals(
                                element.getAttribute(
                                        "isAbstract"
                                )
                        )
                );

                modeloElemento.put(
                        "visibilidad",
                        convertirVisibilidad(
                                element.getAttribute(
                                        "visibility"
                                )
                        )
                );

                if ("uml:AssociationClass".equals(xmiType)) {
                    modeloElemento.put(
                            "estereotipo",
                            "association"
                    );
                }

                Map<String, Object> posicion =
                        new HashMap<>();

                posicion.put(
                        "x",
                        elementos.size() * 320.0 + 80.0
                );

                posicion.put(
                        "y",
                        80.0
                );

                modeloElemento.put(
                        "posicion",
                        posicion
                );

                Map<String, Object> tamaño =
                        new HashMap<>();

                tamaño.put(
                        "ancho",
                        260.0
                );

                tamaño.put(
                        "alto",
                        180.0
                );

                modeloElemento.put(
                        "tamaño",
                        tamaño
                );

                /*
                 * Atributos
                 */
                modeloElemento.put(
                        "atributos",
                        leerAtributos(
                                element,
                                nombrePorXmiId
                        )
                );

                /*
                 * Operaciones
                 */
                modeloElemento.put(
                        "operaciones",
                        leerOperaciones(
                                element,
                                nombrePorXmiId
                        )
                );

                /*
                 * Literales de ENUM
                 */
                if (
                        "ENUM".equals(
                                tipo
                        )
                ) {

                    modeloElemento.put(
                            "literales",
                            leerLiterales(
                                    element
                            )
                    );
                }

                elementos.add(
                        modeloElemento
                );

                elementoPorNombre.put(
                        nombreKey,
                        modeloElemento
                );

                nombrePorXmiId.put(
                        xmiId,
                        nombre
                );

                elementoIdPorXmiId.put(
                        xmiId,
                        idInterno
                );

                tipoElementoPorXmiId.put(
                        xmiId,
                        tipo
                );
            }

            /*
             * ============================================================
             * GENERALIZACIONES / REALIZACIONES
             * ============================================================
             */

            for (
                    Element element :
                    packagedElements
            ) {

                String origenXmiId =
                        obtenerXmiId(
                                element
                        );

                if (
                        !elementoIdPorXmiId.containsKey(
                                origenXmiId
                        )
                ) {
                    continue;
                }

                /*
                 * GENERALIZACIÓN
                 */

                List<Element> generalizaciones =
                        hijos(
                                element,
                                "generalization"
                        );

                for (
                        Element generalizacion :
                        generalizaciones
                ) {

                    String destinoXmiId =
                            obtenerReferencia(
                                    generalizacion,
                                    "general"
                            );

                    if (
                            destinoXmiId == null
                    ) {
                        continue;
                    }

                    if (
                            !elementoIdPorXmiId.containsKey(
                                    destinoXmiId
                            )
                    ) {
                        continue;
                    }

                    relaciones.add(
                            crearRelacion(
                                    "GENERALIZACION",
                                    elementoIdPorXmiId.get(
                                            origenXmiId
                                    ),
                                    elementoIdPorXmiId.get(
                                            destinoXmiId
                                    ),
                                    null,
                                    null,
                                    "1",
                                    "1"
                            )
                    );
                }

                /*
                 * REALIZACIÓN
                 */

                List<Element> realizaciones =
                        hijos(
                                element,
                                "interfaceRealization"
                        );

                for (
                        Element realization :
                        realizaciones
                ) {

                    String destinoXmiId =
                            obtenerReferencia(
                                    realization,
                                    "contract"
                            );

                    if (
                            destinoXmiId == null
                    ) {
                        continue;
                    }

                    if (
                            !elementoIdPorXmiId.containsKey(
                                    destinoXmiId
                            )
                    ) {
                        continue;
                    }

                    relaciones.add(
                            crearRelacion(
                                    "REALIZACION",
                                    elementoIdPorXmiId.get(
                                            origenXmiId
                                    ),
                                    elementoIdPorXmiId.get(
                                            destinoXmiId
                                    ),
                                    null,
                                    null,
                                    "1",
                                    "1"
                            )
                    );
                }
            }

            /*
             * ============================================================
             * ASOCIACIONES
             * ============================================================
             *
             * Esta es la parte importante para Enterprise Architect.
             *
             * EA genera:
             *
             * <packagedElement xmi:type="uml:Association">
             *
             *     <memberEnd xmi:idref="..."/>
             *     <memberEnd xmi:idref="..."/>
             *
             *     <ownedEnd ...>
             *         <type xmi:idref="CLASE"/>
             *         <lowerValue .../>
             *         <upperValue .../>
             *     </ownedEnd>
             *
             * ============================================================
             */

            for (
                    Element association :
                    packagedElements
            ) {

                String xmiType =
                        obtenerXmiType(
                                association
                        );

                /*
                 * Asociación o Clase de Asociación
                 */
                if (
                        !"uml:Association".equals(
                                xmiType
                        ) &&
                        !"uml:AssociationClass".equals(
                                xmiType
                        )
                ) {
                    continue;
                }

                List<Element> ownedEnds =
                        hijos(
                                association,
                                "ownedEnd"
                        );

                /*
                 * Necesitamos al menos dos extremos.
                 */
                if (
                        ownedEnds.size() < 2
                ) {
                    continue;
                }

                Element end1 =
                        ownedEnds.get(0);

                Element end2 =
                        ownedEnds.get(1);

                String tipoClase1 =
                        obtenerTipoDesdeEnd(
                                end1
                        );

                String tipoClase2 =
                        obtenerTipoDesdeEnd(
                                end2
                        );

                if (
                        tipoClase1 == null ||
                        tipoClase2 == null
                ) {
                    continue;
                }

                if (
                        !elementoIdPorXmiId.containsKey(
                                tipoClase1
                        ) ||
                        !elementoIdPorXmiId.containsKey(
                                tipoClase2
                        )
                ) {
                    continue;
                }

                String multiplicidad1 =
                        leerMultiplicidadEnd(
                                end1
                        );

                String multiplicidad2 =
                        leerMultiplicidadEnd(
                                end2
                        );

                String tipoRelacion =
                        "uml:AssociationClass".equals(
                                xmiType
                        )
                                ? "CLASE_ASOCIACION"
                                : obtenerTipoRelacion(
                                        end1,
                                        end2
                                );

                /*
                 * Para EA, el orden de los ownedEnd representa
                 * directamente sus extremos.
                 */
                String nombreAsocClass = association.getAttribute("name");
                String rolDestinoFinal = ("uml:AssociationClass".equals(xmiType) && nombreAsocClass != null && !nombreAsocClass.isBlank())
                        ? nombreAsocClass
                        : obtenerNombreEnd(end2);

                String idOrigenFinal = elementoIdPorXmiId.get(tipoClase1);
                String idDestinoFinal = elementoIdPorXmiId.get(tipoClase2);
                if (idOrigenFinal != null && idOrigenFinal.equals(idDestinoFinal) && "ASOCIACION".equals(tipoRelacion)) {
                    tipoRelacion = "RECURSIVA";
                }

                relaciones.add(
                        crearRelacion(
                                tipoRelacion,
                                idOrigenFinal,
                                idDestinoFinal,
                                obtenerNombreEnd(
                                        end1
                                ),
                                rolDestinoFinal,
                                multiplicidad1,
                                multiplicidad2
                        )
                );
            }

            /*
             * ============================================================
             * RESPALDO ENTERPRISE ARCHITECT
             * ============================================================
             *
             * Si una asociación no pudo construirse desde ownedEnd,
             * intentamos leer la extensión de EA:
             *
             * <connector>
             *   <source ...>
             *      <type multiplicity="1..*"/>
             *   </source>
             *   <target ...>
             *      <type multiplicity="1"/>
             *   </target>
             * </connector>
             *
             * ============================================================
             */

            importarAsociacionesDesdeExtensionEA(
                    document,
                    elementoIdPorXmiId,
                    relaciones
            );

            return resultado;

        } catch (
                Exception e
        ) {

            throw new IllegalArgumentException(
                    "No se pudo importar el archivo XMI: " +
                            e.getMessage(),
                    e
            );
        }
    }

    /*
     * ====================================================================
     * ATRIBUTOS
     * ====================================================================
     */

    private List<Map<String, Object>> leerAtributos(
            Element elemento,
            Map<String, String> nombrePorXmiId
    ) {

        List<Map<String, Object>> resultado =
                new ArrayList<>();

        List<Element> atributos =
                hijos(
                        elemento,
                        "ownedAttribute"
                );

        for (
                Element atributo :
                atributos
        ) {

            String nombre =
                    atributo.getAttribute(
                            "name"
                    );

            /*
             * Una Association también puede ser encontrada por
             * ownedAttribute en otros XMI. Aquí únicamente procesamos
             * hijos directos de una Class/Interface/Enum.
             */
            if (
                    nombre == null ||
                    nombre.isBlank()
            ) {
                continue;
            }

            Map<String, Object> mapa =
                    new HashMap<>();

            mapa.put(
                    "id",
                    UUID.randomUUID()
                            .toString()
            );

            mapa.put(
                    "nombre",
                    nombre
            );

            mapa.put(
                    "tipo",
                    resolverTipoAtributo(
                            atributo,
                            nombrePorXmiId
                    )
            );

            mapa.put(
                    "visibilidad",
                    convertirVisibilidad(
                            atributo.getAttribute(
                                    "visibility"
                            )
                    )
            );

            mapa.put(
                    "derivado",
                    "true".equals(
                            atributo.getAttribute(
                                    "isDerived"
                            )
                    )
            );

            mapa.put(
                    "estatico",
                    "true".equals(
                            atributo.getAttribute(
                                    "isStatic"
                            )
                    )
            );

            mapa.put(
                    "valorInicial",
                    null
            );

            mapa.put(
                    "multiplicidad",
                    leerMultiplicidadAtributo(
                            atributo
                    )
            );

            resultado.add(
                    mapa
            );
        }

        return resultado;
    }

    private String resolverTipoAtributo(
            Element atributo,
            Map<String, String> nombrePorXmiId
    ) {

        /*
         * ------------------------------------------------------------
         * Caso 1:
         *
         * <type xmi:idref="EAID_..."/>
         * ------------------------------------------------------------
         */

        Element type =
                primerHijo(
                        atributo,
                        "type"
                );

        if (
                type != null
        ) {

            String idref =
                    obtenerReferencia(
                            type,
                            "idref"
                    );

            if (
                    idref != null &&
                    !idref.isBlank()
            ) {

                String nombreTipo =
                        nombrePorXmiId.get(
                                idref
                        );

                if (
                        nombreTipo != null &&
                        !nombreTipo.isBlank()
                ) {

                    return nombreTipo;
                }

                /*
                 * Caso especial de EA:
                 * EAJava_String
                 */
                if (
                        "EAJava_String".equals(
                                idref
                        )
                ) {

                    return "String";
                }
            }

            /*
             * Algunos XMI pueden traer type="..."
             */
            String typeAttribute =
                    type.getAttribute(
                            "type"
                    );

            if (
                    typeAttribute != null &&
                    !typeAttribute.isBlank()
            ) {

                return typeAttribute;
            }
        }

        /*
         * ------------------------------------------------------------
         * Caso 2:
         * Extensión EA
         * <properties type="String".../>
         *
         * Esto se procesa indirectamente porque, si no existe
         * el <type>, podemos buscar por xmi:idref en todo el documento.
         * ------------------------------------------------------------
         */

        return "Object";
    }

    private String leerMultiplicidadAtributo(
            Element atributo
    ) {

        String lower =
                obtenerValorLiteral(
                        atributo,
                        "lowerValue"
                );

        String upper =
                obtenerValorLiteral(
                        atributo,
                        "upperValue"
                );

        if (
                lower == null ||
                lower.isBlank()
        ) {
            lower =
                    atributo.getAttribute(
                            "lower"
                    );
        }

        if (
                upper == null ||
                upper.isBlank()
        ) {
            upper =
                    atributo.getAttribute(
                            "upper"
                    );
        }

        return combinarMultiplicidad(
                lower,
                upper
        );
    }

    /*
     * ====================================================================
     * OPERACIONES
     * ====================================================================
     */

    private List<Map<String, Object>> leerOperaciones(
            Element elemento,
            Map<String, String> nombrePorXmiId
    ) {

        List<Map<String, Object>> resultado =
                new ArrayList<>();

        for (
                Element operacion :
                hijos(
                        elemento,
                        "ownedOperation"
                )
        ) {

            Map<String, Object> mapa =
                    new HashMap<>();

            mapa.put(
                    "id",
                    UUID.randomUUID()
                            .toString()
            );

            mapa.put(
                    "nombre",
                    operacion.getAttribute(
                            "name"
                    )
            );

            mapa.put(
                    "visibilidad",
                    convertirVisibilidad(
                            operacion.getAttribute(
                                    "visibility"
                            )
                    )
            );

            mapa.put(
                    "parametros",
                    leerParametros(
                            operacion,
                            nombrePorXmiId
                    )
            );

            resultado.add(
                    mapa
            );
        }

        return resultado;
    }

    private List<Map<String, Object>> leerParametros(
            Element operacion,
            Map<String, String> nombrePorXmiId
    ) {

        List<Map<String, Object>> resultado =
                new ArrayList<>();

        for (
                Element parametro :
                hijos(
                        operacion,
                        "ownedParameter"
                )
        ) {

            Map<String, Object> mapa =
                    new HashMap<>();

            mapa.put(
                    "id",
                    UUID.randomUUID()
                            .toString()
            );

            mapa.put(
                    "nombre",
                    parametro.getAttribute(
                            "name"
                    )
            );

            mapa.put(
                    "tipo",
                    resolverTipoAtributo(
                            parametro,
                            nombrePorXmiId
                    )
            );

            String direction =
                    parametro.getAttribute(
                            "direction"
                    );

            if (
                    direction == null ||
                    direction.isBlank()
            ) {
                direction = "in";
            }

            mapa.put(
                    "direccion",
                    direction
            );

            resultado.add(
                    mapa
            );
        }

        return resultado;
    }

    private List<Map<String, Object>> leerLiterales(
            Element elemento
    ) {

        List<Map<String, Object>> resultado =
                new ArrayList<>();

        for (
                Element literal :
                hijos(
                        elemento,
                        "ownedLiteral"
                )
        ) {

            Map<String, Object> mapa =
                    new HashMap<>();

            mapa.put(
                    "id",
                    UUID.randomUUID()
                            .toString()
            );

            mapa.put(
                    "nombre",
                    literal.getAttribute(
                            "name"
                    )
            );

            resultado.add(
                    mapa
            );
        }

        return resultado;
    }

    /*
     * ====================================================================
     * ASOCIACIONES DESDE EXTENSIÓN EA
     * ====================================================================
     */

    private void importarAsociacionesDesdeExtensionEA(
            Document document,
            Map<String, String> elementoIdPorXmiId,
            List<Map<String, Object>> relaciones
    ) {

        List<Element> connectors =
                obtenerElementos(
                        document,
                        "connector"
                );

        for (
                Element connector :
                connectors
        ) {

            Element source =
                    primerHijo(
                            connector,
                            "source"
                    );

            Element target =
                    primerHijo(
                            connector,
                            "target"
                    );

            if (
                    source == null ||
                    target == null
            ) {
                continue;
            }

            String sourceXmiId =
                    obtenerReferencia(
                            source,
                            "idref"
                    );

            String targetXmiId =
                    obtenerReferencia(
                            target,
                            "idref"
                    );

            if (
                    sourceXmiId == null ||
                    targetXmiId == null
            ) {
                continue;
            }

            if (
                    !elementoIdPorXmiId.containsKey(
                            sourceXmiId
                    ) ||
                    !elementoIdPorXmiId.containsKey(
                            targetXmiId
                    )
            ) {
                continue;
            }

            String multiplicidadOrigen =
                    leerMultiplicityExtension(
                            source
                    );

            String multiplicidadDestino =
                    leerMultiplicityExtension(
                            target
                    );

            /*
             * Evitar duplicar una relación que ya fue leída
             * correctamente desde ownedEnd.
             */
            if (
                    existeRelacion(
                            relaciones,
                            elementoIdPorXmiId.get(
                                    sourceXmiId
                            ),
                            elementoIdPorXmiId.get(
                                    targetXmiId
                            )
                    )
            ) {
                continue;
            }

            relaciones.add(
                    crearRelacion(
                            "ASOCIACION",
                            elementoIdPorXmiId.get(
                                    sourceXmiId
                            ),
                            elementoIdPorXmiId.get(
                                    targetXmiId
                            ),
                            null,
                            null,
                            multiplicidadOrigen,
                            multiplicidadDestino
                    )
            );
        }
    }

    private String leerMultiplicityExtension(
            Element extremo
    ) {

        Element type =
                primerHijo(
                        extremo,
                        "type"
                );

        if (
                type == null
        ) {
            return "1";
        }

        String multiplicidad =
                type.getAttribute(
                        "multiplicity"
                );

        if (
                multiplicidad == null ||
                multiplicidad.isBlank()
        ) {

            return "1";
        }

        return multiplicidad;
    }

    private boolean existeRelacion(
            List<Map<String, Object>> relaciones,
            String origen,
            String destino
    ) {

        for (
                Map<String, Object> relacion :
                relaciones
        ) {

            Object origenActual =
                    relacion.get(
                            "origen"
                    );

            Object destinoActual =
                    relacion.get(
                            "destino"
                    );

            if (
                    origen.equals(
                            String.valueOf(
                                    origenActual
                            )
                    ) &&
                    destino.equals(
                            String.valueOf(
                                    destinoActual
                            )
                    )
            ) {

                return true;
            }

            /*
             * También evitamos duplicar si viene invertida.
             */
            if (
                    destino.equals(
                            String.valueOf(
                                    origenActual
                            )
                    ) &&
                    origen.equals(
                            String.valueOf(
                                    destinoActual
                            )
                    )
            ) {

                return true;
            }
        }

        return false;
    }
private String obtenerNombreEnd(
        Element end
) {

    String nombre =
            end.getAttribute(
                    "name"
            );

    if (
            nombre == null ||
            nombre.isBlank()
    ) {
        return null;
    }

    return nombre;
}
    /*
     * ====================================================================
     * EXTREMOS DE ASOCIACIÓN
     * ====================================================================
     */

    private String obtenerTipoDesdeEnd(
            Element end
    ) {

        if (end == null) {
            return null;
        }

        String typeAttr = end.getAttribute("type");
        if (typeAttr != null && !typeAttr.isBlank()) {
            return typeAttr;
        }

        String idrefEnd = obtenerReferencia(end, "idref");
        if (idrefEnd != null && !idrefEnd.isBlank()) {
            return idrefEnd;
        }

        Element type =
                primerHijo(
                        end,
                        "type"
                );

        if (
                type != null
        ) {

            String idref =
                    obtenerReferencia(
                            type,
                            "idref"
                    );

            if (
                    idref != null &&
                    !idref.isBlank()
            ) {
                return idref;
            }

            String typeAttribute =
                    type.getAttribute(
                            "type"
                    );

            if (
                    typeAttribute != null &&
                    !typeAttribute.isBlank()
            ) {
                return typeAttribute;
            }
        }

        return null;
    }

    private String leerMultiplicidadEnd(
            Element end
    ) {

        String lower =
                obtenerValorLiteral(
                        end,
                        "lowerValue"
                );

        String upper =
                obtenerValorLiteral(
                        end,
                        "upperValue"
                );

        /*
         * Algunos XMI pueden traer lower/upper directamente.
         */
        if (
                lower == null ||
                lower.isBlank()
        ) {

            lower =
                    end.getAttribute(
                            "lower"
                    );
        }

        if (
                upper == null ||
                upper.isBlank()
        ) {

            upper =
                    end.getAttribute(
                            "upper"
                    );
        }

        return combinarMultiplicidad(
                lower,
                upper
        );
    }

    private String obtenerTipoRelacion(
            Element end1,
            Element end2
    ) {

        String aggregation1 =
                end1.getAttribute(
                        "aggregation"
                );

        String aggregation2 =
                end2.getAttribute(
                        "aggregation"
                );

        if (
                "composite".equalsIgnoreCase(
                        aggregation1
                ) ||
                "composite".equalsIgnoreCase(
                        aggregation2
                )
        ) {

            return "COMPOSICION";
        }

        if (
                "shared".equalsIgnoreCase(
                        aggregation1
                ) ||
                "shared".equalsIgnoreCase(
                        aggregation2
                )
        ) {

            return "AGREGACION";
        }

        return "ASOCIACION";
    }

    /*
     * ====================================================================
     * RELACIÓN
     * ====================================================================
     */

    private Map<String, Object> crearRelacion(
            String tipo,
            String origen,
            String destino,
            String rolOrigen,
            String rolDestino,
            String multiplicidadOrigen,
            String multiplicidadDestino
    ) {

        Map<String, Object> relacion =
                new HashMap<>();

        relacion.put(
                "id",
                UUID.randomUUID()
                        .toString()
        );

        relacion.put(
                "tipo",
                tipo
        );

        relacion.put(
                "origen",
                origen
        );

        relacion.put(
                "destino",
                destino
        );

        relacion.put(
                "rolOrigen",
                rolOrigen
        );

        relacion.put(
                "rolDestino",
                rolDestino
        );

        relacion.put(
                "multiplicidadOrigen",
                multiplicidadOrigen
        );

        relacion.put(
                "multiplicidadDestino",
                multiplicidadDestino
        );

        return relacion;
    }

    /*
     * ====================================================================
     * UTILIDADES XML
     * ====================================================================
     */

    private List<Element> obtenerElementos(
            Document document,
            String localName
    ) {

        List<Element> resultado =
                new ArrayList<>();

        var nodos =
                document.getElementsByTagNameNS(
                        "*",
                        localName
                );

        for (
                int i = 0;
                i < nodos.getLength();
                i++
        ) {

            Node nodo =
                    nodos.item(i);

            if (
                    nodo instanceof Element
            ) {

                resultado.add(
                        (Element) nodo
                );
            }
        }

        return resultado;
    }

    private List<Element> hijos(
            Element padre,
            String localName
    ) {

        List<Element> resultado =
                new ArrayList<>();

        var nodos =
                padre.getChildNodes();

        for (
                int i = 0;
                i < nodos.getLength();
                i++
        ) {

            Node nodo =
                    nodos.item(i);

            if (
                    !(nodo instanceof Element)
            ) {
                continue;
            }

            Element element =
                    (Element) nodo;

            String actual =
                    element.getLocalName();

            if (
                    localName.equals(
                            actual
                    ) ||
                    localName.equals(
                            element.getNodeName()
                    )
            ) {

                resultado.add(
                        element
                );
            }
        }

        return resultado;
    }

    private Element primerHijo(
            Element padre,
            String localName
    ) {

        List<Element> hijos =
                hijos(
                        padre,
                        localName
                );

        if (
                hijos.isEmpty()
        ) {
            return null;
        }

        return hijos.get(0);
    }

    private String obtenerXmiId(
            Element element
    ) {

        String id =
                element.getAttributeNS(
                        XMI_NS,
                        "id"
                );

        if (
                id == null ||
                id.isBlank()
        ) {

            id =
                    element.getAttribute(
                            "xmi:id"
                    );
        }

        return id;
    }

    private String obtenerXmiType(
            Element element
    ) {

        String type =
                element.getAttributeNS(
                        XMI_NS,
                        "type"
                );

        if (
                type == null ||
                type.isBlank()
        ) {

            type =
                    element.getAttribute(
                            "xmi:type"
                    );
        }

        return type;
    }

    private String obtenerReferencia(
            Element element,
            String atributo
    ) {

        String valor =
                element.getAttributeNS(
                        XMI_NS,
                        atributo
                );

        if (
                valor == null ||
                valor.isBlank()
        ) {

            valor =
                    element.getAttribute(
                            "xmi:" + atributo
                    );
        }

        if (
                valor == null ||
                valor.isBlank()
        ) {

            valor =
                    element.getAttribute(
                            atributo
                    );
        }

        return valor;
    }

    private String obtenerValorLiteral(
            Element padre,
            String nombreElemento
    ) {

        Element literal =
                primerHijo(
                        padre,
                        nombreElemento
                );

        if (
                literal == null
        ) {
            return null;
        }

        String value =
                literal.getAttribute(
                        "value"
                );

        if (
                value == null ||
                value.isBlank()
        ) {
            return null;
        }

        return value;
    }

    /*
     * ====================================================================
     * UML
     * ====================================================================
     */

    private boolean esElementoUml(
            String tipo
    ) {

        return
                "uml:Class".equals(
                        tipo
                ) ||
                "uml:AssociationClass".equals(
                        tipo
                ) ||
                "uml:Interface".equals(
                        tipo
                ) ||
                "uml:Enumeration".equals(
                        tipo
                );
    }

    private String convertirTipoElemento(
            String tipo
    ) {

        return switch (
                tipo
        ) {

            case "uml:Interface" ->
                    "INTERFACE";

            case "uml:Enumeration" ->
                    "ENUM";

            default ->
                    "CLASE";
        };
    }

    private String convertirVisibilidad(
            String visibilidad
    ) {

        if (
                visibilidad == null ||
                visibilidad.isBlank()
        ) {
            return "PUBLIC";
        }

        return switch (
                visibilidad.toLowerCase()
        ) {

            case "private" ->
                    "PRIVATE";

            case "protected" ->
                    "PROTECTED";

            case "package" ->
                    "PACKAGE";

            default ->
                    "PUBLIC";
        };
    }

    private String combinarMultiplicidad(
            String lower,
            String upper
    ) {

        if (
                lower == null ||
                lower.isBlank()
        ) {
            lower = "1";
        }

        if (
                upper == null ||
                upper.isBlank()
        ) {
            upper = "1";
        }

        /*
         * XMI usa -1 para unlimited.
         */
        if (
                "-1".equals(
                        upper
                )
        ) {
            upper = "*";
        }

        if (
                lower.equals(
                        upper
                )
        ) {

            return lower;
        }

        return lower +
                ".." +
                upper;
    }
}