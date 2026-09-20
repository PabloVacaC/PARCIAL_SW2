package com.codeforge.proyecto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeforge.proyecto.entity.Proyecto;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    List<Proyecto> findByPropietario_IdUsuario(Long idUsuario);

    List<Proyecto> findByEstado(String estado);

    boolean existsByNombreAndPropietario_IdUsuario(
            String nombre,
            Long idUsuario
    );
}