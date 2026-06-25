package com.admision.repository;

import com.admision.entity.PreguntaBanco;
import com.admision.enums.ComponentePregunta;
import com.admision.enums.EstadoPregunta;
import com.admision.enums.SubcursoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreguntaBancoRepository extends JpaRepository<PreguntaBanco, Long> {

    List<PreguntaBanco> findAllByOrderByFechaRegistroDesc();

    List<PreguntaBanco> findByEstadoOrderByFechaRegistroDesc(
            EstadoPregunta estado
    );

    List<PreguntaBanco> findByComponenteAndEstadoOrderByFechaRegistroDesc(
            ComponentePregunta componente,
            EstadoPregunta estado
    );

    List<PreguntaBanco> findByComponenteAndSubcursoAndEstadoOrderByFechaRegistroDesc(
            ComponentePregunta componente,
            SubcursoPregunta subcurso,
            EstadoPregunta estado
    );

    long countByEstado(
            EstadoPregunta estado
    );

    long countByComponenteAndEstado(
            ComponentePregunta componente,
            EstadoPregunta estado
    );

    long countByComponenteAndSubcursoAndEstado(
            ComponentePregunta componente,
            SubcursoPregunta subcurso,
            EstadoPregunta estado
    );

    /**
     * Detecta si ya existe una pregunta con el mismo enunciado,
     * ignorando mayúsculas, minúsculas y espacios al inicio o final.
     */
    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM PreguntaBanco p
            WHERE LOWER(TRIM(p.enunciado)) = LOWER(TRIM(:enunciado))
            """)
    boolean existsByEnunciadoNormalizado(
            @Param("enunciado") String enunciado
    );
}