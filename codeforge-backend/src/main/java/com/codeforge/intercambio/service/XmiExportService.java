package com.codeforge.intercambio.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.codeforge.modelo.entity.Modelo;
import com.codeforge.modelo.repository.ModeloRepository;

@Service
public class XmiExportService {

    private static final String XMI_NS =
            "http://schema.omg.org/spec/XMI/2.1";

    private static final String UML_NS =
            "http://schema.omg.org/spec/UML/2.1";

    private final ModeloRepository modeloRepository;

    public XmiExportService(
            ModeloRepository modeloRepository
    ) {
        this.modeloRepository = modeloRepository;
    }

    public byte[] exportar(Long idModelo) {

        Modelo modelo = modeloRepository
                .findById(idModelo)
                .orElseThrow(
                        () -> new RuntimeException(
                                "El modelo no existe."
                        )
                );

        Map<String, Object> contenido =
                modelo.getContenidoJson();

        try {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            Document document =
                    factory.newDocumentBuilder()
                            .newDocument();

            /*
             * ============================================================
             * XMI
             * ============================================================
             */

            Element root =
                    document.createElementNS(
                            XMI_NS,
                            "xmi:XMI"
                    );

            root.setAttributeNS(
                    "http://www.w3.org/2000/xmlns/",
                    "xmlns:xmi",
                    XMI_NS
            );

            root.setAttributeNS(
                    "http://www.w3.org/2000/xmlns/",
                    "xmlns:uml",
                    UML_NS
            );

            root.setAttributeNS(
                    XMI_NS,
                    "xmi:version",
                    "2.1"
            );

            document.appendChild(root);

            /*
             * ============================================================
             * DOCUMENTACIÓN
             * ============================================================
             */

            Element documentation =
                    document.createElementNS(
                            XMI_NS,
                            "xmi:Documentation"
                    );

            documentation.setAttribute(
                    "exporter",
                    "CodeForge"
            );

            documentation.setAttribute(
                    "exporterVersion",
                    "1.0"
            );

            root.appendChild(
                    documentation
            );

            /*
             * ============================================================
             * MODELO UML
             * ============================================================
             */

            Element umlModel =
                    document.createElementNS(
                            UML_NS,
                            "uml:Model"
                    );

            umlModel.setAttributeNS(
                    XMI_NS,
                    "xmi:type",
                    "uml:Model"
            );

            umlModel.setAttributeNS(
                    XMI_NS,
                    "xmi:id",
                    "model-" + idModelo
            );

            umlModel.setAttribute(
                    "name",
                    obtenerNombreModelo(modelo)
            );

            umlModel.setAttribute(
                    "visibility",
                    "public"
            );

            root.appendChild(
                    umlModel
            );

            /*
             * ============================================================
             * PACKAGE
             * ============================================================
             */

            Element paquete =
                    document.createElementNS(
                            UML_NS,
                            "packagedElement"
                    );

            paquete.setAttributeNS(
                    XMI_NS,
                    "xmi:type",
                    "uml:Package"
            );

            paquete.setAttributeNS(
                    XMI_NS,
                    "xmi:id",
                    "package-" + idModelo
            );

            paquete.setAttribute(
                    "name",
                    obtenerNombreModelo(modelo)
            );

            paquete.setAttribute(
                    "visibility",
                    "public"
            );

            umlModel.appendChild(
                    paquete
            );

            /*
             * ============================================================
             * MAPA DE IDS
             * ============================================================
             */

            Map<String, String> idsXmi =
                    new HashMap<>();

            List<Map<String, Object>> elementos =
                    obtenerLista(
                            contenido.get("elementos")
                    );

            /*
             * ============================================================
             * TIPOS PRIMITIVOS
             * ============================================================
             */

            agregarTiposPrimitivos(
                    document,
                    paquete
            );

            /*
             * ============================================================
             * ELEMENTOS UML
             * ============================================================
             */

            for (
                    Map<String, Object> elemento :
                    elementos
            ) {

                String idOriginal =
                        texto(
                                elemento.get("id")
                        );

                String tipo =
                        texto(
                                elemento.get("tipo")
                        );

                String nombre =
                        texto(
                                elemento.get("nombre")
                        );

                String idXmi =
                        "element-" +
                        normalizarId(
                                idOriginal
                        );

                idsXmi.put(
                        idOriginal,
                        idXmi
                );

                String tipoXmi;

                switch (tipo) {

                    case "INTERFACE":
                        tipoXmi =
                                "uml:Interface";
                        break;

                    case "ENUM":
                        tipoXmi =
                                "uml:Enumeration";
                        break;

                    default:
                        tipoXmi =
                                "uml:Class";
                        break;
                }

                Element elementoXmi =
                        document.createElementNS(
                                UML_NS,
                                "packagedElement"
                        );

                elementoXmi.setAttributeNS(
                        XMI_NS,
                        "xmi:type",
                        tipoXmi
                );

                elementoXmi.setAttributeNS(
                        XMI_NS,
                        "xmi:id",
                        idXmi
                );

                elementoXmi.setAttribute(
                        "name",
                        nombre
                );

                elementoXmi.setAttribute(
                        "visibility",
                        convertirVisibilidad(
                                texto(
                                        elemento.get(
                                                "visibilidad"
                                        )
                                )
                        )
                );

                if (
                        booleano(
                                elemento.get(
                                        "abstracto"
                                )
                        )
                ) {

                    elementoXmi.setAttribute(
                            "isAbstract",
                            "true"
                    );
                }

                paquete.appendChild(
                        elementoXmi
                );

                /*
                 * ========================================================
                 * ATRIBUTOS
                 * ========================================================
                 */

                List<Map<String, Object>> atributos =
                        obtenerLista(
                                elemento.get(
                                        "atributos"
                                )
                        );

                for (
                        Map<String, Object> atributo :
                        atributos
                ) {

                    Element propiedad =
                            document.createElementNS(
                                    UML_NS,
                                    "ownedAttribute"
                            );

                    propiedad.setAttributeNS(
                            XMI_NS,
                            "xmi:type",
                            "uml:Property"
                    );

                    String atributoId =
                            texto(
                                    atributo.get(
                                            "id"
                                    )
                            );

                    propiedad.setAttributeNS(
                            XMI_NS,
                            "xmi:id",
                            "attribute-" +
                                    normalizarId(
                                            atributoId
                                    )
                    );

                    propiedad.setAttribute(
                            "name",
                            texto(
                                    atributo.get(
                                            "nombre"
                                    )
                            )
                    );

                    propiedad.setAttribute(
                            "visibility",
                            convertirVisibilidad(
                                    texto(
                                            atributo.get(
                                                    "visibilidad"
                                            )
                                    )
                            )
                    );

                    String tipoAtributo =
                            texto(
                                    atributo.get(
                                            "tipo"
                                    )
                            );

                    propiedad.setAttribute(
                            "type",
                            "primitive-" +
                                    normalizarId(
                                            tipoAtributo
                                    )
                    );

                    if (
                            booleano(
                                    atributo.get(
                                            "estatico"
                                    )
                            )
                    ) {

                        propiedad.setAttribute(
                                "isStatic",
                                "true"
                        );
                    }

                    if (
                            booleano(
                                    atributo.get(
                                            "derivado"
                                    )
                            )
                    ) {

                        propiedad.setAttribute(
                                "isDerived",
                                "true"
                        );
                    }

                    propiedad.setAttribute(
                            "lower",
                            convertirLimiteInferior(
                                    texto(
                                            atributo.get(
                                                    "multiplicidad"
                                            )
                                    )
                            )
                    );

                    propiedad.setAttribute(
                            "upper",
                            convertirLimiteSuperior(
                                    texto(
                                            atributo.get(
                                                    "multiplicidad"
                                            )
                                    )
                            )
                    );

                    elementoXmi.appendChild(
                            propiedad
                    );
                }

                /*
                 * ========================================================
                 * OPERACIONES
                 * ========================================================
                 */

                List<Map<String, Object>> operaciones =
                        obtenerLista(
                                elemento.get(
                                        "operaciones"
                                )
                        );

                for (
                        Map<String, Object> operacion :
                        operaciones
                ) {

                    Element ownedOperation =
                            document.createElementNS(
                                    UML_NS,
                                    "ownedOperation"
                            );

                    ownedOperation.setAttributeNS(
                            XMI_NS,
                            "xmi:type",
                            "uml:Operation"
                    );

                    String operacionId =
                            texto(
                                    operacion.get(
                                            "id"
                                    )
                            );

                    ownedOperation.setAttributeNS(
                            XMI_NS,
                            "xmi:id",
                            "operation-" +
                                    normalizarId(
                                            operacionId
                                    )
                    );

                    ownedOperation.setAttribute(
                            "name",
                            texto(
                                    operacion.get(
                                            "nombre"
                                    )
                            )
                    );

                    ownedOperation.setAttribute(
                            "visibility",
                            convertirVisibilidad(
                                    texto(
                                            operacion.get(
                                                    "visibilidad"
                                            )
                                    )
                            )
                    );

                    List<Map<String, Object>> parametros =
                            obtenerLista(
                                    operacion.get(
                                            "parametros"
                                    )
                            );

                    for (
                            Map<String, Object> parametro :
                            parametros
                    ) {

                        Element ownedParameter =
                                document.createElementNS(
                                        UML_NS,
                                        "ownedParameter"
                                );

                        ownedParameter.setAttributeNS(
                                XMI_NS,
                                "xmi:type",
                                "uml:Parameter"
                        );

                        String parametroId =
                                texto(
                                        parametro.get(
                                                "id"
                                        )
                                );

                        ownedParameter.setAttributeNS(
                                XMI_NS,
                                "xmi:id",
                                "parameter-" +
                                        normalizarId(
                                                parametroId
                                        )
                        );

                        ownedParameter.setAttribute(
                                "name",
                                texto(
                                        parametro.get(
                                                "nombre"
                                        )
                                )
                        );

                        ownedParameter.setAttribute(
                                "type",
                                "primitive-" +
                                        normalizarId(
                                                texto(
                                                        parametro.get(
                                                                "tipo"
                                                        )
                                                )
                                        )
                        );

                        String direccion =
                                texto(
                                        parametro.get(
                                                "direccion"
                                        )
                                );

                        if (
                                direccion == null ||
                                direccion.isBlank()
                        ) {

                            direccion =
                                    "in";
                        }

                        ownedParameter.setAttribute(
                                "direction",
                                direccion
                        );

                        ownedOperation.appendChild(
                                ownedParameter
                        );
                    }

                    elementoXmi.appendChild(
                            ownedOperation
                    );
                }

                /*
                 * ========================================================
                 * ENUMERACIONES
                 * ========================================================
                 */

                if ("ENUM".equals(tipo)) {

                    List<Map<String, Object>> literales =
                            obtenerLista(
                                    elemento.get(
                                            "literales"
                                    )
                            );

                    for (
                            Map<String, Object> literal :
                            literales
                    ) {

                        Element ownedLiteral =
                                document.createElementNS(
                                        UML_NS,
                                        "ownedLiteral"
                                );

                        ownedLiteral.setAttributeNS(
                                XMI_NS,
                                "xmi:type",
                                "uml:EnumerationLiteral"
                        );

                        String literalId =
                                texto(
                                        literal.get(
                                                "id"
                                        )
                                );

                        ownedLiteral.setAttributeNS(
                                XMI_NS,
                                "xmi:id",
                                "literal-" +
                                        normalizarId(
                                                literalId
                                        )
                        );

                        ownedLiteral.setAttribute(
                                "name",
                                texto(
                                        literal.get(
                                                "nombre"
                                        )
                                )
                        );

                        elementoXmi.appendChild(
                                ownedLiteral
                        );
                    }
                }
            }

            /*
             * ============================================================
             * RELACIONES
             * ============================================================
             */

            List<Map<String, Object>> relaciones =
                    obtenerLista(
                            contenido.get(
                                    "relaciones"
                            )
                    );

            for (
                    Map<String, Object> relacion :
                    relaciones
            ) {

                String tipo =
                        texto(
                                relacion.get(
                                        "tipo"
                                )
                        );

                String origen =
                        texto(
                                relacion.get(
                                        "origen"
                                )
                        );

                String destino =
                        texto(
                                relacion.get(
                                        "destino"
                                )
                        );

                String origenXmi =
                        idsXmi.get(
                                origen
                        );

                String destinoXmi =
                        idsXmi.get(
                                destino
                        );

                if (
                        origenXmi == null ||
                        destinoXmi == null
                ) {
                    continue;
                }

                /*
                 * ========================================================
                 * GENERALIZACIÓN
                 * ========================================================
                 */

                if (
                        "GENERALIZACION".equals(
                                tipo
                        )
                ) {

                    Element origenElement =
                            buscarElemento(
                                    paquete,
                                    origenXmi
                            );

                    if (
                            origenElement != null
                    ) {

                        Element generalization =
                                document.createElementNS(
                                        UML_NS,
                                        "generalization"
                                );

                        generalization.setAttributeNS(
                                XMI_NS,
                                "xmi:type",
                                "uml:Generalization"
                        );

                        generalization.setAttributeNS(
                                XMI_NS,
                                "xmi:id",
                                "generalization-" +
                                        normalizarId(
                                                texto(
                                                        relacion.get(
                                                                "id"
                                                        )
                                                )
                                        )
                        );

                        generalization.setAttribute(
                                "general",
                                destinoXmi
                        );

                        origenElement.appendChild(
                                generalization
                        );
                    }

                    continue;
                }

                /*
                 * ========================================================
                 * REALIZACIÓN
                 * ========================================================
                 */

                if (
                        "REALIZACION".equals(
                                tipo
                        )
                ) {

                    Element origenElement =
                            buscarElemento(
                                    paquete,
                                    origenXmi
                            );

                    if (
                            origenElement != null
                    ) {

                        Element realization =
                                document.createElementNS(
                                        UML_NS,
                                        "interfaceRealization"
                                );

                        realization.setAttributeNS(
                                XMI_NS,
                                "xmi:type",
                                "uml:InterfaceRealization"
                        );

                        realization.setAttributeNS(
                                XMI_NS,
                                "xmi:id",
                                "realization-" +
                                        normalizarId(
                                                texto(
                                                        relacion.get(
                                                                "id"
                                                        )
                                                )
                                        )
                        );

                        realization.setAttribute(
                                "contract",
                                destinoXmi
                        );

                        origenElement.appendChild(
                                realization
                        );
                    }

                    continue;
                }

                /*
                 * ========================================================
                 * DEPENDENCIA
                 * ========================================================
                 */

                if (
                        "DEPENDENCIA".equals(
                                tipo
                        )
                ) {

                    Element dependency =
                            document.createElementNS(
                                    UML_NS,
                                    "packagedElement"
                            );

                    dependency.setAttributeNS(
                            XMI_NS,
                            "xmi:type",
                            "uml:Dependency"
                    );

                    dependency.setAttributeNS(
                            XMI_NS,
                            "xmi:id",
                            "dependency-" +
                                    normalizarId(
                                            texto(
                                                    relacion.get(
                                                            "id"
                                                    )
                                            )
                                    )
                    );

                    dependency.setAttribute(
                            "client",
                            origenXmi
                    );

                    dependency.setAttribute(
                            "supplier",
                            destinoXmi
                    );

                    paquete.appendChild(
                            dependency
                    );

                    continue;
                }

                /*
                 * ========================================================
                 * ASOCIACIÓN / AGREGACIÓN / COMPOSICIÓN
                 * ========================================================
                 */

                Element association =
                        document.createElementNS(
                                UML_NS,
                                "packagedElement"
                        );

                association.setAttributeNS(
                        XMI_NS,
                        "xmi:type",
                        "uml:Association"
                );

                association.setAttributeNS(
                        XMI_NS,
                        "xmi:id",
                        "association-" +
                                normalizarId(
                                        texto(
                                                relacion.get(
                                                        "id"
                                                )
                                        )
                                )
                );

                String end1Id =
                        "end-" +
                                normalizarId(
                                        texto(
                                                relacion.get(
                                                        "id"
                                                )
                                        )
                                ) +
                                "-1";

                String end2Id =
                        "end-" +
                                normalizarId(
                                        texto(
                                                relacion.get(
                                                        "id"
                                                )
                                        )
                                ) +
                                "-2";

                association.setAttribute(
                        "memberEnd",
                        end1Id +
                                " " +
                                end2Id
                );

                /*
                 * EXTREMO ORIGEN
                 */

                Element end1 =
                        crearAssociationEnd(
                                document,
                                end1Id,
                                origenXmi,
                                texto(
                                        relacion.get(
                                                "rolOrigen"
                                        )
                                ),
                                texto(
                                        relacion.get(
                                                "multiplicidadOrigen"
                                        )
                                ),
                                obtenerAgregacion(
                                        tipo
                                )
                        );

                /*
                 * EXTREMO DESTINO
                 */

                Element end2 =
                        crearAssociationEnd(
                                document,
                                end2Id,
                                destinoXmi,
                                texto(
                                        relacion.get(
                                                "rolDestino"
                                        )
                                ),
                                texto(
                                        relacion.get(
                                                "multiplicidadDestino"
                                        )
                                ),
                                obtenerAgregacion(
                                        tipo
                                )
                        );

                association.appendChild(
                        end1
                );

                association.appendChild(
                        end2
                );

                paquete.appendChild(
                        association
                );
            }

            /*
             * ============================================================
             * SERIALIZACIÓN
             * ============================================================
             */

            TransformerFactory transformerFactory =
                    TransformerFactory.newInstance();

            Transformer transformer =
                    transformerFactory.newTransformer();

            transformer.setOutputProperty(
                    OutputKeys.INDENT,
                    "yes"
            );

            transformer.setOutputProperty(
                    OutputKeys.ENCODING,
                    "UTF-8"
            );

            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount",
                    "2"
            );

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            transformer.transform(
                    new DOMSource(document),
                    new StreamResult(output)
            );

            return output.toString(
                    StandardCharsets.UTF_8
            ).getBytes(
                    StandardCharsets.UTF_8
            );

        } catch (
                ParserConfigurationException |
                javax.xml.transform.TransformerException e
        ) {

            throw new RuntimeException(
                    "No se pudo generar el archivo XMI.",
                    e
            );
        }
    }

    private Element crearAssociationEnd(
            Document document,
            String id,
            String tipo,
            String nombre,
            String multiplicidad,
            String aggregation
    ) {

        Element end =
                document.createElementNS(
                        UML_NS,
                        "ownedEnd"
                );

        end.setAttributeNS(
                XMI_NS,
                "xmi:type",
                "uml:Property"
        );

        end.setAttributeNS(
                XMI_NS,
                "xmi:id",
                id
        );

        end.setAttribute(
                "type",
                tipo
        );

        if (
                nombre != null &&
                !nombre.isBlank()
        ) {

            end.setAttribute(
                    "name",
                    nombre
            );
        }

        end.setAttribute(
                "lower",
                convertirLimiteInferior(
                        multiplicidad
                )
        );

        end.setAttribute(
                "upper",
                convertirLimiteSuperior(
                        multiplicidad
                )
        );

        if (
                aggregation != null
        ) {

            end.setAttribute(
                    "aggregation",
                    aggregation
            );
        }

        return end;
    }

    private void agregarTiposPrimitivos(
            Document document,
            Element paquete
    ) {

        String[] tipos = {
                "String",
                "Integer",
                "Long",
                "Double",
                "Float",
                "Boolean",
                "Date",
                "LocalDate",
                "LocalDateTime",
                "Time",
                "Object"
        };

        for (
                String tipo :
                tipos
        ) {

            Element primitive =
                    document.createElementNS(
                            UML_NS,
                            "packagedElement"
                    );

            primitive.setAttributeNS(
                    XMI_NS,
                    "xmi:type",
                    "uml:PrimitiveType"
            );

            primitive.setAttributeNS(
                    XMI_NS,
                    "xmi:id",
                    "primitive-" +
                            normalizarId(
                                    tipo
                            )
            );

            primitive.setAttribute(
                    "name",
                    tipo
            );

            primitive.setAttribute(
                    "visibility",
                    "public"
            );

            paquete.appendChild(
                    primitive
            );
        }
    }

    private Element buscarElemento(
            Element paquete,
            String xmiId
    ) {

        var children =
                paquete.getChildNodes();

        for (
                int i = 0;
                i < children.getLength();
                i++
        ) {

            if (
                    !(children.item(i)
                            instanceof Element)
            ) {
                continue;
            }

            Element element =
                    (Element)
                            children.item(i);

            String id =
                    element.getAttributeNS(
                            XMI_NS,
                            "id"
                    );

            if (
                    xmiId.equals(id)
            ) {

                return element;
            }
        }

        return null;
    }

    private List<Map<String, Object>> obtenerLista(
            Object valor
    ) {

        if (
                valor instanceof List<?> lista
        ) {

            return lista.stream()
                    .filter(
                            item ->
                                    item instanceof Map<?, ?>
                    )
                    .map(
                            item ->
                                    (Map<String, Object>)
                                            item
                    )
                    .toList();
        }

        return List.of();
    }

    private String texto(
            Object valor
    ) {

        if (
                valor == null
        ) {
            return "";
        }

        return String.valueOf(
                valor
        );
    }

    private boolean booleano(
            Object valor
    ) {

        return Boolean.TRUE.equals(
                valor
        );
    }

    private String normalizarId(
            String valor
    ) {

        if (
                valor == null ||
                valor.isBlank()
        ) {

            return "sin-id";
        }

        return valor.replaceAll(
                "[^a-zA-Z0-9_\\-]",
                "_"
        );
    }

    private String convertirVisibilidad(
            String visibilidad
    ) {

        if (
                visibilidad == null
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

            case "PACKAGE" ->
                    "package";

            default ->
                    "public";
        };
    }

    private String convertirLimiteInferior(
            String multiplicidad
    ) {

        if (
                multiplicidad == null ||
                multiplicidad.isBlank()
        ) {
            return "1";
        }

        if (
                multiplicidad.startsWith(
                        "0"
                )
        ) {
            return "0";
        }

        return "1";
    }

    private String convertirLimiteSuperior(
            String multiplicidad
    ) {

        if (
                multiplicidad == null ||
                multiplicidad.isBlank()
        ) {
            return "1";
        }

        if (
                multiplicidad.contains("*")
        ) {
            return "*";
        }

        if (
                multiplicidad.contains("..")
        ) {

            String[] partes =
                    multiplicidad.split(
                            "\\.\\."
                    );

            if (
                    partes.length == 2
            ) {
                return partes[1];
            }
        }

        return multiplicidad;
    }

    private String obtenerAgregacion(
            String tipo
    ) {

        return switch (tipo) {

            case "COMPOSICION" ->
                    "composite";

            case "AGREGACION" ->
                    "shared";

            default ->
                    null;
        };
    }

    private String obtenerNombreModelo(
            Modelo modelo
    ) {

        return "CodeForge_Model_" +
                modelo.getIdModelo();
    }
}