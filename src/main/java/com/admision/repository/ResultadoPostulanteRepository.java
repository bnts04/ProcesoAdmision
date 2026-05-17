package com.admision.repository;

import com.admision.entity.ResultadoPostulante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoPostulanteRepository extends JpaRepository<ResultadoPostulante, Long> {

    List<ResultadoPostulante> findByProcesoId(Long procesoId);

    List<ResultadoPostulante> findByProcesoIdOrderByPuntajeFinalDesc(Long procesoId);

    void deleteByProcesoId(Long procesoId);

    long countByProcesoId(Long procesoId);
}