package com.codeforge.invitacion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeforge.invitacion.entity.Invitacion;

public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {

    List<Invitacion> findByReceptor_IdUsuario(Long idUsuario);

    List<Invitacion> findByEmisor_IdUsuario(Long idUsuario);

    List<Invitacion> findByProyecto_IdProyecto(Long idProyecto);

    List<Invitacion> findByReceptor_IdUsuarioAndEstado(
            Long idUsuario,
            String estado
    );
}