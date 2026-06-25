package com.admision.repository;

import com.admision.entity.CarreraVacante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarreraVacanteRepository extends JpaRepository<CarreraVacante, Long> {

    List<CarreraVacante> findByActivoTrueOrderByFacultadAscCarreraAsc();

    Optional<CarreraVacante> findByCarreraIgnoreCaseAndActivoTrue(String carrera);
}