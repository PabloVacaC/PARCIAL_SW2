package com.codeforge.modelo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeforge.modelo.dto.AtributoCreateDTO;
import com.codeforge.modelo.dto.AtributoUpdateDTO;
import com.codeforge.modelo.dto.ElementoCreateDTO;
import com.codeforge.modelo.dto.ElementoUpdateDTO;
import com.codeforge.modelo.dto.ModeloContenidoDTO;
import com.codeforge.modelo.dto.ModeloCreateDTO;
import com.codeforge.modelo.dto.ModeloResponseDTO;
import com.codeforge.modelo.dto.OperacionCreateDTO;
import com.codeforge.modelo.dto.OperacionUpdateDTO;
import com.codeforge.modelo.dto.ParametroCreateDTO;
import com.codeforge.modelo.dto.ParametroUpdateDTO;
import com.codeforge.modelo.dto.RelacionCreateDTO;
import com.codeforge.modelo.dto.RelacionUpdateDTO;
import com.codeforge.modelo.entity.Modelo;
import com.codeforge.modelo.repository.ModeloRepository;
import com.codeforge.proyecto.entity.Proyecto;
import com.codeforge.proyecto.repository.ProyectoRepository;

@Service
public class ModeloService {

    private final ModeloRepository modeloRepository;

    private final ProyectoRepository proyectoRepository;


    public ModeloService(
            ModeloRepository modeloRepository,
            ProyectoRepository proyectoRepository
    ) {
        this.modeloRepository =
                modeloRepository;

        this.proyectoRepository =
                proyectoRepository;
    }


    // =========================================================
    // JSON UML INICIAL
    // =========================================================

    private Map<String, Object> crearJsonInicial() {

        Map<String, Object> json =
                new LinkedHashMap<>();

        json.put(
                "versionUML",
                "2.5"
        );

        json.put(
                "elementos",
                new ArrayList<>()
        );

        json.put(
                "relaciones",
                new ArrayList<>()
        );

        return json;
    }


    // =========================================================
    // CREAR MODELO
    // =========================================================

    @Transactional
    public ModeloResponseDTO crearModelo(
            ModeloCreateDTO dto
    ) {

        Proyecto proyecto =
                proyectoRepository
                        .findById(
                                dto.getIdProyecto()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El proyecto no existe"
                                )
                        );


        Modelo modelo =
                new Modelo();


        modelo.setProyecto(
                proyecto
        );


        modelo.setNombre(
                dto.getNombre().trim()
        );


        if (
                dto.getDescripcion() != null
                &&
                !dto.getDescripcion().isBlank()
        ) {

            modelo.setDescripcion(
                    dto.getDescripcion().trim()
            );

        } else {

            modelo.setDescripcion(
                    null
            );
        }


        modelo.setVersion(
                "1.0"
        );


        modelo.setContenidoJson(
                crearJsonInicial()
        );


        LocalDateTime ahora =
                LocalDateTime.now();


        modelo.setFechaCreacion(
                ahora
        );


        modelo.setFechaActualizacion(
                ahora
        );


        Modelo guardado =
                modeloRepository.save(
                        modelo
                );


