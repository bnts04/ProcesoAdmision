package com.admision.repository;

import com.admision.entity.ProcesoAdmision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcesoAdmisionRepository extends JpaRepository<ProcesoAdmision, Long> {
}