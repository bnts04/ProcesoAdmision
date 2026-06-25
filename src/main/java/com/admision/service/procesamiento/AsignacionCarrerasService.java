package com.admision.service.procesamiento;

import com.admision.dto.AsignacionCarrerasResponse;
import com.admision.dto.PadronCarreraExcelResponse;
import com.admision.entity.ResultadoPostulante;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import com.admision.service.excel.PadronCarrerasExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AsignacionCarrerasService {

    private final PadronCarrerasExcelService padronCarrerasExcelService;
    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    @Transactional
    public AsignacionCarrerasResponse asignarCarrerasYFacultades(Long procesoId) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId);
        List<PadronCarreraExcelResponse> padron = padronCarrerasExcelService.leerPadronCarreras(procesoId, 0);

        Map<String, PadronCarreraExcelResponse> padronPorCodigo = new HashMap<>();

        for (PadronCarreraExcelResponse registro : padron) {
            String codigoNormalizado = normalizarCodigo(registro.getCodigo());

            if (!codigoNormalizado.isBlank()) {
                padronPorCodigo.put(codigoNormalizado, registro);
            }
        }

        int actualizados = 0;
        List<String> codigosNoEncontrados = new ArrayList<>();

        for (ResultadoPostulante resultado : resultados) {
            String codigoNormalizado = normalizarCodigo(resultado.getCodigo());

            PadronCarreraExcelResponse registroPadron = padronPorCodigo.get(codigoNormalizado);

            if (registroPadron == null) {
                resultado.setFacultad("PENDIENTE");
                resultado.setCarrera("PENDIENTE");
                resultado.setObservacion(
                        agregarObservacion(resultado.getObservacion(), "No se encontró carrera/facultad para el código")
                );

                codigosNoEncontrados.add(resultado.getCodigo());
                continue;
            }

            resultado.setFacultad(registroPadron.getFacultad());
            resultado.setCarrera(registroPadron.getCarrera());
            resultado.setObservacion(
                    agregarObservacion(resultado.getObservacion(), "Carrera y facultad asignadas correctamente")
            );

            actualizados++;
        }

        resultadoPostulanteRepository.saveAll(resultados);

        return AsignacionCarrerasResponse.builder()
                .procesoId(procesoId)
                .totalResultados(resultados.size())
                .totalPadron(padron.size())
                .totalActualizados(actualizados)
                .totalNoEncontrados(codigosNoEncontrados.size())
                .codigosNoEncontrados(codigosNoEncontrados)
                .mensaje(codigosNoEncontrados.isEmpty()
                        ? "Carreras y facultades asignadas correctamente a todos los resultados"
                        : "Carreras y facultades asignadas con algunos códigos no encontrados")
                .build();
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }

        String limpio = codigo.trim();

        if (limpio.endsWith(".0")) {
            limpio = limpio.substring(0, limpio.length() - 2);
        }

        return limpio;
    }

    private String agregarObservacion(String observacionActual, String nuevaObservacion) {
        if (observacionActual == null || observacionActual.isBlank()) {
            return nuevaObservacion;
        }

        if (observacionActual.contains(nuevaObservacion)) {
            return observacionActual;
        }

        return observacionActual + " | " + nuevaObservacion;
    }
}