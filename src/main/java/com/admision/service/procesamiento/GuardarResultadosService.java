package com.admision.service.procesamiento;

import com.admision.dto.PuntajePostulanteResponse;
import com.admision.dto.ResultadoGuardadoResponse;
import com.admision.entity.ProcesoAdmision;
import com.admision.entity.ResultadoPostulante;
import com.admision.enums.CondicionPostulante;
import com.admision.enums.EstadoProceso;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuardarResultadosService {

    private final CalculoPuntajeService calculoPuntajeService;
    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    @Transactional
    public ResultadoGuardadoResponse guardarResultadosCalculados(Long procesoId, Integer limite) {
        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        List<PuntajePostulanteResponse> puntajes = calculoPuntajeService.calcularPuntajes(procesoId, limite);

        resultadoPostulanteRepository.deleteByProcesoId(procesoId);

        List<ResultadoPostulante> resultados = puntajes.stream()
                .map(p -> ResultadoPostulante.builder()
                        .proceso(proceso)
                        .codigo(p.getCodigo())
                        .dni(p.getCodigo())
                        .apellidosNombres("PENDIENTE")
                        .facultad("PENDIENTE")
                        .carrera("PENDIENTE")
                        .litho(p.getLitho())
                        .tema(p.getTema())
                        .secuencia(p.getSecuencia())
                        .correctas(p.getCorrectas())
                        .incorrectas(p.getIncorrectas())
                        .blancas(p.getBlancas())
                        .puntajeBruto(p.getPuntajeBruto())
                        .puntajeFinal(p.getPuntajeFinal())
                        .ome(null)
                        .omg(null)
                        .condicion(CondicionPostulante.PENDIENTE)
                        .observacion(p.getObservacion())
                        .build())
                .toList();

        resultadoPostulanteRepository.saveAll(resultados);

        proceso.setTotalPostulantes(resultados.size());
        proceso.setEstado(EstadoProceso.PROCESANDO);
        procesoAdmisionRepository.save(proceso);

        return ResultadoGuardadoResponse.builder()
                .procesoId(procesoId)
                .totalGuardados(resultados.size())
                .mensaje("Resultados calculados guardados correctamente")
                .build();
    }
}