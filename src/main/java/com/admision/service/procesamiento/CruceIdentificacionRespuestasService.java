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

        List<RespuestaPostulanteExcelResponse> respuestas =
                respuestasExcelService.leerRespuestas(procesoId, limite);

        /*
         * Leemos todas las identificaciones porque el orden de RESPUEST
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
                ResultadoTema resultadoTema = resolverTema(
                        respuesta.getTema(),
                        null
                );

                resultado.add(PostulanteConRespuestasResponse.builder()
                        .codigo("NO_ENCONTRADO")
                        .litho(respuesta.getLitho())
                        .tema(resultadoTema.temaFinal())
                        .temaRespuest(respuesta.getTema())
                        .temaIdentifi(null)
                        .secuencia(null)
                        .totalPreguntas(respuesta.getTotalPreguntas())
                        .respuestas(respuesta.getRespuestas())
                        .temaValido(false)
                        .observacionTema("No se encontró LITHO en IDENTIFI")
                        .build());

                continue;
            }

            ResultadoTema resultadoTema = resolverTema(
                    respuesta.getTema(),
                    identificacion.getTema()
            );

            resultado.add(PostulanteConRespuestasResponse.builder()
                    .codigo(identificacion.getCodigo())
                    .litho(respuesta.getLitho())
                    .tema(resultadoTema.temaFinal())
                    .temaRespuest(respuesta.getTema())
                    .temaIdentifi(identificacion.getTema())
                    .secuencia(identificacion.getSecuencia())
                    .totalPreguntas(respuesta.getTotalPreguntas())
                    .respuestas(respuesta.getRespuestas())
                    .temaValido(resultadoTema.valido())
                    .observacionTema(resultadoTema.observacion())
                    .build());
        }

        return resultado;
    }

    private ResultadoTema resolverTema(String temaRespuest, String temaIdentifi) {
        String temaResp = normalizarTexto(temaRespuest);
        String temaIden = normalizarTexto(temaIdentifi);

        if (!temaResp.isBlank() && !temaIden.isBlank()) {
            if (temaResp.equals(temaIden)) {
                return new ResultadoTema(
                        temaResp,
                        true,
                        "Tema validado correctamente en RESPUEST e IDENTIFI"
                );
            }

            return new ResultadoTema(
                    "",
                    false,
                    "Conflicto de TEMA: RESPUEST=" + temaResp + ", IDENTIFI=" + temaIden
            );
        }

        if (!temaResp.isBlank()) {
            return new ResultadoTema(
                    temaResp,
                    true,
                    "Tema tomado desde RESPUEST"
            );
        }

        if (!temaIden.isBlank()) {
            return new ResultadoTema(
                    temaIden,
                    true,
                    "Tema tomado desde IDENTIFI porque RESPUEST estaba vacío"
            );
        }

        return new ResultadoTema(
                "",
                false,
                "No se encontró TEMA en RESPUEST ni en IDENTIFI"
        );
    }

    private String normalizarLitho(String litho) {
        if (litho == null) {
            return "";
        }

        return litho.trim();
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim().toUpperCase();
    }

    private record ResultadoTema(
            String temaFinal,
            boolean valido,
            String observacion
    ) {
    }
}