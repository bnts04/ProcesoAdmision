package com.admision.repository;

import com.admision.entity.ExamenGenerado;
import com.admision.entity.ExamenPreguntaBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamenPreguntaBaseRepository extends JpaRepository<ExamenPreguntaBase, Long> {

    List<ExamenPreguntaBase> findByExamenOrderByOrdenBaseAsc(ExamenGenerado examen);

    long countByExamen(ExamenGenerado examen);

    void deleteByExamen(ExamenGenerado examen);
}