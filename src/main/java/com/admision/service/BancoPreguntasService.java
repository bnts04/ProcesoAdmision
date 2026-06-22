package com.admision.service;

import com.admision.dto.banco.AlternativaPreguntaResponse;
import com.admision.dto.banco.ActualizarPreguntaRequest;
import com.admision.dto.banco.CrearPreguntaRequest;
import com.admision.dto.banco.PreguntaBancoResponse;
import com.admision.dto.banco.ResumenBancoPreguntasResponse;
import com.admision.dto.banco.ResumenComponenteBancoResponse;
import com.admision.dto.banco.ResumenSubcursoBancoResponse;
import com.admision.entity.AlternativaPregunta;
import com.admision.entity.PreguntaBanco;
import com.admision.enums.ComponentePregunta;
import com.admision.enums.EstadoPregunta;
import com.admision.enums.LetraAlternativa;
import com.admision.enums.SubcursoPregunta;
import com.admision.repository.PreguntaBancoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BancoPreguntasService {

    private final PreguntaBancoRepository preguntaBancoRepository;
    private final ArchivoPreguntaService archivoPreguntaService;

    @Transactional
    public PreguntaBancoResponse registrarPregunta(CrearPreguntaRequest request) {
        return registrarPregunta(request, null);
    }

    @Transactional
    public PreguntaBancoResponse registrarPregunta(CrearPreguntaRequest request, MultipartFile imagen) {
        validarPregunta(
                request.getComponente(),
                request.getSubcurso(),
                request.getEnunciado(),
                request.getAlternativaA(),
                request.getAlternativaB(),
                request.getAlternativaC(),
                request.getAlternativaD(),
                request.getAlternativaE(),
                request.getRespuestaCorrecta()
        );

        PreguntaBanco pregunta = PreguntaBanco.builder()
                .componente(request.getComponente())
                .subcurso(request.getSubcurso())
                .enunciado(request.getEnunciado().trim())
                .observacion(limpiarTextoOpcional(request.getObservacion()))
                .estado(request.getEstado() != null ? request.getEstado() : EstadoPregunta.ACTIVA)
                .build();

        pregunta.setAlternativas(crearAlternativas(
                pregunta,
                request.getAlternativaA(),
                request.getAlternativaB(),
                request.getAlternativaC(),
                request.getAlternativaD(),
                request.getAlternativaE(),
                request.getRespuestaCorrecta()
        ));

        PreguntaBanco guardada = preguntaBancoRepository.save(pregunta);

        guardada.setCodigo(generarCodigo(guardada.getId()));

        if (imagen != null && !imagen.isEmpty()) {
            String imagenUrl = archivoPreguntaService.guardarImagenPregunta(guardada.getCodigo(), imagen);
            guardada.setImagenUrl(imagenUrl);
        }

        PreguntaBanco actualizada = preguntaBancoRepository.save(guardada);

        return convertirAResponse(actualizada);
    }

    @Transactional(readOnly = true)
    public List<PreguntaBancoResponse> listarPreguntas(ComponentePregunta componente, SubcursoPregunta subcurso) {
        List<PreguntaBanco> preguntas;

        if (subcurso != null && componente == null) {
            componente = subcurso.getComponente();
        }

        if (componente != null && subcurso != null) {
            preguntas = preguntaBancoRepository.findByComponenteAndSubcursoAndEstadoOrderByFechaRegistroDesc(
                    componente,
                    subcurso,
                    EstadoPregunta.ACTIVA
            );
        } else if (componente != null) {
            preguntas = preguntaBancoRepository.findByComponenteAndEstadoOrderByFechaRegistroDesc(
                    componente,
                    EstadoPregunta.ACTIVA
            );
        } else {
            preguntas = preguntaBancoRepository.findByEstadoOrderByFechaRegistroDesc(EstadoPregunta.ACTIVA);
        }

        return preguntas.stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PreguntaBancoResponse obtenerPorId(Long id) {
        PreguntaBanco pregunta = preguntaBancoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pregunta no encontrada con ID: " + id));

        return convertirAResponse(pregunta);
    }

    @Transactional
    public PreguntaBancoResponse actualizarPregunta(Long id, ActualizarPreguntaRequest request) {
        return actualizarPregunta(id, request, null);
    }

    @Transactional
    public PreguntaBancoResponse actualizarPregunta(Long id, ActualizarPreguntaRequest request, MultipartFile imagen) {
        validarPregunta(
                request.getComponente(),
                request.getSubcurso(),
                request.getEnunciado(),
                request.getAlternativaA(),
                request.getAlternativaB(),
                request.getAlternativaC(),
                request.getAlternativaD(),
                request.getAlternativaE(),
                request.getRespuestaCorrecta()
        );

        PreguntaBanco pregunta = preguntaBancoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pregunta no encontrada con ID: " + id));

        pregunta.setComponente(request.getComponente());
        pregunta.setSubcurso(request.getSubcurso());
        pregunta.setEnunciado(request.getEnunciado().trim());
        pregunta.setObservacion(limpiarTextoOpcional(request.getObservacion()));
        pregunta.setEstado(request.getEstado() != null ? request.getEstado() : EstadoPregunta.ACTIVA);

        if (imagen != null && !imagen.isEmpty()) {
            archivoPreguntaService.eliminarImagenPregunta(pregunta.getImagenUrl());

            String imagenUrl = archivoPreguntaService.guardarImagenPregunta(pregunta.getCodigo(), imagen);
            pregunta.setImagenUrl(imagenUrl);
        }

        pregunta.getAlternativas().clear();
        pregunta.getAlternativas().addAll(crearAlternativas(
                pregunta,
                request.getAlternativaA(),
                request.getAlternativaB(),
                request.getAlternativaC(),
                request.getAlternativaD(),
                request.getAlternativaE(),
                request.getRespuestaCorrecta()
        ));

        PreguntaBanco actualizada = preguntaBancoRepository.save(pregunta);

        return convertirAResponse(actualizada);
    }

    @Transactional
    public void desactivarPregunta(Long id) {
        PreguntaBanco pregunta = preguntaBancoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pregunta no encontrada con ID: " + id));

        pregunta.setEstado(EstadoPregunta.INACTIVA);
        preguntaBancoRepository.save(pregunta);
    }

    @Transactional
    public PreguntaBancoResponse eliminarImagenPregunta(Long id) {
        PreguntaBanco pregunta = preguntaBancoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pregunta no encontrada con ID: " + id));

        archivoPreguntaService.eliminarImagenPregunta(pregunta.getImagenUrl());
        pregunta.setImagenUrl(null);

        PreguntaBanco actualizada = preguntaBancoRepository.save(pregunta);

        return convertirAResponse(actualizada);
    }

    @Transactional(readOnly = true)
    public ResumenBancoPreguntasResponse obtenerResumenBanco() {
        Long totalActivas = preguntaBancoRepository.countByEstado(EstadoPregunta.ACTIVA);

        List<ResumenComponenteBancoResponse> componentes = Arrays.stream(ComponentePregunta.values())
                .map(componente -> {
                    Long totalComponente = preguntaBancoRepository.countByComponenteAndEstado(
                            componente,
                            EstadoPregunta.ACTIVA
                    );

                    List<ResumenSubcursoBancoResponse> subcursos = Arrays.stream(SubcursoPregunta.values())
                            .filter(subcurso -> subcurso.getComponente() == componente)
                            .map(subcurso -> ResumenSubcursoBancoResponse.builder()
                                    .subcurso(subcurso)
                                    .nombreSubcurso(subcurso.getNombre())
                                    .totalPreguntas(preguntaBancoRepository.countByComponenteAndSubcursoAndEstado(
                                            componente,
                                            subcurso,
                                            EstadoPregunta.ACTIVA
                                    ))
                                    .build())
                            .toList();

                    return ResumenComponenteBancoResponse.builder()
                            .componente(componente)
                            .nombreComponente(componente.getNombre())
                            .totalPreguntas(totalComponente)
                            .subcursos(subcursos)
                            .build();
                })
                .toList();

        return ResumenBancoPreguntasResponse.builder()
                .totalPreguntasActivas(totalActivas)
                .componentes(componentes)
                .build();
    }

    private void validarPregunta(
            ComponentePregunta componente,
            SubcursoPregunta subcurso,
            String enunciado,
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD,
            String alternativaE,
            LetraAlternativa respuestaCorrecta
    ) {
        if (componente == null) {
            throw new IllegalArgumentException("El componente es obligatorio.");
        }

        if (subcurso == null) {
            throw new IllegalArgumentException("El subcurso es obligatorio.");
        }

        if (subcurso.getComponente() != componente) {
            throw new IllegalArgumentException("El subcurso no pertenece al componente seleccionado.");
        }

        if (esVacio(enunciado)) {
            throw new IllegalArgumentException("El enunciado es obligatorio.");
        }

        if (esVacio(alternativaA)
                || esVacio(alternativaB)
                || esVacio(alternativaC)
                || esVacio(alternativaD)
                || esVacio(alternativaE)) {
            throw new IllegalArgumentException("Debe registrar las cinco alternativas: A, B, C, D y E.");
        }

        if (respuestaCorrecta == null) {
            throw new IllegalArgumentException("Debe seleccionar la respuesta correcta.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String limpiarTextoOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        return valor.trim();
    }

    private List<AlternativaPregunta> crearAlternativas(
            PreguntaBanco pregunta,
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD,
            String alternativaE,
            LetraAlternativa respuestaCorrecta
    ) {
        List<AlternativaPregunta> alternativas = new ArrayList<>();

        alternativas.add(crearAlternativa(pregunta, LetraAlternativa.A, alternativaA, respuestaCorrecta));
        alternativas.add(crearAlternativa(pregunta, LetraAlternativa.B, alternativaB, respuestaCorrecta));
        alternativas.add(crearAlternativa(pregunta, LetraAlternativa.C, alternativaC, respuestaCorrecta));
        alternativas.add(crearAlternativa(pregunta, LetraAlternativa.D, alternativaD, respuestaCorrecta));
        alternativas.add(crearAlternativa(pregunta, LetraAlternativa.E, alternativaE, respuestaCorrecta));

        return alternativas;
    }

    private AlternativaPregunta crearAlternativa(
            PreguntaBanco pregunta,
            LetraAlternativa letra,
            String texto,
            LetraAlternativa respuestaCorrecta
    ) {
        return AlternativaPregunta.builder()
                .pregunta(pregunta)
                .letraOriginal(letra)
                .texto(texto.trim())
                .esCorrecta(letra == respuestaCorrecta)
                .build();
    }

    private PreguntaBancoResponse convertirAResponse(PreguntaBanco pregunta) {
        List<AlternativaPreguntaResponse> alternativas = pregunta.getAlternativas().stream()
                .map(alternativa -> AlternativaPreguntaResponse.builder()
                        .id(alternativa.getId())
                        .letra(alternativa.getLetraOriginal())
                        .texto(alternativa.getTexto())
                        .esCorrecta(alternativa.getEsCorrecta())
                        .build())
                .toList();

        LetraAlternativa respuestaCorrecta = alternativas.stream()
                .filter(AlternativaPreguntaResponse::getEsCorrecta)
                .map(AlternativaPreguntaResponse::getLetra)
                .findFirst()
                .orElse(null);

        return PreguntaBancoResponse.builder()
                .id(pregunta.getId())
                .codigo(pregunta.getCodigo())
                .componente(pregunta.getComponente())
                .nombreComponente(pregunta.getComponente().getNombre())
                .subcurso(pregunta.getSubcurso())
                .nombreSubcurso(pregunta.getSubcurso().getNombre())
                .enunciado(pregunta.getEnunciado())
                .imagenUrl(pregunta.getImagenUrl())
                .observacion(pregunta.getObservacion())
                .respuestaCorrecta(respuestaCorrecta)
                .estado(pregunta.getEstado())
                .fechaRegistro(pregunta.getFechaRegistro())
                .fechaActualizacion(pregunta.getFechaActualizacion())
                .alternativas(alternativas)
                .build();
    }

    private String generarCodigo(Long id) {
        return String.format("P%06d", id);
    }
}