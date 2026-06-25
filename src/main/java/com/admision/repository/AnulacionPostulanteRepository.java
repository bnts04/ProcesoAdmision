package com.admision.repository;

import com.admision.entity.AnulacionPostulante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnulacionPostulanteRepository extends JpaRepository<AnulacionPostulante, Long> {

    List<AnulacionPostulante> findByProcesoIdOrderByFechaAnulacionDesc(Long procesoId);

    Optional<AnulacionPostulante> findByProcesoIdAndCodigo(Long procesoId, String codigo);

    boolean existsByResultadoPostulanteId(Long resultadoPostulanteId);
}