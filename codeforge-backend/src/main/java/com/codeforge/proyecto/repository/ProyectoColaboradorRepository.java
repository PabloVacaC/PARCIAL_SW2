package com.codeforge.proyecto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeforge.proyecto.entity.ProyectoColaborador;

public interface ProyectoColaboradorRepository
        extends JpaRepository<ProyectoColaborador, Long> {

    List<ProyectoColaborador> findByProyecto_IdProyecto(Long idProyecto);

    List<ProyectoColaborador> findByUsuario_IdUsuario(Long idUsuario);

    boolean existsByProyecto_IdProyectoAndUsuario_IdUsuario(
            Long idProyecto,
            Long idUsuario
    );
}