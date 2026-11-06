package com.codeforge.ia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codeforge.ia.entity.IaMetrica;

@Repository
public interface IaMetricaRepository extends JpaRepository<IaMetrica, Long> {

    List<IaMetrica> findByProyecto_IdProyectoOrderByFechaRegistroDesc(Long idProyecto);
}
