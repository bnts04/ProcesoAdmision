package com.admision.service.procesamiento;

import com.admision.dto.OrdenMeritoResponse;
import com.admision.entity.ResultadoPostulante;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdenMeritoService {

    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    @Transactional
    public OrdenMeritoResponse calcularOrdenMerito(Long procesoId) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId);

        if (resultados.isEmpty()) {
            return OrdenMeritoResponse.builder()
                    .procesoId(procesoId)
                    .totalResultados(0)
                    .totalConOmg(0)
                    .totalConOme(0)
                    .mensaje("No existen resultados para calcular orden de mérito")
                    .build();
        }

        validarCarrerasAsignadas(resultados);

        calcularOmg(resultados);
        calcularOme(resultados);

        resultadoPostulanteRepository.saveAll(resultados);

        return OrdenMeritoResponse.builder()
                .procesoId(procesoId)
                .totalResultados(resultados.size())
                .totalConOmg((int) resultados.stream().filter(r -> r.getOmg() != null).count())
                .totalConOme((int) resultados.stream().filter(r -> r.getOme() != null).count())
                .mensaje("Orden de mérito general y por carrera calculado correctamente")
                .build();
    }

    private void calcularOmg(List<ResultadoPostulante> resultados) {
        List<ResultadoPostulante> ordenados = resultados.stream()
                .sorted(Comparator
                        .comparing(ResultadoPostulante::getPuntajeFinal, Comparator.reverseOrder())
                        .thenComparing(ResultadoPostulante::getCodigo))
                .toList();

        int orden = 0;
        BigDecimal ultimoPuntaje = null;

        for (ResultadoPostulante resultado : ordenados) {
            BigDecimal puntajeActual = resultado.getPuntajeFinal();

            if (ultimoPuntaje == null || puntajeActual.compareTo(ultimoPuntaje) != 0) {
                orden++;
                ultimoPuntaje = puntajeActual;
            }

            resultado.setOmg(orden);
        }
    }

    private void calcularOme(List<ResultadoPostulante> resultados) {
        Map<String, List<ResultadoPostulante>> resultadosPorCarrera = resultados.stream()
                .collect(Collectors.groupingBy(r -> normalizarTexto(r.getCarrera())));

        for (List<ResultadoPostulante> grupoCarrera : resultadosPorCarrera.values()) {
            List<ResultadoPostulante> ordenados = grupoCarrera.stream()
                    .sorted(Comparator
                            .comparing(ResultadoPostulante::getPuntajeFinal, Comparator.reverseOrder())
                            .thenComparing(ResultadoPostulante::getCodigo))
                    .toList();

            int orden = 0;
            BigDecimal ultimoPuntaje = null;

            for (ResultadoPostulante resultado : ordenados) {
                BigDecimal puntajeActual = resultado.getPuntajeFinal();

                if (ultimoPuntaje == null || puntajeActual.compareTo(ultimoPuntaje) != 0) {
                    orden++;
                    ultimoPuntaje = puntajeActual;
                }

                resultado.setOme(orden);
            }
        }
    }

    private void validarCarrerasAsignadas(List<ResultadoPostulante> resultados) {
        boolean existePendiente = resultados.stream()
                .anyMatch(r -> r.getCarrera() == null
                        || r.getCarrera().isBlank()
                        || r.getCarrera().equalsIgnoreCase("PENDIENTE"));

        if (existePendiente) {
            throw new RuntimeException("Existen resultados sin carrera asignada. Primero debe asignar carrera y facultad.");
        }
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim().toUpperCase();
    }
}