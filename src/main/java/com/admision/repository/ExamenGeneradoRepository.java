package com.admision.repository;

import com.admision.entity.ExamenGenerado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamenGeneradoRepository extends JpaRepository<ExamenGenerado, Long> {

    List<ExamenGenerado> findAllByOrderByFechaGeneracionDesc();
}