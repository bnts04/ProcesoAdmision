package com.admision.repository;

import com.admision.entity.ExamenGenerado;
import com.admision.entity.TemaExamen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemaExamenRepository extends JpaRepository<TemaExamen, Long> {

    List<TemaExamen> findByExamenOrderByLetraTemaAsc(ExamenGenerado examen);

    Optional<TemaExamen> findByExamenAndLetraTemaIgnoreCase(
            ExamenGenerado examen,
            String letraTema
    );

    long countByExamen(ExamenGenerado examen);

    void deleteByExamen(ExamenGenerado examen);
}