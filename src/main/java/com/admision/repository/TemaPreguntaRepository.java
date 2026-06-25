package com.admision.repository;

import com.admision.entity.TemaExamen;
import com.admision.entity.TemaPregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemaPreguntaRepository extends JpaRepository<TemaPregunta, Long> {

    List<TemaPregunta> findByTemaOrderByNumeroPreguntaAsc(TemaExamen tema);

    Optional<TemaPregunta> findByTemaAndNumeroPregunta(
            TemaExamen tema,
            Integer numeroPregunta
    );

    long countByTema(TemaExamen tema);

    void deleteByTema(TemaExamen tema);
}