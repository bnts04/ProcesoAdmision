package com.admision.repository;

import com.admision.entity.TemaAlternativa;
import com.admision.entity.TemaPregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemaAlternativaRepository extends JpaRepository<TemaAlternativa, Long> {

    List<TemaAlternativa> findByTemaPreguntaOrderByLetraFinalAsc(
            TemaPregunta temaPregunta
    );

    void deleteByTemaPregunta(TemaPregunta temaPregunta);
}