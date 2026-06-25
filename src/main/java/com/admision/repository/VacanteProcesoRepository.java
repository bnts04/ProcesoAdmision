package com.admision.repository;

import com.admision.entity.VacanteProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VacanteProcesoRepository extends JpaRepository<VacanteProceso, Long> {

    List<VacanteProceso> findByProcesoIdOrderByFacultadAscCarreraAsc(Long procesoId);

    Optional<VacanteProceso> findByProcesoIdAndCarreraIgnoreCase(Long procesoId, String carrera);

    boolean existsByProcesoId(Long procesoId);

    void deleteByProcesoId(Long procesoId);
}