        return convertirDTO(
                guardado
        );
    }


    // =========================================================
    // OBTENER MODELOS DEL PROYECTO
    // =========================================================

    @Transactional(readOnly = true)
    public List<ModeloResponseDTO>
    obtenerPorProyecto(
            Long idProyecto
    ) {

        proyectoRepository
                .findById(
                        idProyecto
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "El proyecto no existe"
                        )
                );


        return modeloRepository
                .findByProyecto_IdProyecto(
                        idProyecto
                )
                .stream()
                .map(this::convertirDTO)
                .toList();
    }


    // =========================================================
    // OBTENER MODELO POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public ModeloResponseDTO
    obtenerPorId(
            Long idModelo
    ) {

        Modelo modelo =
                modeloRepository
                        .findById(
                                idModelo
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El modelo no existe"
                                )
                        );


        return convertirDTO(
                modelo
        );
    }


    // =========================================================
    // ACTUALIZAR TODO EL JSON
    // =========================================================

    @Transactional
    public ModeloResponseDTO
    actualizarContenido(
            Long idModelo,
            ModeloContenidoDTO dto
    ) {

        Modelo modelo =
                modeloRepository
                        .findById(
                                idModelo
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El modelo no existe"
                                )
                        );


        Map<String, Object> contenido =
                dto.getContenidoJson();


        if (contenido == null) {

            throw new RuntimeException(
                    "El contenido JSON es obligatorio"
            );
        }


        validarContenidoUML(
                contenido
        );


        modelo.setContenidoJson(
                contenido
        );


        modelo.setFechaActualizacion(
                LocalDateTime.now()
        );


        actualizarVersion(
                modelo
        );


        Modelo guardado =
                modeloRepository.save(
                        modelo
                );


        return convertirDTO(
                guardado
        );
    }


    // =========================================================
    // OBTENER SOLO EL JSON
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Object>
    obtenerContenido(
            Long idModelo
    ) {

        Modelo modelo =
                modeloRepository
                        .findById(
                                idModelo
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El modelo no existe"
                                )
                        );


        return modelo.getContenidoJson();
    }


    // =========================================================
    // AGREGAR ELEMENTO UML
    // =========================================================

    @Transactional
    public ModeloResponseDTO
    agregarElemento(
            Long idModelo,
            ElementoCreateDTO dto
    ) {

        Modelo modelo =
                modeloRepository
                        .findById(
                                idModelo
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El modelo no existe"
                                )
                        );


        Map<String, Object> contenido =
                modelo.getContenidoJson();


        if (contenido == null) {

            contenido =
                    crearJsonInicial();

        }


        validarContenidoUML(
                contenido
        );


        String tipo =
                dto.getTipo()
                        .trim()
                        .toUpperCase();


        if (
                !tipo.equals("CLASE")
                &&
                !tipo.equals("INTERFACE")
                &&
                !tipo.equals("ENUM")
        ) {

            throw new RuntimeException(
                    "Tipo UML no válido. " +
                    "Use CLASE, INTERFACE o ENUM."
            );
        }


        String nombre =
                dto.getNombre()
                        .trim();


        if (nombre.isBlank()) {

            throw new RuntimeException(
                    "El nombre del elemento es obligatorio"
            );
        }


        @SuppressWarnings("unchecked")
        List<Map<String, Object>> elementos =
                (List<Map<String, Object>>)
                        contenido.get("elementos");


        boolean nombreExiste =
                elementos.stream()
                        .anyMatch(elemento ->
                                nombre.equalsIgnoreCase(
                                        String.valueOf(
                                                elemento.get("nombre")
                                        )
                                )
                        );


        if (nombreExiste) {

            throw new RuntimeException(
                    "Ya existe un elemento con el nombre: "
                    + nombre
            );
        }


        Map<String, Object> elemento =
                new LinkedHashMap<>();


        elemento.put(
                "id",
                UUID.randomUUID()
                        .toString()
        );


        elemento.put(
                "tipo",
                tipo
        );


        elemento.put(
                "nombre",
                nombre
        );


        elemento.put(
                "visibilidad",
                dto.getVisibilidad() != null
                        &&
                        !dto.getVisibilidad().isBlank()
                        ?
                        dto.getVisibilidad()
                                .trim()
                                .toUpperCase()
                        :
                        "PUBLIC"
        );


        elemento.put(
                "estereotipo",
                dto.getEstereotipo() != null
                        &&
                        !dto.getEstereotipo().isBlank()
                        ?
                        dto.getEstereotipo().trim()
                        :
                        null
        );


        elemento.put(
                "abstracto",
                dto.getAbstracto() != null
                        ?
                        dto.getAbstracto()
                        :
                        false
        );


        elemento.put(
                "final",
                dto.getFinalElemento() != null
                        ?
                        dto.getFinalElemento()
                        :
                        false
        );


        Map<String, Object> posicion =
                new LinkedHashMap<>();


        posicion.put(
                "x",
                dto.getPosicionX() != null
                        ?
                        dto.getPosicionX()
                        :
                        100
        );


        posicion.put(
                "y",
                dto.getPosicionY() != null
                        ?
                        dto.getPosicionY()
                        :
                        100
        );


        elemento.put(
                "posicion",
                posicion
        );


        Map<String, Object> tamano =
                new LinkedHashMap<>();


        tamano.put(
                "ancho",
                dto.getAncho() != null
                        ?
                        dto.getAncho()
                        :
                        240
        );


        tamano.put(
                "alto",
                dto.getAlto() != null
                        ?
                        dto.getAlto()
                        :
                        180
        );


        elemento.put(
                "tamaño",
                tamano
        );


        elemento.put(
                "atributos",
                new ArrayList<>()
        );


        elemento.put(
                "operaciones",
                new ArrayList<>()
        );


        elementos.add(
                elemento
        );


        modelo.setContenidoJson(
                contenido
        );


        modelo.setFechaActualizacion(
                LocalDateTime.now()
        );


        actualizarVersion(
                modelo
        );


        Modelo guardado =
                modeloRepository.save(
                        modelo
                );


        return convertirDTO(
                guardado
        );
    }


    // =========================================================
    // VALIDAR ESTRUCTURA UML
    // =========================================================

    private void validarContenidoUML(
            Map<String, Object> contenido
    ) {

        if (!contenido.containsKey("versionUML")) {

            contenido.put(
                    "versionUML",
                    "2.5"
            );
        }


        if (!contenido.containsKey("elementos")) {

            contenido.put(
                    "elementos",
                    new ArrayList<>()
            );
        }


        if (!contenido.containsKey("relaciones")) {

            contenido.put(
                    "relaciones",
                    new ArrayList<>()
            );
        }


        Object elementos =
                contenido.get("elementos");


        if (!(elementos instanceof List<?>)) {

            throw new RuntimeException(
                    "El campo 'elementos' debe ser un arreglo"
            );
        }


        Object relaciones =
                contenido.get("relaciones");


        if (!(relaciones instanceof List<?>)) {

            throw new RuntimeException(
                    "El campo 'relaciones' debe ser un arreglo"
            );
        }
    }


    // =========================================================
    // ACTUALIZAR VERSION
    // =========================================================

    private void actualizarVersion(
            Modelo modelo
    ) {

        String versionActual =
                modelo.getVersion();


        if (
                versionActual == null
                ||
                versionActual.isBlank()
        ) {

            modelo.setVersion(
                    "1.0"
            );

            return;
        }


        String[] partes =
                versionActual.split("\\.");


        try {

            int mayor =
                    Integer.parseInt(
                            partes[0]
                    );


            int menor =
                    partes.length > 1
                            ?
                            Integer.parseInt(
                                    partes[1]
                            )
                            :
                            0;


            menor++;


            modelo.setVersion(
                    mayor + "." + menor
            );

        } catch (NumberFormatException ex) {

            modelo.setVersion(
                    "1.1"
            );
        }
    }


    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private ModeloResponseDTO
    convertirDTO(
            Modelo modelo
    ) {

        return new ModeloResponseDTO(

                modelo.getIdModelo(),

                modelo.getProyecto()
                        .getIdProyecto(),

                modelo.getNombre(),

                modelo.getDescripcion(),

                modelo.getVersion(),

                modelo.getContenidoJson(),

                modelo.getFechaCreacion(),

                modelo.getFechaActualizacion()
        );
    }
    public List<Map<String, Object>> obtenerElementos(Long idModelo) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException("No existe el modelo con ID: " + idModelo));

    Map<String, Object> contenido = modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException("El modelo no tiene contenido JSON");
    }

    Object elementosObj = contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        return new ArrayList<>();
    }

    List<?> elementos = (List<?>) elementosObj;

    List<Map<String, Object>> resultado = new ArrayList<>();

    for (Object elementoObj : elementos) {

        if (elementoObj instanceof Map<?, ?> mapa) {

            @SuppressWarnings("unchecked")
            Map<String, Object> elemento =
                    (Map<String, Object>) mapa;

            resultado.add(elemento);
        }
    }

    return resultado;
} 
public Map<String, Object> obtenerElemento(Long idModelo, String idElemento) {

    List<Map<String, Object>> elementos = obtenerElementos(idModelo);

    return elementos.stream()
            .filter(elemento ->
                    idElemento.equals(
                            String.valueOf(elemento.get("id"))
                    )
            )
            .findFirst()
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el elemento con ID: " + idElemento
                    )
            );
}
public Map<String, Object> actualizarElemento(
        Long idModelo,
        String idElemento,
        ElementoUpdateDTO dto
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: " + idModelo
                    ));

    Map<String, Object> contenido = modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj = contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    Map<String, Object> elementoEncontrado = null;

    for (Map<String, Object> elemento : elementos) {

        if (idElemento.equals(
                String.valueOf(elemento.get("id"))
        )) {
            elementoEncontrado = elemento;
            break;
        }
    }

    if (elementoEncontrado == null) {
        throw new RuntimeException(
                "No existe el elemento con ID: " + idElemento
        );
    }

    String nombreAnterior =
            String.valueOf(elementoEncontrado.get("nombre"));

    if (dto.getNombre() != null
            && !dto.getNombre().trim().isEmpty()
            && !dto.getNombre().trim().equalsIgnoreCase(nombreAnterior)) {

        String nuevoNombre = dto.getNombre().trim();

        boolean nombreExiste = elementos.stream()
                .anyMatch(elemento -> {

                    String idActual =
                            String.valueOf(elemento.get("id"));

                    String nombreActual =
                            String.valueOf(elemento.get("nombre"));

                    return !idElemento.equals(idActual)
                            && nuevoNombre.equalsIgnoreCase(nombreActual);
                });

        if (nombreExiste) {
            throw new RuntimeException(
                    "Ya existe un elemento con el nombre: "
                            + nuevoNombre
            );
        }

        elementoEncontrado.put("nombre", nuevoNombre);
    }

    if (dto.getVisibilidad() != null) {
        elementoEncontrado.put(
                "visibilidad",
                dto.getVisibilidad()
        );
    }

    if (dto.getEstereotipo() != null) {
        elementoEncontrado.put(
                "estereotipo",
                dto.getEstereotipo()
        );
    }

    if (dto.getAbstracto() != null) {
        elementoEncontrado.put(
                "abstracto",
                dto.getAbstracto()
        );
    }

    if (dto.getFinalElemento() != null) {
        elementoEncontrado.put(
                "final",
                dto.getFinalElemento()
        );
    }

    Map<String, Object> posicion;

    Object posicionObj =
            elementoEncontrado.get("posicion");

    if (posicionObj instanceof Map<?, ?> mapa) {

        @SuppressWarnings("unchecked")
        Map<String, Object> posicionExistente =
                (Map<String, Object>) mapa;

        posicion = posicionExistente;

    } else {

        posicion = new LinkedHashMap<>();
        elementoEncontrado.put("posicion", posicion);
    }

    if (dto.getPosicionX() != null) {
        posicion.put("x", dto.getPosicionX());
    }

    if (dto.getPosicionY() != null) {
        posicion.put("y", dto.getPosicionY());
    }

    Map<String, Object> tamaño;

    Object tamañoObj =
            elementoEncontrado.get("tamaño");

    if (tamañoObj instanceof Map<?, ?> mapa) {

        @SuppressWarnings("unchecked")
        Map<String, Object> tamañoExistente =
                (Map<String, Object>) mapa;

        tamaño = tamañoExistente;

    } else {

        tamaño = new LinkedHashMap<>();
        elementoEncontrado.put("tamaño", tamaño);
    }

    if (dto.getAncho() != null) {
        tamaño.put("ancho", dto.getAncho());
    }

    if (dto.getAlto() != null) {
        tamaño.put("alto", dto.getAlto());
    }

    contenido.put("elementos", elementos);

    modelo.setContenidoJson(contenido);

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(LocalDateTime.now());

    modeloRepository.save(modelo);

    return elementoEncontrado;
}
public void eliminarElemento(
        Long idModelo,
        String idElemento
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: " + idModelo
                    ));

    Map<String, Object> contenido = modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj = contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    boolean eliminado = elementos.removeIf(
            elemento ->
                    idElemento.equals(
                            String.valueOf(elemento.get("id"))
                    )
    );

    if (!eliminado) {
        throw new RuntimeException(
                "No existe el elemento con ID: " + idElemento
        );
    }

    contenido.put("elementos", elementos);

    modelo.setContenidoJson(contenido);

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(LocalDateTime.now());

    modeloRepository.save(modelo);
}
public List<Map<String, Object>> obtenerAtributos(
        Long idModelo,
        String idElemento
) {

    Map<String, Object> elemento =
            obtenerElemento(idModelo, idElemento);

    Object atributosObj = elemento.get("atributos");

    if (!(atributosObj instanceof List<?>)) {
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> atributos =
            (List<Map<String, Object>>) atributosObj;

    return atributos;
}
public Map<String, Object> agregarAtributo(
        Long idModelo,
        String idElemento,
        AtributoCreateDTO dto
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: " + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    Map<String, Object> elementoEncontrado = null;

    for (Map<String, Object> elemento : elementos) {

        if (idElemento.equals(
                String.valueOf(elemento.get("id"))
        )) {
            elementoEncontrado = elemento;
            break;
        }
    }

    if (elementoEncontrado == null) {
        throw new RuntimeException(
                "No existe el elemento con ID: " + idElemento
        );
    }

    Object atributosObj =
            elementoEncontrado.get("atributos");

    List<Map<String, Object>> atributos;

    if (atributosObj instanceof List<?>) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista =
                (List<Map<String, Object>>) atributosObj;

        atributos = lista;

    } else {

        atributos = new ArrayList<>();

        elementoEncontrado.put(
                "atributos",
                atributos
        );
    }

    String nombre =
            dto.getNombre().trim();

    boolean nombreExiste = atributos.stream()
            .anyMatch(atributo ->
                    nombre.equalsIgnoreCase(
                            String.valueOf(
                                    atributo.get("nombre")
                            )
                    )
            );

    if (nombreExiste) {
        throw new RuntimeException(
                "Ya existe un atributo con el nombre: "
                        + nombre
        );
    }

    Map<String, Object> atributo =
            new LinkedHashMap<>();

    atributo.put(
            "id",
            UUID.randomUUID().toString()
    );

    atributo.put(
            "tipo",
            dto.getTipo().trim()
    );

    atributo.put(
            "nombre",
            nombre
    );

    atributo.put(
            "derivado",
            dto.getDerivado() != null
                    ? dto.getDerivado()
                    : false
    );

    atributo.put(
            "estatico",
            dto.getEstatico() != null
                    ? dto.getEstatico()
                    : false
    );

    atributo.put(
            "visibilidad",
            dto.getVisibilidad() != null
                    ? dto.getVisibilidad()
                    : "PRIVATE"
    );

    atributo.put(
            "valorInicial",
            dto.getValorInicial()
    );

    atributo.put(
            "multiplicidad",
            dto.getMultiplicidad() != null
                    ? dto.getMultiplicidad()
                    : "1"
    );

    atributos.add(atributo);

    elementoEncontrado.put(
            "atributos",
            atributos
    );

    contenido.put(
            "elementos",
            elementos
    );

    modelo.setContenidoJson(contenido);

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);

    return atributo;
}
public Map<String, Object> actualizarAtributo(
        Long idModelo,
        String idElemento,
        String idAtributo,
        AtributoUpdateDTO dto
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: " + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    Map<String, Object> elementoEncontrado = null;

    for (Map<String, Object> elemento : elementos) {

        if (idElemento.equals(
                String.valueOf(elemento.get("id"))
        )) {
            elementoEncontrado = elemento;
            break;
        }
    }

    if (elementoEncontrado == null) {
        throw new RuntimeException(
                "No existe el elemento con ID: " + idElemento
        );
    }

    Object atributosObj =
            elementoEncontrado.get("atributos");

    if (!(atributosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El elemento no contiene atributos"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> atributos =
            (List<Map<String, Object>>) atributosObj;

    Map<String, Object> atributoEncontrado = null;

    for (Map<String, Object> atributo : atributos) {

        if (idAtributo.equals(
                String.valueOf(atributo.get("id"))
        )) {

            atributoEncontrado = atributo;
            break;
        }
    }

    if (atributoEncontrado == null) {
        throw new RuntimeException(
                "No existe el atributo con ID: " + idAtributo
        );
    }

    if (dto.getNombre() != null
            && !dto.getNombre().trim().isEmpty()) {

        String nuevoNombre =
                dto.getNombre().trim();

        boolean nombreExiste = atributos.stream()
                .anyMatch(atributo -> {

                    String idActual =
                            String.valueOf(
                                    atributo.get("id")
                            );

                    String nombreActual =
                            String.valueOf(
                                    atributo.get("nombre")
                            );

                    return !idAtributo.equals(idActual)
                            && nuevoNombre.equalsIgnoreCase(
                                    nombreActual
                            );
                });

        if (nombreExiste) {
            throw new RuntimeException(
                    "Ya existe un atributo con el nombre: "
                            + nuevoNombre
            );
        }

        atributoEncontrado.put(
                "nombre",
                nuevoNombre
        );
    }

    if (dto.getTipo() != null
            && !dto.getTipo().trim().isEmpty()) {

        atributoEncontrado.put(
                "tipo",
                dto.getTipo().trim()
        );
    }

    if (dto.getVisibilidad() != null) {

        atributoEncontrado.put(
                "visibilidad",
                dto.getVisibilidad()
        );
    }

    if (dto.getMultiplicidad() != null) {

        atributoEncontrado.put(
                "multiplicidad",
                dto.getMultiplicidad()
        );
    }

    if (dto.getDerivado() != null) {

        atributoEncontrado.put(
                "derivado",
                dto.getDerivado()
        );
    }

    if (dto.getEstatico() != null) {

        atributoEncontrado.put(
                "estatico",
                dto.getEstatico()
        );
    }

    if (dto.getValorInicial() != null) {

        atributoEncontrado.put(
                "valorInicial",
                dto.getValorInicial()
        );
    }

    elementoEncontrado.put(
            "atributos",
            atributos
    );

    contenido.put(
            "elementos",
            elementos
    );

    modelo.setContenidoJson(contenido);

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);

    return atributoEncontrado;
}
public void eliminarAtributo(
        Long idModelo,
        String idElemento,
        String idAtributo
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: " + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    Map<String, Object> elementoEncontrado = null;

    for (Map<String, Object> elemento : elementos) {

        if (idElemento.equals(
                String.valueOf(elemento.get("id"))
        )) {
            elementoEncontrado = elemento;
            break;
        }
    }

    if (elementoEncontrado == null) {
        throw new RuntimeException(
                "No existe el elemento con ID: " + idElemento
        );
    }

    Object atributosObj =
            elementoEncontrado.get("atributos");

    if (!(atributosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El elemento no contiene atributos"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> atributos =
            (List<Map<String, Object>>) atributosObj;

    boolean eliminado = atributos.removeIf(
            atributo ->
                    idAtributo.equals(
                            String.valueOf(
                                    atributo.get("id")
                            )
                    )
    );

    if (!eliminado) {
        throw new RuntimeException(
                "No existe el atributo con ID: " + idAtributo
        );
    }

    elementoEncontrado.put(
            "atributos",
            atributos
    );

    contenido.put(
            "elementos",
            elementos
    );

    modelo.setContenidoJson(contenido);

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);
}
public List<Map<String, Object>> obtenerOperaciones(
        Long idModelo,
        String idElemento
) {

    Map<String, Object> elemento =
            obtenerElemento(idModelo, idElemento);

    Object operacionesObj =
            elemento.get("operaciones");

    if (!(operacionesObj instanceof List<?>)) {
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> operaciones =
            (List<Map<String, Object>>) operacionesObj;

    return operaciones;
}
public Map<String, Object> agregarOperacion(
        Long idModelo,
        String idElemento,
        OperacionCreateDTO dto
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: " + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    Map<String, Object> elementoEncontrado = null;

    for (Map<String, Object> elemento : elementos) {

        if (idElemento.equals(
                String.valueOf(elemento.get("id"))
        )) {
            elementoEncontrado = elemento;
            break;
        }
    }

    if (elementoEncontrado == null) {
        throw new RuntimeException(
                "No existe el elemento con ID: " + idElemento
        );
    }

    Object operacionesObj =
            elementoEncontrado.get("operaciones");

    List<Map<String, Object>> operaciones;

    if (operacionesObj instanceof List<?>) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista =
                (List<Map<String, Object>>) operacionesObj;

        operaciones = lista;

    } else {

        operaciones = new ArrayList<>();

        elementoEncontrado.put(
                "operaciones",
                operaciones
        );
    }

    String nombre =
            dto.getNombre().trim();

    boolean nombreExiste = operaciones.stream()
            .anyMatch(operacion ->
                    nombre.equalsIgnoreCase(
                            String.valueOf(
                                    operacion.get("nombre")
                            )
                    )
            );

    if (nombreExiste) {
        throw new RuntimeException(
                "Ya existe una operación con el nombre: "
                        + nombre
        );
    }

    Map<String, Object> operacion =
            new LinkedHashMap<>();

    operacion.put(
            "id",
            UUID.randomUUID().toString()
    );

    operacion.put(
            "nombre",
            nombre
    );

    operacion.put(
            "estatica",
            dto.getEstatica() != null
                    ? dto.getEstatica()
                    : false
    );

    operacion.put(
            "abstracta",
            dto.getAbstracta() != null
                    ? dto.getAbstracta()
                    : false
    );

    operacion.put(
            "parametros",
            new ArrayList<Map<String, Object>>()
    );

    operacion.put(
            "tipoRetorno",
            dto.getTipoRetorno() != null
                    && !dto.getTipoRetorno().trim().isEmpty()
                    ? dto.getTipoRetorno().trim()
                    : "void"
    );

    operacion.put(
            "visibilidad",
            dto.getVisibilidad() != null
                    ? dto.getVisibilidad()
                    : "PUBLIC"
    );

    operaciones.add(operacion);

    elementoEncontrado.put(
            "operaciones",
            operaciones
    );

    contenido.put(
            "elementos",
            elementos
    );

    modelo.setContenidoJson(contenido);

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);

    return operacion;
}
public Map<String, Object> actualizarOperacion(
        Long idModelo,
        String idElemento,
        String idOperacion,
        OperacionUpdateDTO dto
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: " + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    Map<String, Object> elementoEncontrado = null;

    for (Map<String, Object> elemento : elementos) {

        if (idElemento.equals(
                String.valueOf(elemento.get("id"))
        )) {
            elementoEncontrado = elemento;
            break;
        }
    }

    if (elementoEncontrado == null) {
        throw new RuntimeException(
                "No existe el elemento con ID: " + idElemento
        );
    }

    Object operacionesObj =
            elementoEncontrado.get("operaciones");

    if (!(operacionesObj instanceof List<?>)) {
        throw new RuntimeException(
                "El elemento no contiene operaciones"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> operaciones =
            (List<Map<String, Object>>) operacionesObj;

    Map<String, Object> operacionEncontrada = null;

    for (Map<String, Object> operacion : operaciones) {

        if (idOperacion.equals(
                String.valueOf(operacion.get("id"))
        )) {

            operacionEncontrada = operacion;
            break;
        }
    }

    if (operacionEncontrada == null) {
        throw new RuntimeException(
                "No existe la operación con ID: " + idOperacion
        );
    }

    if (dto.getNombre() != null
            && !dto.getNombre().trim().isEmpty()) {

        String nuevoNombre =
                dto.getNombre().trim();

        boolean nombreExiste = operaciones.stream()
                .anyMatch(operacion -> {

                    String idActual =
                            String.valueOf(
                                    operacion.get("id")
                            );

                    String nombreActual =
                            String.valueOf(
                                    operacion.get("nombre")
                            );

                    return !idOperacion.equals(idActual)
                            && nuevoNombre.equalsIgnoreCase(
                                    nombreActual
                            );
                });

        if (nombreExiste) {
            throw new RuntimeException(
                    "Ya existe una operación con el nombre: "
                            + nuevoNombre
            );
        }

        operacionEncontrada.put(
                "nombre",
                nuevoNombre
        );
    }

    if (dto.getVisibilidad() != null) {

        operacionEncontrada.put(
                "visibilidad",
                dto.getVisibilidad()
        );
    }

    if (dto.getTipoRetorno() != null
            && !dto.getTipoRetorno().trim().isEmpty()) {

        operacionEncontrada.put(
                "tipoRetorno",
                dto.getTipoRetorno().trim()
        );
    }

    if (dto.getEstatica() != null) {

        operacionEncontrada.put(
                "estatica",
                dto.getEstatica()
        );
    }

    if (dto.getAbstracta() != null) {

        operacionEncontrada.put(
                "abstracta",
                dto.getAbstracta()
        );
    }

    elementoEncontrado.put(
            "operaciones",
            operaciones
    );

    contenido.put(
            "elementos",
            elementos
    );

    modelo.setContenidoJson(contenido);

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);

    return operacionEncontrada;
}
public void eliminarOperacion(
        Long idModelo,
        String idElemento,
        String idOperacion
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: " + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    Map<String, Object> elementoEncontrado = null;

    for (Map<String, Object> elemento : elementos) {

        if (idElemento.equals(
                String.valueOf(elemento.get("id"))
        )) {
            elementoEncontrado = elemento;
            break;
        }
    }

    if (elementoEncontrado == null) {
        throw new RuntimeException(
                "No existe el elemento con ID: " + idElemento
        );
    }

    Object operacionesObj =
            elementoEncontrado.get("operaciones");

    if (!(operacionesObj instanceof List<?>)) {
        throw new RuntimeException(
                "El elemento no contiene operaciones"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> operaciones =
            (List<Map<String, Object>>) operacionesObj;

    boolean eliminado = operaciones.removeIf(
            operacion ->
                    idOperacion.equals(
                            String.valueOf(
                                    operacion.get("id")
                            )
                    )
    );

    if (!eliminado) {
        throw new RuntimeException(
                "No existe la operación con ID: " + idOperacion
        );
    }

    elementoEncontrado.put(
            "operaciones",
            operaciones
    );

    contenido.put(
            "elementos",
            elementos
    );

    modelo.setContenidoJson(contenido);

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);
}
public List<Map<String, Object>> obtenerParametros(
        Long idModelo,
        String idElemento,
        String idOperacion
) {

    Map<String, Object> operacion =
            obtenerOperacion(
                    idModelo,
                    idElemento,
                    idOperacion
            );

    Object parametrosObj =
            operacion.get("parametros");

    if (!(parametrosObj instanceof List<?>)) {
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> parametros =
            (List<Map<String, Object>>) parametrosObj;

    return parametros;
}
private Map<String, Object> obtenerOperacion(
        Long idModelo,
        String idElemento,
        String idOperacion
) {

    Map<String, Object> elemento =
            obtenerElemento(
                    idModelo,
                    idElemento
            );

    Object operacionesObj =
            elemento.get("operaciones");

    if (!(operacionesObj instanceof List<?>)) {
        throw new RuntimeException(
                "El elemento no contiene operaciones"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> operaciones =
            (List<Map<String, Object>>) operacionesObj;

    return operaciones.stream()
            .filter(operacion ->
                    idOperacion.equals(
                            String.valueOf(
                                    operacion.get("id")
                            )
                    )
            )
            .findFirst()
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe la operación con ID: "
                                    + idOperacion
                    )
            );
}
public Map<String, Object> agregarParametro(
        Long idModelo,
        String idElemento,
        String idOperacion,
        ParametroCreateDTO dto
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: "
                                    + idModelo
                    ));

    Map<String, Object> contenido =
        modelo.getContenidoJson();

if (contenido == null) {
    throw new RuntimeException(
            "El modelo no tiene contenido JSON"
    );
}

Object elementosObj =
        contenido.get("elementos");

if (!(elementosObj instanceof List<?>)) {
    throw new RuntimeException(
            "El modelo no contiene elementos UML"
    );
}

@SuppressWarnings("unchecked")
List<Map<String, Object>> elementos =
        (List<Map<String, Object>>) elementosObj;

Map<String, Object> elementoEncontrado = null;

for (Map<String, Object> elemento : elementos) {

    if (idElemento.equals(
            String.valueOf(
                    elemento.get("id")
            )
    )) {

        elementoEncontrado = elemento;
        break;
    }
}

if (elementoEncontrado == null) {
    throw new RuntimeException(
            "No existe el elemento con ID: "
                    + idElemento
    );
}

Object operacionesObj =
        elementoEncontrado.get("operaciones");

if (!(operacionesObj instanceof List<?>)) {
    throw new RuntimeException(
            "El elemento no contiene operaciones"
    );
}

@SuppressWarnings("unchecked")
List<Map<String, Object>> operaciones =
        (List<Map<String, Object>>) operacionesObj;

Map<String, Object> operacionEncontrada = null;

for (Map<String, Object> operacion : operaciones) {

    if (idOperacion.equals(
            String.valueOf(
                    operacion.get("id")
            )
    )) {

        operacionEncontrada = operacion;
        break;
    }
}

if (operacionEncontrada == null) {
    throw new RuntimeException(
            "No existe la operación con ID: "
                    + idOperacion
    );
}

    Object parametrosObj =
        operacionEncontrada.get("parametros");

    List<Map<String, Object>> parametros;

    if (parametrosObj instanceof List<?>) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista =
                (List<Map<String, Object>>) parametrosObj;

        parametros = lista;

    } else {

        parametros = new ArrayList<>();

        operacionEncontrada.put(
        "parametros",
        parametros
);
    }

    String nombre =
            dto.getNombre().trim();

    boolean nombreExiste =
            parametros.stream()
                    .anyMatch(parametro ->
                            nombre.equalsIgnoreCase(
                                    String.valueOf(
                                            parametro.get("nombre")
                                    )
                            )
                    );

    if (nombreExiste) {
        throw new RuntimeException(
                "Ya existe un parámetro con el nombre: "
                        + nombre
        );
    }

    Map<String, Object> parametro =
            new LinkedHashMap<>();

    parametro.put(
            "id",
            UUID.randomUUID().toString()
    );

    parametro.put(
            "nombre",
            nombre
    );

    parametro.put(
            "tipo",
            dto.getTipo().trim()
    );

    parametro.put(
            "direccion",
            dto.getDireccion() != null
                    && !dto.getDireccion().trim().isEmpty()
                    ? dto.getDireccion().trim().toUpperCase()
                    : "IN"
    );

    parametro.put(
            "valorInicial",
            dto.getValorInicial()
    );

    parametros.add(parametro);

    operacionEncontrada.put(
        "parametros",
        parametros
);

    

    modelo.setContenidoJson(contenido);

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);

    return parametro;
}
public Map<String, Object> actualizarParametro(
        Long idModelo,
        String idElemento,
        String idOperacion,
        String idParametro,
        ParametroUpdateDTO dto
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: "
                                    + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    Map<String, Object> elementoEncontrado = null;

    for (Map<String, Object> elemento : elementos) {

        if (idElemento.equals(
                String.valueOf(
                        elemento.get("id")
                )
        )) {

            elementoEncontrado = elemento;
            break;
        }
    }

    if (elementoEncontrado == null) {
        throw new RuntimeException(
                "No existe el elemento con ID: "
                        + idElemento
        );
    }

    Object operacionesObj =
            elementoEncontrado.get("operaciones");

    if (!(operacionesObj instanceof List<?>)) {
        throw new RuntimeException(
                "El elemento no contiene operaciones"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> operaciones =
            (List<Map<String, Object>>) operacionesObj;

    Map<String, Object> operacionEncontrada = null;

    for (Map<String, Object> operacion : operaciones) {

        if (idOperacion.equals(
                String.valueOf(
                        operacion.get("id")
                )
        )) {

            operacionEncontrada = operacion;
            break;
        }
    }

    if (operacionEncontrada == null) {
        throw new RuntimeException(
                "No existe la operación con ID: "
                        + idOperacion
        );
    }

    Object parametrosObj =
            operacionEncontrada.get("parametros");

    if (!(parametrosObj instanceof List<?>)) {
        throw new RuntimeException(
                "La operación no contiene parámetros"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> parametros =
            (List<Map<String, Object>>) parametrosObj;

    Map<String, Object> parametroEncontrado = null;

    for (Map<String, Object> parametro : parametros) {

        if (idParametro.equals(
                String.valueOf(
                        parametro.get("id")
                )
        )) {

            parametroEncontrado = parametro;
            break;
        }
    }

    if (parametroEncontrado == null) {
        throw new RuntimeException(
                "No existe el parámetro con ID: "
                        + idParametro
        );
    }

    if (dto.getNombre() != null
            && !dto.getNombre().trim().isEmpty()) {

        String nuevoNombre =
                dto.getNombre().trim();

        boolean nombreExiste =
                parametros.stream()
                        .anyMatch(parametro -> {

                            String idActual =
                                    String.valueOf(
                                            parametro.get("id")
                                    );

                            String nombreActual =
                                    String.valueOf(
                                            parametro.get("nombre")
                                    );

                            return !idParametro.equals(idActual)
                                    && nuevoNombre.equalsIgnoreCase(
                                            nombreActual
                                    );
                        });

        if (nombreExiste) {
            throw new RuntimeException(
                    "Ya existe un parámetro con el nombre: "
                            + nuevoNombre
            );
        }

        parametroEncontrado.put(
                "nombre",
                nuevoNombre
        );
    }

    if (dto.getTipo() != null
            && !dto.getTipo().trim().isEmpty()) {

        parametroEncontrado.put(
                "tipo",
                dto.getTipo().trim()
        );
    }

    if (dto.getDireccion() != null
            && !dto.getDireccion().trim().isEmpty()) {

        parametroEncontrado.put(
                "direccion",
                dto.getDireccion()
                        .trim()
                        .toUpperCase()
        );
    }

    if (dto.getValorInicial() != null) {

        parametroEncontrado.put(
                "valorInicial",
                dto.getValorInicial()
        );
    }

    operacionEncontrada.put(
            "parametros",
            parametros
    );

    elementoEncontrado.put(
            "operaciones",
            operaciones
    );

    contenido.put(
            "elementos",
            elementos
    );

    modelo.setContenidoJson(
            contenido
    );

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);

    return parametroEncontrado;
}
public void eliminarParametro(
        Long idModelo,
        String idElemento,
        String idOperacion,
        String idParametro
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: "
                                    + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    Map<String, Object> elementoEncontrado = null;

    for (Map<String, Object> elemento : elementos) {

        if (idElemento.equals(
                String.valueOf(
                        elemento.get("id")
                )
        )) {

            elementoEncontrado = elemento;
            break;
        }
    }

    if (elementoEncontrado == null) {
        throw new RuntimeException(
                "No existe el elemento con ID: "
                        + idElemento
        );
    }

    Object operacionesObj =
            elementoEncontrado.get("operaciones");

    if (!(operacionesObj instanceof List<?>)) {
        throw new RuntimeException(
                "El elemento no contiene operaciones"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> operaciones =
            (List<Map<String, Object>>) operacionesObj;

    Map<String, Object> operacionEncontrada = null;

    for (Map<String, Object> operacion : operaciones) {

        if (idOperacion.equals(
                String.valueOf(
                        operacion.get("id")
                )
        )) {

            operacionEncontrada = operacion;
            break;
        }
    }

    if (operacionEncontrada == null) {
        throw new RuntimeException(
                "No existe la operación con ID: "
                        + idOperacion
        );
    }

    Object parametrosObj =
            operacionEncontrada.get("parametros");

    if (!(parametrosObj instanceof List<?>)) {
        throw new RuntimeException(
                "La operación no contiene parámetros"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> parametros =
            (List<Map<String, Object>>) parametrosObj;

    boolean eliminado =
            parametros.removeIf(
                    parametro ->
                            idParametro.equals(
                                    String.valueOf(
                                            parametro.get("id")
                                    )
                            )
            );

    if (!eliminado) {
        throw new RuntimeException(
                "No existe el parámetro con ID: "
                        + idParametro
        );
    }

    operacionEncontrada.put(
            "parametros",
            parametros
    );

    elementoEncontrado.put(
            "operaciones",
            operaciones
    );

    contenido.put(
            "elementos",
            elementos
    );

    modelo.setContenidoJson(
            contenido
    );

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);
}
private boolean tipoRelacionValido(String tipo) {

    return tipo.equals("ASOCIACION")
            || tipo.equals("GENERALIZACION")
            || tipo.equals("AGREGACION")
            || tipo.equals("COMPOSICION")
            || tipo.equals("DEPENDENCIA")
            || tipo.equals("REALIZACION");
}
public List<Map<String, Object>> obtenerRelaciones(
        Long idModelo
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: "
                                    + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object relacionesObj =
            contenido.get("relaciones");

    if (!(relacionesObj instanceof List<?>)) {
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> relaciones =
            (List<Map<String, Object>>) relacionesObj;

    return relaciones;
}
@Transactional
public Map<String, Object> agregarRelacion(
        Long idModelo,
        RelacionCreateDTO dto
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: "
                                    + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    String tipo = dto.getTipo()
            .trim()
            .toUpperCase();

    if (!tipoRelacionValido(tipo)) {
        throw new RuntimeException(
                "Tipo de relación no válido. Use: "
                        + "ASOCIACION, GENERALIZACION, "
                        + "AGREGACION, COMPOSICION, "
                        + "DEPENDENCIA o REALIZACION."
        );
    }

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    boolean origenExiste = elementos.stream()
            .anyMatch(elemento ->
                    dto.getOrigen().trim().equals(
                            String.valueOf(
                                    elemento.get("id")
                            )
                    )
            );

    if (!origenExiste) {
        throw new RuntimeException(
                "No existe el elemento origen con ID: "
                        + dto.getOrigen()
        );
    }

    boolean destinoExiste = elementos.stream()
            .anyMatch(elemento ->
                    dto.getDestino().trim().equals(
                            String.valueOf(
                                    elemento.get("id")
                            )
                    )
            );

    if (!destinoExiste) {
        throw new RuntimeException(
                "No existe el elemento destino con ID: "
                        + dto.getDestino()
        );
    }

    Object relacionesObj =
            contenido.get("relaciones");

    List<Map<String, Object>> relaciones;

    if (relacionesObj instanceof List<?>) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista =
                (List<Map<String, Object>>) relacionesObj;

        relaciones = lista;

    } else {

        relaciones = new ArrayList<>();

        contenido.put(
                "relaciones",
                relaciones
        );
    }

    String origen = dto.getOrigen().trim();
    String destino = dto.getDestino().trim();

    boolean relacionExiste = relaciones.stream()
            .anyMatch(relacion ->
                    tipo.equals(
                            String.valueOf(
                                    relacion.get("tipo")
                            )
                    )
                    && origen.equals(
                            String.valueOf(
                                    relacion.get("origen")
                            )
                    )
                    && destino.equals(
                            String.valueOf(
                                    relacion.get("destino")
                            )
                    )
            );

    if (relacionExiste) {
        throw new RuntimeException(
                "Ya existe una relación del mismo tipo "
                        + "entre los elementos indicados"
        );
    }

    Map<String, Object> relacion =
            new LinkedHashMap<>();

    relacion.put(
            "id",
            UUID.randomUUID().toString()
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
            dto.getRolOrigen() != null
                    && !dto.getRolOrigen().trim().isEmpty()
                    ? dto.getRolOrigen().trim()
                    : null
    );

    relacion.put(
            "rolDestino",
            dto.getRolDestino() != null
                    && !dto.getRolDestino().trim().isEmpty()
                    ? dto.getRolDestino().trim()
                    : null
    );

    relacion.put(
            "multiplicidadOrigen",
            dto.getMultiplicidadOrigen() != null
                    && !dto.getMultiplicidadOrigen().trim().isEmpty()
                    ? dto.getMultiplicidadOrigen().trim()
                    : "1"
    );

    relacion.put(
            "multiplicidadDestino",
            dto.getMultiplicidadDestino() != null
                    && !dto.getMultiplicidadDestino().trim().isEmpty()
                    ? dto.getMultiplicidadDestino().trim()
                    : "1"
    );

    relaciones.add(relacion);

    contenido.put(
            "relaciones",
            relaciones
    );

    modelo.setContenidoJson(
            contenido
    );

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);

    return relacion;
}
public Map<String, Object> obtenerRelacion(
        Long idModelo,
        String idRelacion
) {

    List<Map<String, Object>> relaciones =
            obtenerRelaciones(idModelo);

    return relaciones.stream()
            .filter(relacion ->
                    idRelacion.equals(
                            String.valueOf(
                                    relacion.get("id")
                            )
                    )
            )
            .findFirst()
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe la relación con ID: "
                                    + idRelacion
                    )
            );
}
@Transactional
public Map<String, Object> actualizarRelacion(
        Long idModelo,
        String idRelacion,
        RelacionUpdateDTO dto
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: "
                                    + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object relacionesObj =
            contenido.get("relaciones");

    if (!(relacionesObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene relaciones"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> relaciones =
            (List<Map<String, Object>>) relacionesObj;

    Map<String, Object> relacionEncontrada = null;

    for (Map<String, Object> relacion : relaciones) {

        if (idRelacion.equals(
                String.valueOf(
                        relacion.get("id")
                )
        )) {

            relacionEncontrada = relacion;
            break;
        }
    }

    if (relacionEncontrada == null) {
        throw new RuntimeException(
                "No existe la relación con ID: "
                        + idRelacion
        );
    }

    if (dto.getTipo() != null
            && !dto.getTipo().trim().isEmpty()) {

        String tipo =
                dto.getTipo()
                        .trim()
                        .toUpperCase();

        if (!tipoRelacionValido(tipo)) {
            throw new RuntimeException(
                    "Tipo de relación no válido"
            );
        }

        relacionEncontrada.put(
                "tipo",
                tipo
        );
    }

    if (dto.getOrigen() != null
            && !dto.getOrigen().trim().isEmpty()) {

        String origen =
                dto.getOrigen().trim();

        validarElementoExiste(
                contenido,
                origen
        );

        relacionEncontrada.put(
                "origen",
                origen
        );
    }

    if (dto.getDestino() != null
            && !dto.getDestino().trim().isEmpty()) {

        String destino =
                dto.getDestino().trim();

        validarElementoExiste(
                contenido,
                destino
        );

        relacionEncontrada.put(
                "destino",
                destino
        );
    }

    if (dto.getRolOrigen() != null) {
        relacionEncontrada.put(
                "rolOrigen",
                dto.getRolOrigen().trim()
        );
    }

    if (dto.getRolDestino() != null) {
        relacionEncontrada.put(
                "rolDestino",
                dto.getRolDestino().trim()
        );
    }

    if (dto.getMultiplicidadOrigen() != null) {
        relacionEncontrada.put(
                "multiplicidadOrigen",
                dto.getMultiplicidadOrigen().trim()
        );
    }

    if (dto.getMultiplicidadDestino() != null) {
        relacionEncontrada.put(
                "multiplicidadDestino",
                dto.getMultiplicidadDestino().trim()
        );
    }

    contenido.put(
            "relaciones",
            relaciones
    );

    modelo.setContenidoJson(
            contenido
    );

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);

    return relacionEncontrada;
}
private void validarElementoExiste(
        Map<String, Object> contenido,
        String idElemento
) {

    Object elementosObj =
            contenido.get("elementos");

    if (!(elementosObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene elementos UML"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elementos =
            (List<Map<String, Object>>) elementosObj;

    boolean existe = elementos.stream()
            .anyMatch(elemento ->
                    idElemento.equals(
                            String.valueOf(
                                    elemento.get("id")
                            )
                    )
            );

    if (!existe) {
        throw new RuntimeException(
                "No existe el elemento con ID: "
                        + idElemento
        );
    }
}
@Transactional
public void eliminarRelacion(
        Long idModelo,
        String idRelacion
) {

    Modelo modelo = modeloRepository.findById(idModelo)
            .orElseThrow(() ->
                    new RuntimeException(
                            "No existe el modelo con ID: "
                                    + idModelo
                    ));

    Map<String, Object> contenido =
            modelo.getContenidoJson();

    if (contenido == null) {
        throw new RuntimeException(
                "El modelo no tiene contenido JSON"
        );
    }

    Object relacionesObj =
            contenido.get("relaciones");

    if (!(relacionesObj instanceof List<?>)) {
        throw new RuntimeException(
                "El modelo no contiene relaciones"
        );
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> relaciones =
            (List<Map<String, Object>>) relacionesObj;

    boolean eliminado =
            relaciones.removeIf(
                    relacion ->
                            idRelacion.equals(
                                    String.valueOf(
                                            relacion.get("id")
                                    )
                            )
            );

    if (!eliminado) {
        throw new RuntimeException(
                "No existe la relación con ID: "
                        + idRelacion
        );
    }

    contenido.put(
            "relaciones",
            relaciones
    );

    modelo.setContenidoJson(
            contenido
    );

    actualizarVersion(modelo);

    modelo.setFechaActualizacion(
            LocalDateTime.now()
    );

    modeloRepository.save(modelo);
}
}