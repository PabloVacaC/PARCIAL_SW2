package com.codeforge.ia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codeforge.ia.entity.IaPropuesta;

@Repository
public interface IaPropuestaRepository extends JpaRepository<IaPropuesta, Long> {

    List<IaPropuesta> findByProyecto_IdProyectoOrderByFechaCreacionDesc(Long idProyecto);

    Optional<IaPropuesta> findByIdPropuestaAndProyecto_IdProyecto(Long idPropuesta, Long idProyecto);

    List<IaPropuesta> findByProyecto_IdProyectoAndEstado(Long idProyecto, String estado);
}
