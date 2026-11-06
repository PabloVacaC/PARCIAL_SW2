package com.codeforge.ia.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codeforge.ia.entity.IaMensaje;

@Repository
public interface IaMensajeRepository extends JpaRepository<IaMensaje, Long> {

    List<IaMensaje> findByConversacion_IdConversacionOrderByFechaEnvioAsc(Long idConversacion);

    List<IaMensaje> findByConversacion_IdConversacionOrderByFechaEnvioDesc(Long idConversacion, Pageable pageable);
}
