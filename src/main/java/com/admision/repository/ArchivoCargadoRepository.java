package com.admision.repository;

import com.admision.entity.ArchivoCargado;
import com.admision.enums.TipoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface ArchivoCargadoRepository extends JpaRepository<ArchivoCargado, Long> {

    List<ArchivoCargado> findByProcesoId(Long procesoId);

    boolean existsByProcesoIdAndTipoArchivo(Long procesoId, TipoArchivo tipoArchivo);

    Optional<ArchivoCargado> findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(
            Long procesoId,
            TipoArchivo tipoArchivo
    );

    Optional<ArchivoCargado> findTopByTipoArchivoOrderByFechaCargaDesc(TipoArchivo tipoArchivo);
}