package com.codeforge.ia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codeforge.ia.entity.IaConversacion;

@Repository
public interface IaConversacionRepository extends JpaRepository<IaConversacion, Long> {

    Optional<IaConversacion> findByProyecto_IdProyecto(Long idProyecto);

    boolean existsByProyecto_IdProyecto(Long idProyecto);
}
