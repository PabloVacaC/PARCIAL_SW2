package com.codeforge.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeforge.usuario.entity.UsuarioRol;
import com.codeforge.usuario.entity.UsuarioRolId;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {
}