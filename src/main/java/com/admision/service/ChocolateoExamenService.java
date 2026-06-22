package com.admision.service;

import com.admision.dto.tema.*;
import com.admision.entity.*;
import com.admision.enums.EstadoExamenGenerado;
import com.admision.enums.LetraAlternativa;
import com.admision.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChocolateoExamenService {

    private final ExamenGeneradoRepository examenGeneradoRepository;
    private final ExamenPreguntaBaseRepository examenPreguntaBaseRepository;
    private final TemaExamenRepository temaExamenRepository;
    private final TemaPreguntaRepository temaPreguntaRepository;
    private final TemaAlternativaRepository temaAlternativaRepository;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public List<TemaExamen> generarTemas(ExamenGenerado examen) {
        if (temaExamenRepository.countByExamen(examen) > 0) {
            throw new IllegalStateException("El examen ya tiene temas generados.");
        }

        List<ExamenPreguntaBase> preguntasBase =
                examenPreguntaBaseRepository.findByExamenOrderByOrdenBaseAsc(examen);

        if (preguntasBase.size() != examen.getTotalPreguntas()) {
            throw new IllegalStateException(
                    "La cantidad de preguntas base no coincide con el total del examen."
            );
        }

        List<String> letrasTemas = generarLetrasTemas(
                examen.getTemaInicial(),
                examen.getCantidadTemas()
        );

        List<TemaExamen> temasGenerados = new ArrayList<>();

        for (String letraTema : letrasTemas) {
            TemaExamen tema = TemaExamen.builder()
                    .examen(examen)
                    .letraTema(letraTema)
                    .totalPreguntas(examen.getTotalPreguntas())
                    .build();

            tema = temaExamenRepository.save(tema);

            List<ExamenPreguntaBase> preguntasMezcladas =
                    new ArrayList<>(preguntasBase);

            Collections.shuffle(preguntasMezcladas, random);

            int numeroPregunta = 1;

            for (ExamenPreguntaBase preguntaBase : preguntasMezcladas) {
                crearPreguntaChocolateada(
                        tema,
                        preguntaBase.getPregunta(),
                        numeroPregunta
                );

                numeroPregunta++;
            }

            temasGenerados.add(tema);
        }

        examen.setEstado(EstadoExamenGenerado.TEMAS_GENERADOS);
        examenGeneradoRepository.save(examen);

        return temasGenerados;
    }

    private void crearPreguntaChocolateada(
            TemaExamen tema,
            PreguntaBanco preguntaOrigen,
            Integer numeroPregunta
    ) {
        List<AlternativaPregunta> alternativasOriginales =
                new ArrayList<>(preguntaOrigen.getAlternativas());

        validarAlternativasOriginales(preguntaOrigen, alternativasOriginales);

        Collections.shuffle(alternativasOriginales, random);

        LetraAlternativa respuestaCorrectaFinal = null;
        LetraAlternativa[] letrasFinales = LetraAlternativa.values();

        for (int i = 0; i < alternativasOriginales.size(); i++) {
            AlternativaPregunta alternativa = alternativasOriginales.get(i);

            if (Boolean.TRUE.equals(alternativa.getEsCorrecta())) {
                respuestaCorrectaFinal = letrasFinales[i];
                break;
            }
        }

        if (respuestaCorrectaFinal == null) {
            throw new IllegalStateException(
                    "No se pudo determinar la respuesta correcta de la pregunta "
                            + preguntaOrigen.getCodigo()
            );
        }

        TemaPregunta temaPregunta = TemaPregunta.builder()
                .tema(tema)
                .preguntaOrigen(preguntaOrigen)
                .numeroPregunta(numeroPregunta)
                .codigoPregunta(preguntaOrigen.getCodigo())
                .componente(preguntaOrigen.getComponente())
                .subcurso(preguntaOrigen.getSubcurso())
                .enunciado(preguntaOrigen.getEnunciado())
                .imagenUrl(preguntaOrigen.getImagenUrl())
                .respuestaCorrectaFinal(respuestaCorrectaFinal)
                .build();

        temaPregunta = temaPreguntaRepository.save(temaPregunta);

        List<TemaAlternativa> alternativasFinales = new ArrayList<>();

        for (int i = 0; i < alternativasOriginales.size(); i++) {
            AlternativaPregunta alternativaOriginal = alternativasOriginales.get(i);
            LetraAlternativa letraFinal = letrasFinales[i];

            TemaAlternativa alternativaFinal = TemaAlternativa.builder()
                    .temaPregunta(temaPregunta)
                    .letraOriginal(alternativaOriginal.getLetraOriginal())
                    .letraFinal(letraFinal)
                    .texto(alternativaOriginal.getTexto())
                    .esCorrecta(alternativaOriginal.getEsCorrecta())
                    .build();

            alternativasFinales.add(alternativaFinal);
        }

        temaAlternativaRepository.saveAll(alternativasFinales);
    }

    private void validarAlternativasOriginales(
            PreguntaBanco pregunta,
            List<AlternativaPregunta> alternativas
    ) {
        if (alternativas.size() != 5) {
            throw new IllegalStateException(
                    "La pregunta " + pregunta.getCodigo()
                            + " no tiene exactamente cinco alternativas."
            );
        }

        long correctas = alternativas.stream()
                .filter(alternativa -> Boolean.TRUE.equals(alternativa.getEsCorrecta()))
                .count();

        if (correctas != 1) {
            throw new IllegalStateException(
                    "La pregunta " + pregunta.getCodigo()
                            + " debe tener exactamente una alternativa correcta."
            );
        }
    }

    private List<String> generarLetrasTemas(
            String temaInicial,
            Integer cantidadTemas
    ) {
        if (temaInicial == null || temaInicial.trim().length() != 1) {
            throw new IllegalArgumentException(
                    "El tema inicial debe contener una sola letra."
            );
        }

        if (cantidadTemas == null || cantidadTemas < 1) {
            throw new IllegalArgumentException(
                    "La cantidad de temas debe ser mayor que cero."
            );
        }

        char inicial = Character.toUpperCase(
                temaInicial.trim().charAt(0)
        );

        if (inicial < 'A' || inicial > 'Z') {
            throw new IllegalArgumentException(
                    "El tema inicial debe ser una letra entre A y Z."
            );
        }

        if (inicial + cantidadTemas - 1 > 'Z') {
            throw new IllegalArgumentException(
                    "La cantidad de temas supera las letras disponibles hasta Z."
            );
        }

        List<String> letras = new ArrayList<>();

        for (int i = 0; i < cantidadTemas; i++) {
            letras.add(String.valueOf((char) (inicial + i)));
        }

        return letras;
    }

    @Transactional(readOnly = true)
    public List<TemaExamenResponse> listarTemas(Long examenId) {
        ExamenGenerado examen = obtenerExamen(examenId);

        return temaExamenRepository.findByExamenOrderByLetraTemaAsc(examen)
                .stream()
                .map(this::convertirTemaAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TemaExamenResponse obtenerTema(
            Long examenId,
            String letraTema
    ) {
        ExamenGenerado examen = obtenerExamen(examenId);
        TemaExamen tema = obtenerTemaExamen(examen, letraTema);

        return convertirTemaAResponse(tema);
    }

    @Transactional(readOnly = true)
    public List<PreguntaTemaResponse> listarPreguntasTema(
            Long examenId,
            String letraTema
    ) {
        ExamenGenerado examen = obtenerExamen(examenId);
        TemaExamen tema = obtenerTemaExamen(examen, letraTema);

        return temaPreguntaRepository.findByTemaOrderByNumeroPreguntaAsc(tema)
                .stream()
                .map(this::convertirPreguntaTemaAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClaveTemaResponse obtenerClavesTema(
            Long examenId,
            String letraTema
    ) {
        ExamenGenerado examen = obtenerExamen(examenId);
        TemaExamen tema = obtenerTemaExamen(examen, letraTema);

        List<DetalleClaveTemaResponse> claves =
                temaPreguntaRepository.findByTemaOrderByNumeroPreguntaAsc(tema)
                        .stream()
                        .map(pregunta -> DetalleClaveTemaResponse.builder()
                                .numeroPregunta(pregunta.getNumeroPregunta())
                                .respuestaCorrecta(
                                        pregunta.getRespuestaCorrectaFinal()
                                )
                                .build())
                        .toList();

        return ClaveTemaResponse.builder()
                .examenId(examen.getId())
                .nombreExamen(examen.getNombreExamen())
                .area(examen.getArea().getCodigo().name())
                .letraTema(tema.getLetraTema())
                .totalPreguntas(tema.getTotalPreguntas())
                .claves(claves)
                .build();
    }

    private PreguntaTemaResponse convertirPreguntaTemaAResponse(
            TemaPregunta pregunta
    ) {
        List<AlternativaTemaResponse> alternativas =
                temaAlternativaRepository
                        .findByTemaPreguntaOrderByLetraFinalAsc(pregunta)
                        .stream()
                        .map(alternativa -> AlternativaTemaResponse.builder()
                                .letra(alternativa.getLetraFinal())
                                .texto(alternativa.getTexto())
                                .esCorrecta(alternativa.getEsCorrecta())
                                .build())
                        .toList();

        return PreguntaTemaResponse.builder()
                .numeroPregunta(pregunta.getNumeroPregunta())
                .codigoPregunta(pregunta.getCodigoPregunta())
                .componente(pregunta.getComponente())
                .nombreComponente(pregunta.getComponente().getNombre())
                .subcurso(pregunta.getSubcurso())
                .nombreSubcurso(pregunta.getSubcurso().getNombre())
                .enunciado(pregunta.getEnunciado())
                .imagenUrl(pregunta.getImagenUrl())
                .respuestaCorrectaFinal(
                        pregunta.getRespuestaCorrectaFinal()
                )
                .alternativas(alternativas)
                .build();
    }

    private TemaExamenResponse convertirTemaAResponse(TemaExamen tema) {
        return TemaExamenResponse.builder()
                .id(tema.getId())
                .examenId(tema.getExamen().getId())
                .nombreExamen(tema.getExamen().getNombreExamen())
                .letraTema(tema.getLetraTema())
                .totalPreguntas(tema.getTotalPreguntas())
                .fechaGeneracion(tema.getFechaGeneracion())
                .build();
    }

    private ExamenGenerado obtenerExamen(Long examenId) {
        return examenGeneradoRepository.findById(examenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Examen no encontrado con ID: " + examenId
                ));
    }

    private TemaExamen obtenerTemaExamen(
            ExamenGenerado examen,
            String letraTema
    ) {
        if (letraTema == null || letraTema.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe indicar la letra del tema."
            );
        }

        return temaExamenRepository.findByExamenAndLetraTemaIgnoreCase(
                        examen,
                        letraTema.trim()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el tema " + letraTema
                                + " para el examen indicado."
                ));
    }
}