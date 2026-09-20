package com.codeforge.modelo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.codeforge.modelo.service.ModeloService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/modelos")
public class ModeloController {

    private final ModeloService modeloService;

    public ModeloController(
            ModeloService modeloService
    ) {
        this.modeloService = modeloService;
    }

    // =========================================================
    // CREAR MODELO
    // =========================================================

    @PostMapping
    public ResponseEntity<ModeloResponseDTO> crearModelo(
            @Valid
            @RequestBody
            ModeloCreateDTO dto
    ) {

        ModeloResponseDTO modelo =
                modeloService.crearModelo(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(modelo);
    }

    // =========================================================
    // MODELOS DE UN PROYECTO
    // =========================================================

    @GetMapping("/proyecto/{idProyecto}")
    public ResponseEntity<List<ModeloResponseDTO>> obtenerPorProyecto(
            @PathVariable Long idProyecto
    ) {

        return ResponseEntity.ok(
                modeloService.obtenerPorProyecto(idProyecto)
        );
    }

    // =========================================================
    // OBTENER MODELO
    // =========================================================

    @GetMapping("/{idModelo}")
    public ResponseEntity<ModeloResponseDTO> obtenerPorId(
            @PathVariable Long idModelo
    ) {

        return ResponseEntity.ok(
                modeloService.obtenerPorId(idModelo)
        );
    }

    // =========================================================
    // ACTUALIZAR TODO EL JSON
    // =========================================================

    @PutMapping("/{idModelo}/contenido")
    public ResponseEntity<ModeloResponseDTO> actualizarContenido(
            @PathVariable Long idModelo,

            @Valid
            @RequestBody
            ModeloContenidoDTO dto
    ) {

        return ResponseEntity.ok(
                modeloService.actualizarContenido(
                        idModelo,
                        dto
                )
        );
    }

    // =========================================================
    // OBTENER SOLO EL JSON
    // =========================================================

    @GetMapping("/{idModelo}/contenido")
    public ResponseEntity<Map<String, Object>> obtenerContenido(
            @PathVariable Long idModelo
    ) {

        return ResponseEntity.ok(
                modeloService.obtenerContenido(idModelo)
        );
    }

    // =========================================================
    // ELEMENTOS UML
    // =========================================================

    @PostMapping("/{idModelo}/elementos")
    public ResponseEntity<ModeloResponseDTO> agregarElemento(
            @PathVariable Long idModelo,
            @Valid @RequestBody ElementoCreateDTO dto
    ) {

        ModeloResponseDTO modelo =
                modeloService.agregarElemento(
                        idModelo,
                        dto
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(modelo);
    }

    @GetMapping("/{idModelo}/elementos")
    public ResponseEntity<List<Map<String, Object>>> obtenerElementos(
            @PathVariable Long idModelo
    ) {

        return ResponseEntity.ok(
                modeloService.obtenerElementos(idModelo)
        );
    }

    @GetMapping("/{idModelo}/elementos/{idElemento}")
    public ResponseEntity<Map<String, Object>> obtenerElemento(
            @PathVariable Long idModelo,
            @PathVariable String idElemento
    ) {

        return ResponseEntity.ok(
                modeloService.obtenerElemento(
                        idModelo,
                        idElemento
                )
        );
    }

    @PutMapping("/{idModelo}/elementos/{idElemento}")
    public ResponseEntity<Map<String, Object>> actualizarElemento(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @Valid @RequestBody ElementoUpdateDTO dto
    ) {

        return ResponseEntity.ok(
                modeloService.actualizarElemento(
                        idModelo,
                        idElemento,
                        dto
                )
        );
    }

    @DeleteMapping("/{idModelo}/elementos/{idElemento}")
    public ResponseEntity<Void> eliminarElemento(
            @PathVariable Long idModelo,
            @PathVariable String idElemento
    ) {

        modeloService.eliminarElemento(
                idModelo,
                idElemento
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // =========================================================
    // ATRIBUTOS UML
    // =========================================================

    @GetMapping("/{idModelo}/elementos/{idElemento}/atributos")
    public ResponseEntity<List<Map<String, Object>>> obtenerAtributos(
            @PathVariable Long idModelo,
            @PathVariable String idElemento
    ) {

        return ResponseEntity.ok(
                modeloService.obtenerAtributos(
                        idModelo,
                        idElemento
                )
        );
    }

    @PostMapping("/{idModelo}/elementos/{idElemento}/atributos")
    public ResponseEntity<Map<String, Object>> agregarAtributo(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @Valid @RequestBody AtributoCreateDTO dto
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        modeloService.agregarAtributo(
                                idModelo,
                                idElemento,
                                dto
                        )
                );
    }

    @PutMapping(
            "/{idModelo}/elementos/{idElemento}/atributos/{idAtributo}"
    )
    public ResponseEntity<Map<String, Object>> actualizarAtributo(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @PathVariable String idAtributo,
            @Valid @RequestBody AtributoUpdateDTO dto
    ) {

        return ResponseEntity.ok(
                modeloService.actualizarAtributo(
                        idModelo,
                        idElemento,
                        idAtributo,
                        dto
                )
        );
    }

    @DeleteMapping(
            "/{idModelo}/elementos/{idElemento}/atributos/{idAtributo}"
    )
    public ResponseEntity<Void> eliminarAtributo(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @PathVariable String idAtributo
    ) {

        modeloService.eliminarAtributo(
                idModelo,
                idElemento,
                idAtributo
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // =========================================================
    // OPERACIONES UML
    // =========================================================

    @GetMapping("/{idModelo}/elementos/{idElemento}/operaciones")
    public ResponseEntity<List<Map<String, Object>>> obtenerOperaciones(
            @PathVariable Long idModelo,
            @PathVariable String idElemento
    ) {

        return ResponseEntity.ok(
                modeloService.obtenerOperaciones(
                        idModelo,
                        idElemento
                )
        );
    }

    @PostMapping("/{idModelo}/elementos/{idElemento}/operaciones")
    public ResponseEntity<Map<String, Object>> agregarOperacion(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @Valid @RequestBody OperacionCreateDTO dto
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        modeloService.agregarOperacion(
                                idModelo,
                                idElemento,
                                dto
                        )
                );
    }

    @PutMapping(
            "/{idModelo}/elementos/{idElemento}/operaciones/{idOperacion}"
    )
    public ResponseEntity<Map<String, Object>> actualizarOperacion(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @PathVariable String idOperacion,
            @Valid @RequestBody OperacionUpdateDTO dto
    ) {

        return ResponseEntity.ok(
                modeloService.actualizarOperacion(
                        idModelo,
                        idElemento,
                        idOperacion,
                        dto
                )
        );
    }

    @DeleteMapping(
            "/{idModelo}/elementos/{idElemento}/operaciones/{idOperacion}"
    )
    public ResponseEntity<Void> eliminarOperacion(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @PathVariable String idOperacion
    ) {

        modeloService.eliminarOperacion(
                idModelo,
                idElemento,
                idOperacion
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // =========================================================
    // PARAMETROS UML
    // =========================================================

    @GetMapping(
            "/{idModelo}/elementos/{idElemento}/operaciones/{idOperacion}/parametros"
    )
    public ResponseEntity<List<Map<String, Object>>> obtenerParametros(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @PathVariable String idOperacion
    ) {

        return ResponseEntity.ok(
                modeloService.obtenerParametros(
                        idModelo,
                        idElemento,
                        idOperacion
                )
        );
    }

    @PostMapping(
            "/{idModelo}/elementos/{idElemento}/operaciones/{idOperacion}/parametros"
    )
    public ResponseEntity<Map<String, Object>> agregarParametro(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @PathVariable String idOperacion,

            @Valid
            @RequestBody
            ParametroCreateDTO dto
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        modeloService.agregarParametro(
                                idModelo,
                                idElemento,
                                idOperacion,
                                dto
                        )
                );
    }

    @PutMapping(
            "/{idModelo}/elementos/{idElemento}/operaciones/{idOperacion}/parametros/{idParametro}"
    )
    public ResponseEntity<Map<String, Object>> actualizarParametro(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @PathVariable String idOperacion,
            @PathVariable String idParametro,

            @Valid
            @RequestBody
            ParametroUpdateDTO dto
    ) {

        return ResponseEntity.ok(
                modeloService.actualizarParametro(
                        idModelo,
                        idElemento,
                        idOperacion,
                        idParametro,
                        dto
                )
        );
    }

    @DeleteMapping(
            "/{idModelo}/elementos/{idElemento}/operaciones/{idOperacion}/parametros/{idParametro}"
    )
    public ResponseEntity<Void> eliminarParametro(
            @PathVariable Long idModelo,
            @PathVariable String idElemento,
            @PathVariable String idOperacion,
            @PathVariable String idParametro
    ) {

        modeloService.eliminarParametro(
                idModelo,
                idElemento,
                idOperacion,
                idParametro
        );

        return ResponseEntity
                .noContent()
                .build();
    }
    // =========================================================
// OBTENER RELACIONES UML
// =========================================================

@GetMapping("/{idModelo}/relaciones")
public ResponseEntity<List<Map<String, Object>>> obtenerRelaciones(
        @PathVariable Long idModelo
) {

    return ResponseEntity.ok(
            modeloService.obtenerRelaciones(idModelo)
    );
}


// =========================================================
// OBTENER UNA RELACIÓN UML
// =========================================================

@GetMapping("/{idModelo}/relaciones/{idRelacion}")
public ResponseEntity<Map<String, Object>> obtenerRelacion(
        @PathVariable Long idModelo,
        @PathVariable String idRelacion
) {

    return ResponseEntity.ok(
            modeloService.obtenerRelacion(
                    idModelo,
                    idRelacion
            )
    );
}


// =========================================================
// AGREGAR RELACIÓN UML
// =========================================================

@PostMapping("/{idModelo}/relaciones")
public ResponseEntity<Map<String, Object>> agregarRelacion(
        @PathVariable Long idModelo,

        @Valid
        @RequestBody
        RelacionCreateDTO dto
) {

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                    modeloService.agregarRelacion(
                            idModelo,
                            dto
                    )
            );
}


// =========================================================
// ACTUALIZAR RELACIÓN UML
// =========================================================

@PutMapping("/{idModelo}/relaciones/{idRelacion}")
public ResponseEntity<Map<String, Object>> actualizarRelacion(
        @PathVariable Long idModelo,
        @PathVariable String idRelacion,

        @Valid
        @RequestBody
        RelacionUpdateDTO dto
) {

    return ResponseEntity.ok(
            modeloService.actualizarRelacion(
                    idModelo,
                    idRelacion,
                    dto
            )
    );
}


// =========================================================
// ELIMINAR RELACIÓN UML
// =========================================================

@DeleteMapping("/{idModelo}/relaciones/{idRelacion}")
public ResponseEntity<Void> eliminarRelacion(
        @PathVariable Long idModelo,
        @PathVariable String idRelacion
) {

    modeloService.eliminarRelacion(
            idModelo,
            idRelacion
    );

    return ResponseEntity
            .noContent()
            .build();
}
}