package com.admision.repository;

import com.admision.entity.ExamenGenerado;
import com.admision.entity.PdfExamenGenerado;
import com.admision.enums.TipoExamenPdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PdfExamenGeneradoRepository extends JpaRepository<PdfExamenGenerado, Long> {

    List<PdfExamenGenerado> findByExamenOrderByFechaGeneracionDesc(ExamenGenerado examen);

    List<PdfExamenGenerado> findByExamenAndTipo(ExamenGenerado examen, TipoExamenPdf tipo);

    Optional<PdfExamenGenerado> findByExamenAndLetraTemaAndTipo(
            ExamenGenerado examen,
            String letraTema,
            TipoExamenPdf tipo
    );

    boolean existsByExamenAndLetraTemaAndTipo(
            ExamenGenerado examen,
            String letraTema,
            TipoExamenPdf tipo
    );
}
