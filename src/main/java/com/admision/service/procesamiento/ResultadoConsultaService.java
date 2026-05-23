package com.admision.service.procesamiento;

import com.admision.dto.ResultadoPostulanteVistaResponse;
import com.admision.dto.ResumenCarreraResponse;
import com.admision.entity.CarreraVacante;
import com.admision.entity.ResultadoPostulante;
import com.admision.enums.CondicionPostulante;
import com.admision.repository.CarreraVacanteRepository;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultadoConsultaService {

    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final CarreraVacanteRepository carreraVacanteRepository;

    public List<ResultadoPostulanteVistaResponse> listarResultadosPorProceso(Long procesoId, Integer limite) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        int maximo = limite != null && limite > 0 ? limite : Integer.MAX_VALUE;

        return resultadoPostulanteRepository.findByProcesoIdOrderByPuntajeFinalDesc(procesoId)
                .stream()
                .limit(maximo)
                .map(resultado -> ResultadoPostulanteVistaResponse.fromEntity(resultado, procesoId))
                .toList();
    }

    public List<ResultadoPostulanteVistaResponse> listarResultadosPorCarrera(
            Long procesoId,
            String nombreCarrera,
            Integer limite
    ) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        if (nombreCarrera == null || nombreCarrera.isBlank()) {
            throw new RuntimeException("Debe enviar el nombre de la carrera");
        }

        int maximo = limite != null && limite > 0 ? limite : Integer.MAX_VALUE;
        String carreraBuscada = normalizarTexto(nombreCarrera);

        return resultadoPostulanteRepository.findByProcesoIdOrderByPuntajeFinalDesc(procesoId)
                .stream()
                .filter(r -> normalizarTexto(r.getCarrera()).equals(carreraBuscada))
                .limit(maximo)
                .map(resultado -> ResultadoPostulanteVistaResponse.fromEntity(resultado, procesoId))
                .toList();
    }

    public List<ResumenCarreraResponse> obtenerResumenPorCarreras(Long procesoId) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId);

        Map<String, CarreraVacante> vacantesPorCarrera = carreraVacanteRepository
                .findByActivoTrueOrderByFacultadAscCarreraAsc()
                .stream()
                .collect(Collectors.toMap(
                        v -> normalizarTexto(v.getCarrera()),
                        v -> v,
                        (v1, v2) -> v1
                ));

        Map<String, List<ResultadoPostulante>> agrupadoPorCarrera = resultados.stream()
                .filter(r -> r.getCarrera() != null && !r.getCarrera().isBlank())
                .filter(r -> !r.getCarrera().equalsIgnoreCase("PENDIENTE"))
                .collect(Collectors.groupingBy(r -> normalizarTexto(r.getCarrera())));

        return agrupadoPorCarrera.values()
                .stream()
                .map(grupo -> crearResumenCarrera(grupo, vacantesPorCarrera))
                .sorted(Comparator.comparing(ResumenCarreraResponse::getCarrera))
                .toList();
    }

    private ResumenCarreraResponse crearResumenCarrera(
            List<ResultadoPostulante> resultadosCarrera,
            Map<String, CarreraVacante> vacantesPorCarrera
    ) {
        ResultadoPostulante primero = resultadosCarrera.get(0);

        BigDecimal mayorPuntaje = resultadosCarrera.stream()
                .map(ResultadoPostulante::getPuntajeFinal)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal menorPuntaje = resultadosCarrera.stream()
                .map(ResultadoPostulante::getPuntajeFinal)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal sumaPuntajes = resultadosCarrera.stream()
                .map(ResultadoPostulante::getPuntajeFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedio = sumaPuntajes.divide(
                BigDecimal.valueOf(resultadosCarrera.size()),
                4,
                RoundingMode.HALF_UP
        );

        int ingresantes = (int) resultadosCarrera.stream()
                .filter(r -> r.getCondicion() == CondicionPostulante.INGRESO)
                .count();

        int noIngresantes = (int) resultadosCarrera.stream()
                .filter(r -> r.getCondicion() == CondicionPostulante.NO_INGRESO)
                .count();

        int pendientes = (int) resultadosCarrera.stream()
                .filter(r -> r.getCondicion() == CondicionPostulante.PENDIENTE)
                .count();

        CarreraVacante vacante = vacantesPorCarrera.get(normalizarTexto(primero.getCarrera()));

        return ResumenCarreraResponse.builder()
                .facultad(primero.getFacultad())
                .carrera(primero.getCarrera())
                .vacantes(vacante != null ? vacante.getVacantes() : 0)
                .totalPostulantes(resultadosCarrera.size())
                .totalIngresantes(ingresantes)
                .totalNoIngresantes(noIngresantes)
                .totalPendientes(pendientes)
                .mayorPuntaje(mayorPuntaje)
                .menorPuntaje(menorPuntaje)
                .promedioPuntaje(promedio)
                .build();
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor.trim().toUpperCase();

        limpio = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        limpio = limpio.replaceAll("\\s+", " ");

        return limpio;
    }

    public ResultadoPostulanteVistaResponse buscarResultadoPorCodigo(Long procesoId, String codigo) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        if (codigo == null || codigo.isBlank()) {
            throw new RuntimeException("Debe enviar el código del postulante");
        }

        ResultadoPostulante resultado = resultadoPostulanteRepository
                .findByProcesoIdAndCodigo(procesoId, codigo.trim())
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró postulante con código " + codigo + " en el proceso " + procesoId
                ));

        return ResultadoPostulanteVistaResponse.fromEntity(resultado, procesoId);
    }
}