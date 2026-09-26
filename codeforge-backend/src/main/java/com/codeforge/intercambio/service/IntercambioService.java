package com.codeforge.intercambio.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeforge.modelo.entity.Modelo;
import com.codeforge.modelo.repository.ModeloRepository;

@Service
public class IntercambioService {

    private final ModeloRepository modeloRepository;

    public IntercambioService(
            ModeloRepository modeloRepository
    ) {
        this.modeloRepository = modeloRepository;
    }

    // =========================================================
    // EXPORTAR MODELO COMO JSON
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Object> exportarJson(
            Long idModelo
    ) {

        Modelo modelo =
                modeloRepository.findById(idModelo)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No existe el modelo con ID: "
                                                + idModelo
                                )
                        );

        Map<String, Object> contenido =
                modelo.getContenidoJson();

        if (contenido == null) {
            throw new RuntimeException(
                    "El modelo no tiene contenido JSON"
            );
        }

        return contenido;
    }

    // =========================================================
    // IMPORTAR MODELO DESDE JSON
    // =========================================================

    @Transactional
    public Map<String, Object> importarJson(
            Long idModelo,
            Map<String, Object> contenidoImportado
    ) {

        if (contenidoImportado == null) {
            throw new RuntimeException(
                    "El contenido JSON no puede ser nulo"
            );
        }

        // -----------------------------------------------------
        // VALIDAR ESTRUCTURA
        // -----------------------------------------------------

        validarContenidoUML(
                contenidoImportado
        );

        // -----------------------------------------------------
        // BUSCAR MODELO
        // -----------------------------------------------------

        Modelo modelo =
                modeloRepository.findById(idModelo)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No existe el modelo con ID: "
                                                + idModelo
                                )
                        );

        // -----------------------------------------------------
        // GUARDAR CONTENIDO
        // -----------------------------------------------------

        modelo.setContenidoJson(
                contenidoImportado
        );

        // -----------------------------------------------------
        // ACTUALIZAR FECHA
        // -----------------------------------------------------

        modelo.setFechaActualizacion(
                LocalDateTime.now()
        );

        // -----------------------------------------------------
        // INCREMENTAR VERSIÓN
        // -----------------------------------------------------

        incrementarVersion(
                modelo
        );

        // -----------------------------------------------------
        // PERSISTIR
        // -----------------------------------------------------

        Modelo guardado =
                modeloRepository.save(
                        modelo
                );

        return guardado.getContenidoJson();
    }

    // =========================================================
    // VALIDAR ESTRUCTURA UML
    // =========================================================

    private void validarContenidoUML(
            Map<String, Object> contenido
    ) {

        // -----------------------------------------------------
        // versionUML
        // -----------------------------------------------------

        Object versionUML =
                contenido.get("versionUML");

        if (versionUML == null) {

            contenido.put(
                    "versionUML",
                    "2.5"
            );

        }
        else if (!(versionUML instanceof String)) {

            throw new RuntimeException(
                    "El campo 'versionUML' debe ser texto"
            );

        }

        // -----------------------------------------------------
        // elementos
        // -----------------------------------------------------

        Object elementosObj =
                contenido.get("elementos");

        if (elementosObj == null) {

            contenido.put(
                    "elementos",
                    new ArrayList<>()
            );

        }
        else if (!(elementosObj instanceof List<?>)) {

            throw new RuntimeException(
                    "El campo 'elementos' debe ser un arreglo"
            );

        }

        // -----------------------------------------------------
        // relaciones
        // -----------------------------------------------------

        Object relacionesObj =
                contenido.get("relaciones");

        if (relacionesObj == null) {

            contenido.put(
                    "relaciones",
                    new ArrayList<>()
            );

        }
        else if (!(relacionesObj instanceof List<?>)) {

            throw new RuntimeException(
                    "El campo 'relaciones' debe ser un arreglo"
            );

        }

        // -----------------------------------------------------
        // VALIDAR CADA ELEMENTO
        // -----------------------------------------------------

        @SuppressWarnings("unchecked")
        List<Object> elementos =
                (List<Object>)
                        contenido.get("elementos");

        for (Object objeto : elementos) {

            if (!(objeto instanceof Map<?, ?>)) {

                throw new RuntimeException(
                        "Cada elemento UML debe ser un objeto JSON"
                );
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> elemento =
                    (Map<String, Object>) objeto;

            validarElemento(
                    elemento
            );
        }

        // -----------------------------------------------------
        // VALIDAR CADA RELACIÓN
        // -----------------------------------------------------

        @SuppressWarnings("unchecked")
        List<Object> relaciones =
                (List<Object>)
                        contenido.get("relaciones");

        for (Object objeto : relaciones) {

            if (!(objeto instanceof Map<?, ?>)) {

                throw new RuntimeException(
                        "Cada relación UML debe ser un objeto JSON"
                );
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> relacion =
                    (Map<String, Object>) objeto;

            validarRelacion(
                    relacion
            );
        }
    }

    // =========================================================
    // VALIDAR ELEMENTO
    // =========================================================

    private void validarElemento(
            Map<String, Object> elemento
    ) {

        Object id =
                elemento.get("id");

        Object tipo =
                elemento.get("tipo");

        Object nombre =
                elemento.get("nombre");

        if (id == null) {

            throw new RuntimeException(
                    "Un elemento UML debe tener 'id'"
            );
        }

        if (tipo == null) {

            throw new RuntimeException(
                    "Un elemento UML debe tener 'tipo'"
            );
        }

        if (nombre == null) {

            throw new RuntimeException(
                    "Un elemento UML debe tener 'nombre'"
            );
        }

        String tipoTexto =
                String.valueOf(
                        tipo
                )
                .toUpperCase();

        if (
                !tipoTexto.equals("CLASE")
                && !tipoTexto.equals("INTERFACE")
                && !tipoTexto.equals("ENUM")
        ) {

            throw new RuntimeException(
                    "Tipo de elemento UML no válido: "
                            + tipo
            );
        }

        // -----------------------------------------------------
        // ATRIBUTOS
        // -----------------------------------------------------

        Object atributosObj =
                elemento.get("atributos");

        if (atributosObj == null) {

            elemento.put(
                    "atributos",
                    new ArrayList<>()
            );

        }
        else if (!(atributosObj instanceof List<?>)) {

            throw new RuntimeException(
                    "El campo 'atributos' debe ser un arreglo"
            );
        }

        // -----------------------------------------------------
        // OPERACIONES
        // -----------------------------------------------------

        Object operacionesObj =
                elemento.get("operaciones");

        if (operacionesObj == null) {

            elemento.put(
                    "operaciones",
                    new ArrayList<>()
            );

        }
        else if (!(operacionesObj instanceof List<?>)) {

            throw new RuntimeException(
                    "El campo 'operaciones' debe ser un arreglo"
            );
        }
    }

    // =========================================================
    // VALIDAR RELACIÓN
    // =========================================================

    private void validarRelacion(
            Map<String, Object> relacion
    ) {

        Object id =
                relacion.get("id");

        Object tipo =
                relacion.get("tipo");

        Object origen =
                relacion.get("origen");

        Object destino =
                relacion.get("destino");

        if (id == null) {

            throw new RuntimeException(
                    "Una relación debe tener 'id'"
            );
        }

        if (tipo == null) {

            throw new RuntimeException(
                    "Una relación debe tener 'tipo'"
            );
        }

        if (origen == null) {

            throw new RuntimeException(
                    "Una relación debe tener 'origen'"
            );
        }

        if (destino == null) {

            throw new RuntimeException(
                    "Una relación debe tener 'destino'"
            );
        }

        String tipoTexto =
                String.valueOf(
                        tipo
                )
                .toUpperCase();

        if (
                !tipoTexto.equals("ASOCIACION")
                && !tipoTexto.equals("GENERALIZACION")
                && !tipoTexto.equals("AGREGACION")
                && !tipoTexto.equals("COMPOSICION")
                && !tipoTexto.equals("DEPENDENCIA")
                && !tipoTexto.equals("REALIZACION")
                && !tipoTexto.equals("CLASE_ASOCIACION")
                && !tipoTexto.equals("ASOCIACION_CLASE")
                && !tipoTexto.equals("RECURSIVA")
        ) {

            throw new RuntimeException(
                    "Tipo de relación UML no válido: "
                            + tipo
            );
        }
    }

    // =========================================================
    // INCREMENTAR VERSIÓN
    // =========================================================

    private void incrementarVersion(
            Modelo modelo
    ) {

        String versionActual =
                modelo.getVersion();

        if (
                versionActual == null
                || versionActual.isBlank()
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
                    mayor +
                            "." +
                            menor
            );

        }
        catch (NumberFormatException ex) {

            modelo.setVersion(
                    "1.1"
            );
        }
    }
}