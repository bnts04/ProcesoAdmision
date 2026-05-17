package com.admision.service.procesamiento;

import com.admision.dto.IdentificacionPostulanteExcelResponse;
import com.admision.dto.PostulanteConRespuestasResponse;
import com.admision.dto.RespuestaPostulanteExcelResponse;
import com.admision.service.excel.IdentificacionExcelService;
import com.admision.service.excel.RespuestasExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CruceIdentificacionRespuestasService {

    private final RespuestasExcelService respuestasExcelService;
    private final IdentificacionExcelService identificacionExcelService;

    public List<PostulanteConRespuestasResponse> cruzarRespuestasConIdentificacion(Long procesoId, Integer limite) {

        List<RespuestaPostulanteExcelResponse> respuestas = respuestasExcelService.leerRespuestas(procesoId, limite);

        /*
         * Aquí leemos todas las identificaciones porque el orden de RESPUEST
         * y el orden de IDENTIFI no necesariamente coinciden.
         */
        List<IdentificacionPostulanteExcelResponse> identificaciones =
                identificacionExcelService.leerIdentificaciones(procesoId, 0);

        Map<String, IdentificacionPostulanteExcelResponse> identificacionPorLitho = new HashMap<>();

        for (IdentificacionPostulanteExcelResponse identificacion : identificaciones) {
            String lithoNormalizado = normalizarLitho(identificacion.getLitho());

            if (!lithoNormalizado.isBlank()) {
                identificacionPorLitho.put(lithoNormalizado, identificacion);
            }
        }

        List<PostulanteConRespuestasResponse> resultado = new ArrayList<>();

        for (RespuestaPostulanteExcelResponse respuesta : respuestas) {
            String lithoNormalizado = normalizarLitho(respuesta.getLitho());

            IdentificacionPostulanteExcelResponse identificacion =
                    identificacionPorLitho.get(lithoNormalizado);

            if (identificacion == null) {
                resultado.add(PostulanteConRespuestasResponse.builder()
                        .codigo("NO_ENCONTRADO")
                        .litho(respuesta.getLitho())
                        .tema(respuesta.getTema())
                        .secuencia(null)
                        .totalPreguntas(respuesta.getTotalPreguntas())
                        .respuestas(respuesta.getRespuestas())
                        .build());
                continue;
            }

            resultado.add(PostulanteConRespuestasResponse.builder()
                    .codigo(identificacion.getCodigo())
                    .litho(respuesta.getLitho())
                    .tema(respuesta.getTema())
                    .secuencia(identificacion.getSecuencia())
                    .totalPreguntas(respuesta.getTotalPreguntas())
                    .respuestas(respuesta.getRespuestas())
                    .build());
        }

        return resultado;
    }

    private String normalizarLitho(String litho) {
        if (litho == null) {
            return "";
        }

        return litho.trim();
    }
}