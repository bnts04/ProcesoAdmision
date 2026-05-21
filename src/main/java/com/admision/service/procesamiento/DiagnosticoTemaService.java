package com.admision.service.procesamiento;

import com.admision.dto.DiagnosticoTemaResponse;
import com.admision.dto.PostulanteConRespuestasResponse;
import com.admision.repository.ProcesoAdmisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosticoTemaService {

    private final CruceIdentificacionRespuestasService cruceIdentificacionRespuestasService;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    public DiagnosticoTemaResponse diagnosticarTemas(Long procesoId) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        List<PostulanteConRespuestasResponse> registros =
                cruceIdentificacionRespuestasService.cruzarRespuestasConIdentificacion(procesoId, 0);

        int temasValidos = 0;
        int temasInvalidos = 0;

        int temaValidadoEnAmbos = 0;
        int temaTomadoDesdeRespuest = 0;
        int temaTomadoDesdeIdentifi = 0;

        int conflictosTema = 0;
        int sinTemaEnAmbos = 0;
        int lithoNoEncontrado = 0;

        List<String> observaciones = new ArrayList<>();

        for (PostulanteConRespuestasResponse registro : registros) {
            String observacion = registro.getObservacionTema() == null
                    ? ""
                    : registro.getObservacionTema();

            if (Boolean.TRUE.equals(registro.getTemaValido())) {
                temasValidos++;
            } else {
                temasInvalidos++;
            }

            if (observacion.contains("Tema validado correctamente")) {
                temaValidadoEnAmbos++;
            }

            if (observacion.contains("Tema tomado desde RESPUEST")) {
                temaTomadoDesdeRespuest++;
            }

            if (observacion.contains("Tema tomado desde IDENTIFI")) {
                temaTomadoDesdeIdentifi++;

                observaciones.add(
                        "LITHO " + registro.getLitho()
                                + " | CODIGO " + registro.getCodigo()
                                + " | RESPUEST.TEMA vacío"
                                + " | IDENTIFI.TEMA " + registro.getTemaIdentifi()
                                + " | Se usó TEMA " + registro.getTema()
                );
            }

            if (observacion.contains("Conflicto de TEMA")) {
                conflictosTema++;

                observaciones.add(
                        "LITHO " + registro.getLitho()
                                + " | CODIGO " + registro.getCodigo()
                                + " | " + observacion
                );
            }

            if (observacion.contains("No se encontró TEMA")) {
                sinTemaEnAmbos++;

                observaciones.add(
                        "LITHO " + registro.getLitho()
                                + " | CODIGO " + registro.getCodigo()
                                + " | " + observacion
                );
            }

            if (observacion.contains("No se encontró LITHO")) {
                lithoNoEncontrado++;

                observaciones.add(
                        "LITHO " + registro.getLitho()
                                + " | " + observacion
                );
            }
        }

        String mensaje = temasInvalidos == 0
                ? "Diagnóstico de TEMA correcto. No hay conflictos críticos."
                : "Diagnóstico de TEMA con observaciones. Revisar registros inválidos.";

        return DiagnosticoTemaResponse.builder()
                .procesoId(procesoId)
                .totalRegistros(registros.size())
                .temasValidos(temasValidos)
                .temasInvalidos(temasInvalidos)
                .temaValidadoEnAmbos(temaValidadoEnAmbos)
                .temaTomadoDesdeRespuest(temaTomadoDesdeRespuest)
                .temaTomadoDesdeIdentifi(temaTomadoDesdeIdentifi)
                .conflictosTema(conflictosTema)
                .sinTemaEnAmbos(sinTemaEnAmbos)
                .lithoNoEncontrado(lithoNoEncontrado)
                .observaciones(observaciones)
                .mensaje(mensaje)
                .build();
    }
}