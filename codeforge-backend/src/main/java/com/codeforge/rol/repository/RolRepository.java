package com.codeforge.rol.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeforge.rol.entity.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}