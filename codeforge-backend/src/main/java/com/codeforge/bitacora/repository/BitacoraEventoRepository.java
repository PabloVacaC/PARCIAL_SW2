package com.codeforge.bitacora.repository;

import com.codeforge.bitacora.entity.BitacoraEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraEventoRepository extends JpaRepository<BitacoraEvento, Long> {

    List<BitacoraEvento> findByIdProyectoOrderByFechaHoraDesc(Long idProyecto);

    List<BitacoraEvento> findByIdProyectoOrderByFechaHoraAsc(Long idProyecto);
}
