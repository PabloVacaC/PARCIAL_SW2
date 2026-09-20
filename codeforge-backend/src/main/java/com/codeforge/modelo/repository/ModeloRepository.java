package com.codeforge.modelo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codeforge.modelo.entity.Modelo;

@Repository
public interface ModeloRepository
        extends JpaRepository<Modelo, Long> {

    List<Modelo> findByProyecto_IdProyecto(
            Long idProyecto
    );

}