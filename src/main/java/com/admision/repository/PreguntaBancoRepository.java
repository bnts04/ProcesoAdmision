package com.admision.repository;

import com.admision.entity.PreguntaBanco;
import com.admision.enums.ComponentePregunta;
import com.admision.enums.EstadoPregunta;
import com.admision.enums.SubcursoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreguntaBancoRepository extends JpaRepository<PreguntaBanco, Long> {

    List<PreguntaBanco> findAllByOrderByFechaRegistroDesc();

    List<PreguntaBanco> findByEstadoOrderByFechaRegistroDesc(EstadoPregunta estado);

    List<PreguntaBanco> findByComponenteAndEstadoOrderByFechaRegistroDesc(
            ComponentePregunta componente,
            EstadoPregunta estado
    );

    List<PreguntaBanco> findByComponenteAndSubcursoAndEstadoOrderByFechaRegistroDesc(
            ComponentePregunta componente,
            SubcursoPregunta subcurso,
            EstadoPregunta estado
    );

    long countByEstado(EstadoPregunta estado);

    long countByComponenteAndEstado(
            ComponentePregunta componente,
            EstadoPregunta estado
    );

    long countByComponenteAndSubcursoAndEstado(
            ComponentePregunta componente,
            SubcursoPregunta subcurso,
            EstadoPregunta estado
    );
}