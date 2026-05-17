package com.admision.service.procesamiento;

import com.admision.dto.EstadisticaProcesoResponse;
import com.admision.entity.ResultadoPostulante;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadisticaProcesoService {

    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    public EstadisticaProcesoResponse obtenerEstadisticas(Long procesoId) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId);

        if (resultados.isEmpty()) {
            return EstadisticaProcesoResponse.builder()
                    .procesoId(procesoId)
                    .totalPostulantes(0L)
                    .mayorPuntaje(BigDecimal.ZERO)
                    .menorPuntaje(BigDecimal.ZERO)
                    .promedioPuntaje(BigDecimal.ZERO)
                    .promedioCorrectas(BigDecimal.ZERO)
                    .promedioIncorrectas(BigDecimal.ZERO)
                    .promedioBlancas(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal mayorPuntaje = resultados.stream()
                .map(ResultadoPostulante::getPuntajeFinal)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal menorPuntaje = resultados.stream()
                .map(ResultadoPostulante::getPuntajeFinal)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal sumaPuntajes = resultados.stream()
                .map(ResultadoPostulante::getPuntajeFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumaCorrectas = resultados.stream()
                .map(r -> BigDecimal.valueOf(r.getCorrectas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumaIncorrectas = resultados.stream()
                .map(r -> BigDecimal.valueOf(r.getIncorrectas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumaBlancas = resultados.stream()
                .map(r -> BigDecimal.valueOf(r.getBlancas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = BigDecimal.valueOf(resultados.size());

        return EstadisticaProcesoResponse.builder()
                .procesoId(procesoId)
                .totalPostulantes((long) resultados.size())
                .mayorPuntaje(mayorPuntaje)
                .menorPuntaje(menorPuntaje)
                .promedioPuntaje(sumaPuntajes.divide(total, 4, RoundingMode.HALF_UP))
                .promedioCorrectas(sumaCorrectas.divide(total, 2, RoundingMode.HALF_UP))
                .promedioIncorrectas(sumaIncorrectas.divide(total, 2, RoundingMode.HALF_UP))
                .promedioBlancas(sumaBlancas.divide(total, 2, RoundingMode.HALF_UP))
                .build();
    }
}