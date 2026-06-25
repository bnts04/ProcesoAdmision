package com.admision.service.pdf;

import com.admision.dto.ActualizacionDesdePdfResponse;
import com.admision.dto.PostulantePdfGuiaResponse;
import com.admision.entity.ResultadoPostulante;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActualizacionDesdePdfService {

    private final PdfGuiaLecturaService pdfGuiaLecturaService;
    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    @Transactional
    public ActualizacionDesdePdfResponse actualizarDatosDesdePdf(Long procesoId) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId);
        List<PostulantePdfGuiaResponse> registrosPdf = pdfGuiaLecturaService.obtenerTodosLosRegistros(procesoId);

        Map<String, PostulantePdfGuiaResponse> pdfPorCodigo = registrosPdf.stream()
                .collect(Collectors.toMap(
                        r -> normalizarCodigo(r.getCodigo()),
                        r -> r,
                        (r1, r2) -> r1
                ));

        int actualizados = 0;
        List<String> codigosNoEncontrados = new ArrayList<>();

        for (ResultadoPostulante resultado : resultados) {
            String codigo = normalizarCodigo(resultado.getCodigo());

            PostulantePdfGuiaResponse registroPdf = pdfPorCodigo.get(codigo);

            if (registroPdf == null) {
                codigosNoEncontrados.add(resultado.getCodigo());

                resultado.setObservacion(agregarObservacion(
                        resultado.getObservacion(),
                        "No se encontró el código en el PDF guía"
                ));

                continue;
            }

            resultado.setApellidosNombres(registroPdf.getNombre());
            resultado.setFacultad(normalizarTextoSalida(registroPdf.getFacultad()));
            resultado.setCarrera(normalizarTextoSalida(registroPdf.getCarrera()));

            resultado.setObservacion(agregarObservacion(
                    resultado.getObservacion(),
                    "Datos completados desde PDF guía"
            ));

            actualizados++;
        }

        resultadoPostulanteRepository.saveAll(resultados);

        return ActualizacionDesdePdfResponse.builder()
                .procesoId(procesoId)
                .totalResultados(resultados.size())
                .totalRegistrosPdf(registrosPdf.size())
                .totalActualizados(actualizados)
                .totalNoEncontrados(codigosNoEncontrados.size())
                .codigosNoEncontrados(codigosNoEncontrados)
                .mensaje(codigosNoEncontrados.isEmpty()
                        ? "Datos actualizados correctamente desde PDF guía"
                        : "Datos actualizados con algunos códigos no encontrados en PDF guía")
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

    private String normalizarTextoSalida(String texto) {
        if (texto == null) {
            return "PENDIENTE";
        }

        String limpio = texto.trim().replaceAll("\\s+", " ");

        if (limpio.isBlank()) {
            return "PENDIENTE";
        }

        return limpio.toUpperCase();
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