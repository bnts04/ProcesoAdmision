package com.admision.service.procesamiento;

import com.admision.dto.ClaveTemaResponse;
import com.admision.dto.PostulanteConRespuestasResponse;
import com.admision.dto.PuntajePostulanteResponse;
import com.admision.entity.ProcesoAdmision;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.service.excel.ClavesExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalculoPuntajeService {

    private final ClavesExcelService clavesExcelService;
    private final CruceIdentificacionRespuestasService cruceIdentificacionRespuestasService;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    private static final BigDecimal DEFAULT_PUNTAJE_CORRECTA = new BigDecimal("20.0000");
    private static final BigDecimal DEFAULT_PUNTAJE_INCORRECTA = new BigDecimal("-1.2500");
    private static final BigDecimal DEFAULT_PUNTAJE_BLANCA = new BigDecimal("1.2500");
    private static final BigDecimal DEFAULT_FACTOR_ESCALA = new BigDecimal("100.0000");

    public List<PuntajePostulanteResponse> calcularPuntajes(Long procesoId, Integer limite) {

        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        ConfiguracionPuntaje configuracion = obtenerConfiguracionPuntaje(proceso);

        List<ClaveTemaResponse> claves = clavesExcelService.leerClaves(procesoId);

        Map<String, ClaveTemaResponse> clavesPorTema = new HashMap<>();

        for (ClaveTemaResponse clave : claves) {
            String temaNormalizado = normalizarTexto(clave.getTema());

            if (!temaNormalizado.isBlank()) {
                clavesPorTema.put(temaNormalizado, clave);
            }
        }

        List<PostulanteConRespuestasResponse> postulantes =
                cruceIdentificacionRespuestasService.cruzarRespuestasConIdentificacion(procesoId, limite);

        return postulantes.stream()
                .map(postulante -> calcularPuntajePostulante(postulante, clavesPorTema, configuracion))
                .toList();
    }

    private PuntajePostulanteResponse calcularPuntajePostulante(
            PostulanteConRespuestasResponse postulante,
            Map<String, ClaveTemaResponse> clavesPorTema,
            ConfiguracionPuntaje configuracion
    ) {
        String temaNormalizado = normalizarTexto(postulante.getTema());

        if (postulante.getTemaValido() == null || !postulante.getTemaValido() || temaNormalizado.isBlank()) {
            return PuntajePostulanteResponse.builder()
                    .codigo(postulante.getCodigo())
                    .litho(postulante.getLitho())
                    .tema(postulante.getTema())
                    .secuencia(postulante.getSecuencia())
                    .correctas(0)
                    .incorrectas(0)
                    .blancas(0)
                    .puntajeBruto(BigDecimal.ZERO)
                    .puntajeFinal(BigDecimal.ZERO)
                    .puntajeCalculado(false)
                    .observacion(postulante.getObservacionTema() + " | No se calculó puntaje")
                    .build();
        }

        ClaveTemaResponse clave = clavesPorTema.get(temaNormalizado);

        if (clave == null) {
            return PuntajePostulanteResponse.builder()
                    .codigo(postulante.getCodigo())
                    .litho(postulante.getLitho())
                    .tema(postulante.getTema())
                    .secuencia(postulante.getSecuencia())
                    .correctas(0)
                    .incorrectas(0)
                    .blancas(0)
                    .puntajeBruto(BigDecimal.ZERO)
                    .puntajeFinal(BigDecimal.ZERO)
                    .puntajeCalculado(false)
                    .observacion("No se encontró clave para el tema " + postulante.getTema() + " | No se calculó puntaje")
                    .build();
        }

        int correctas = 0;
        int incorrectas = 0;
        int blancas = 0;

        for (int pregunta = 1; pregunta <= 100; pregunta++) {
            String nombrePregunta = String.format("PREG_%03d", pregunta);

            String respuestaPostulante = normalizarTexto(
                    postulante.getRespuestas().get(nombrePregunta)
            );

            String respuestaCorrecta = normalizarTexto(
                    clave.getRespuestas().get(nombrePregunta)
            );

            if (esRespuestaBlanca(respuestaPostulante)) {
                blancas++;
            } else if (respuestaPostulante.equals(respuestaCorrecta)) {
                correctas++;
            } else {
                incorrectas++;
            }
        }

        BigDecimal puntajeBruto = calcularPuntajeBruto(
                correctas,
                incorrectas,
                blancas,
                configuracion
        );

        BigDecimal puntajeFinal = puntajeBruto
                .divide(configuracion.getFactorEscala(), 4, RoundingMode.HALF_UP);

        return PuntajePostulanteResponse.builder()
                .codigo(postulante.getCodigo())
                .litho(postulante.getLitho())
                .tema(postulante.getTema())
                .secuencia(postulante.getSecuencia())
                .correctas(correctas)
                .incorrectas(incorrectas)
                .blancas(blancas)
                .puntajeBruto(puntajeBruto)
                .puntajeFinal(puntajeFinal)
                .puntajeCalculado(true)
                .observacion(
                        "Puntaje calculado correctamente | Configuración usada: correcta="
                                + configuracion.getPuntajeCorrecta()
                                + ", incorrecta="
                                + configuracion.getPuntajeIncorrecta()
                                + ", blanca="
                                + configuracion.getPuntajeBlanca()
                                + ", factor="
                                + configuracion.getFactorEscala()
                )
                .build();
    }

    private BigDecimal calcularPuntajeBruto(
            int correctas,
            int incorrectas,
            int blancas,
            ConfiguracionPuntaje configuracion
    ) {
        BigDecimal totalCorrectas = configuracion.getPuntajeCorrecta()
                .multiply(BigDecimal.valueOf(correctas));

        BigDecimal totalIncorrectas = configuracion.getPuntajeIncorrecta()
                .multiply(BigDecimal.valueOf(incorrectas));

        BigDecimal totalBlancas = configuracion.getPuntajeBlanca()
                .multiply(BigDecimal.valueOf(blancas));

        return totalCorrectas
                .add(totalIncorrectas)
                .add(totalBlancas)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private ConfiguracionPuntaje obtenerConfiguracionPuntaje(ProcesoAdmision proceso) {
        BigDecimal puntajeCorrecta = valorODefecto(
                proceso.getPuntajeCorrecta(),
                DEFAULT_PUNTAJE_CORRECTA
        );

        BigDecimal puntajeIncorrecta = valorODefecto(
                proceso.getPuntajeIncorrecta(),
                DEFAULT_PUNTAJE_INCORRECTA
        );

        BigDecimal puntajeBlanca = valorODefecto(
                proceso.getPuntajeBlanca(),
                DEFAULT_PUNTAJE_BLANCA
        );

        BigDecimal factorEscala = valorODefecto(
                proceso.getFactorEscala(),
                DEFAULT_FACTOR_ESCALA
        );

        if (factorEscala.compareTo(BigDecimal.ZERO) <= 0) {
            factorEscala = DEFAULT_FACTOR_ESCALA;
        }

        return new ConfiguracionPuntaje(
                puntajeCorrecta,
                puntajeIncorrecta,
                puntajeBlanca,
                factorEscala
        );
    }

    private BigDecimal valorODefecto(BigDecimal valor, BigDecimal defecto) {
        return valor != null ? valor : defecto;
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim().toUpperCase();
    }

    private boolean esRespuestaBlanca(String respuesta) {
        return respuesta == null || respuesta.isBlank();
    }

    private static class ConfiguracionPuntaje {

        private final BigDecimal puntajeCorrecta;
        private final BigDecimal puntajeIncorrecta;
        private final BigDecimal puntajeBlanca;
        private final BigDecimal factorEscala;

        public ConfiguracionPuntaje(
                BigDecimal puntajeCorrecta,
                BigDecimal puntajeIncorrecta,
                BigDecimal puntajeBlanca,
                BigDecimal factorEscala
        ) {
            this.puntajeCorrecta = puntajeCorrecta;
            this.puntajeIncorrecta = puntajeIncorrecta;
            this.puntajeBlanca = puntajeBlanca;
            this.factorEscala = factorEscala;
        }

        public BigDecimal getPuntajeCorrecta() {
            return puntajeCorrecta;
        }

        public BigDecimal getPuntajeIncorrecta() {
            return puntajeIncorrecta;
        }

        public BigDecimal getPuntajeBlanca() {
            return puntajeBlanca;
        }

        public BigDecimal getFactorEscala() {
            return factorEscala;
        }
    }
}