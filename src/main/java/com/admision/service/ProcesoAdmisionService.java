package com.admision.service;

import com.admision.dto.CrearProcesoRequest;
import com.admision.entity.ProcesoAdmision;
import com.admision.enums.EstadoProceso;
import com.admision.repository.ProcesoAdmisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesoAdmisionService {

    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    public ProcesoAdmision crearProceso(CrearProcesoRequest request) {
        ProcesoAdmision proceso = ProcesoAdmision.builder()
                .nombreProceso(request.getNombreProceso())
                .modalidad(request.getModalidad())
                .estado(EstadoProceso.PENDIENTE)
                .totalPostulantes(0)
                .totalIngresantes(0)
                .totalNoIngresantes(0)
                .codigoVerificacion(generarCodigoVerificacion(request.getNombreProceso()))
                .build();

        return procesoAdmisionRepository.save(proceso);
    }

    public List<ProcesoAdmision> listarProcesos() {
        return procesoAdmisionRepository.findAll();
    }

    public ProcesoAdmision obtenerProcesoPorId(Long id) {
        return procesoAdmisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));
    }

    private String generarCodigoVerificacion(String nombreProceso) {
        String limpio = nombreProceso
                .toUpperCase()
                .replace("ADMISIÓN", "ADM")
                .replace("ADMISION", "ADM")
                .replaceAll("[^A-Z0-9]", "-")
                .replaceAll("-+", "-");

        return limpio + "-0001";
    }
}