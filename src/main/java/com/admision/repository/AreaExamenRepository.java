package com.admision.repository;

import com.admision.entity.AreaExamen;
import com.admision.enums.CodigoAreaExamen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaExamenRepository extends JpaRepository<AreaExamen, Long> {

    Optional<AreaExamen> findByCodigo(CodigoAreaExamen codigo);

    boolean existsByCodigo(CodigoAreaExamen codigo);

    List<AreaExamen> findByActivoTrueOrderByIdAsc();
}