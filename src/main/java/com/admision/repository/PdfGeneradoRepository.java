package com.admision.repository;

import com.admision.entity.PdfGenerado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdfGeneradoRepository extends JpaRepository<PdfGenerado, Long> {

    List<PdfGenerado> findByProcesoIdOrderByFechaGeneracionDesc(Long procesoId);
}