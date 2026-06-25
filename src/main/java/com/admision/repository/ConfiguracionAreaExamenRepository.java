package com.admision.repository;

import com.admision.entity.AreaExamen;
import com.admision.entity.ConfiguracionAreaExamen;
import com.admision.enums.CodigoAreaExamen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfiguracionAreaExamenRepository extends JpaRepository<ConfiguracionAreaExamen, Long> {

    List<ConfiguracionAreaExamen> findByAreaOrderByIdAsc(AreaExamen area);

    List<ConfiguracionAreaExamen> findByArea_CodigoOrderByIdAsc(CodigoAreaExamen codigo);

    long countByArea(AreaExamen area);
}