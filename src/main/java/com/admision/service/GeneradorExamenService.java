package com.admision.service;

import com.admision.dto.examen.*;
import com.admision.entity.*;
import com.admision.enums.*;
import com.admision.exception.BancoPreguntasInsuficienteException;
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
public class GeneradorExamenService {

    private final AreaExamenRepository areaExamenRepository;
    private final ConfiguracionAreaExamenRepository configuracionAreaExamenRepository;
    private final PreguntaBancoRepository preguntaBancoRepository;
    private final ExamenGeneradoRepository examenGeneradoRepository;
    private final ExamenPreguntaBaseRepository examenPreguntaBaseRepository;
    private final TemaExamenRepository temaExamenRepository;
    private final ChocolateoExamenService chocolateoExamenService;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public ExamenGeneradoResponse generarExamen(
            GenerarExamenRequest request
    ) {
        validarRequest(request);

        CodigoAreaExamen codigoArea = parsearCodigoArea(request.getArea());

        AreaExamen area = areaExamenRepository.findByCodigo(codigoArea)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el área de examen: " + request.getArea()
                ));

        if (!Boolean.TRUE.equals(area.getActivo())) {
            throw new IllegalArgumentException(
                    "El área seleccionada se encuentra inactiva."
            );
        }

        List<ConfiguracionAreaExamen> configuraciones =
                configuracionAreaExamenRepository.findByAreaOrderByIdAsc(area);

        if (configuraciones.isEmpty()) {
            throw new IllegalStateException(
                    "El área no tiene una distribución de preguntas configurada."
            );
        }

        int totalRequerido = configuraciones.stream()
                .mapToInt(ConfiguracionAreaExamen::getCantidadPreguntas)
                .sum();

        if (totalRequerido != 100) {
            throw new IllegalStateException(
                    "La configuración del área debe sumar exactamente 100 preguntas."
            );
        }

        List<FaltanteSubcursoResponse> faltantes = new ArrayList<>();
        List<PreguntaBanco> preguntasSeleccionadas = new ArrayList<>();

        for (ConfiguracionAreaExamen configuracion : configuraciones) {
            List<PreguntaBanco> disponibles =
                    preguntaBancoRepository
                            .findByComponenteAndSubcursoAndEstadoOrderByFechaRegistroDesc(
                                    configuracion.getComponente(),
                                    configuracion.getSubcurso(),
                                    EstadoPregunta.ACTIVA
                            );

            int cantidadRequerida = configuracion.getCantidadPreguntas();

            if (disponibles.size() < cantidadRequerida) {
                long faltante = cantidadRequerida - disponibles.size();

                faltantes.add(FaltanteSubcursoResponse.builder()
                        .componente(configuracion.getComponente())
                        .nombreComponente(
                                configuracion.getComponente().getNombre()
                        )
                        .subcurso(configuracion.getSubcurso())
                        .nombreSubcurso(
                                configuracion.getSubcurso().getNombre()
                        )
                        .requeridas(cantidadRequerida)
                        .disponibles((long) disponibles.size())
                        .faltantes(faltante)
                        .build());

                continue;
            }

            List<PreguntaBanco> candidatas =
                    new ArrayList<>(disponibles);

            Collections.shuffle(candidatas, random);

            preguntasSeleccionadas.addAll(
                    candidatas.subList(0, cantidadRequerida)
            );
        }

        if (!faltantes.isEmpty()) {
            throw new BancoPreguntasInsuficienteException(
                    codigoArea,
                    faltantes
            );
        }

        if (preguntasSeleccionadas.size() != totalRequerido) {
            throw new IllegalStateException(
                    "No se pudo completar la selección de las 100 preguntas."
            );
        }

        Collections.shuffle(preguntasSeleccionadas, random);

        String temaInicial = request.getTemaInicial()
                .trim()
                .toUpperCase();

        ExamenGenerado examen = ExamenGenerado.builder()
                .nombreExamen(request.getNombreExamen().trim())
                .area(area)
                .cantidadTemas(request.getCantidadTemas())
                .temaInicial(temaInicial)
                .totalPreguntas(totalRequerido)
                .estado(EstadoExamenGenerado.BASE_GENERADA)
                .build();

        examen = examenGeneradoRepository.save(examen);

        List<ExamenPreguntaBase> registrosBase = new ArrayList<>();

        for (int i = 0; i < preguntasSeleccionadas.size(); i++) {
            ExamenPreguntaBase registro = ExamenPreguntaBase.builder()
                    .examen(examen)
                    .pregunta(preguntasSeleccionadas.get(i))
                    .ordenBase(i + 1)
                    .build();

            registrosBase.add(registro);
        }

        examenPreguntaBaseRepository.saveAll(registrosBase);

        chocolateoExamenService.generarTemas(examen);

        return convertirExamenAResponse(examen);
    }

    @Transactional(readOnly = true)
    public ExamenGeneradoResponse obtenerExamen(Long examenId) {
        ExamenGenerado examen = buscarExamen(examenId);
        return convertirExamenAResponse(examen);
    }

    @Transactional(readOnly = true)
    public List<ExamenGeneradoResponse> listarExamenes() {
        return examenGeneradoRepository
                .findAllByOrderByFechaGeneracionDesc()
                .stream()
                .map(this::convertirExamenAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DetalleExamenBaseResponse obtenerExamenBase(Long examenId) {
        ExamenGenerado examen = buscarExamen(examenId);

        List<PreguntaSeleccionadaResponse> preguntas =
                examenPreguntaBaseRepository
                        .findByExamenOrderByOrdenBaseAsc(examen)
                        .stream()
                        .map(registro -> PreguntaSeleccionadaResponse.builder()
                                .ordenBase(registro.getOrdenBase())
                                .preguntaId(
                                        registro.getPregunta().getId()
                                )
                                .codigoPregunta(
                                        registro.getPregunta().getCodigo()
                                )
                                .componente(
                                        registro.getPregunta().getComponente()
                                )
                                .nombreComponente(
                                        registro.getPregunta()
                                                .getComponente()
                                                .getNombre()
                                )
                                .subcurso(
                                        registro.getPregunta().getSubcurso()
                                )
                                .nombreSubcurso(
                                        registro.getPregunta()
                                                .getSubcurso()
                                                .getNombre()
                                )
                                .enunciado(
                                        registro.getPregunta().getEnunciado()
                                )
                                .imagenUrl(
                                        registro.getPregunta().getImagenUrl()
                                )
                                .build())
                        .toList();

        return DetalleExamenBaseResponse.builder()
                .examen(convertirExamenAResponse(examen))
                .preguntasBase(preguntas)
                .build();
    }

    private void validarRequest(GenerarExamenRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos del examen son obligatorios."
            );
        }

        if (request.getNombreExamen() == null
                || request.getNombreExamen().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre del examen es obligatorio."
            );
        }

        if (request.getArea() == null
                || request.getArea().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El área del examen es obligatoria."
            );
        }

        if (request.getCantidadTemas() == null
                || request.getCantidadTemas() < 1) {
            throw new IllegalArgumentException(
                    "La cantidad de temas debe ser mayor que cero."
            );
        }

        if (request.getCantidadTemas() > 26) {
            throw new IllegalArgumentException(
                    "La cantidad de temas no puede superar 26."
            );
        }

        if (request.getTemaInicial() == null
                || request.getTemaInicial().trim().length() != 1) {
            throw new IllegalArgumentException(
                    "El tema inicial debe contener una sola letra."
            );
        }

        char inicial = Character.toUpperCase(
                request.getTemaInicial().trim().charAt(0)
        );

        if (inicial < 'A' || inicial > 'Z') {
            throw new IllegalArgumentException(
                    "El tema inicial debe ser una letra entre A y Z."
            );
        }

        if (inicial + request.getCantidadTemas() - 1 > 'Z') {
            throw new IllegalArgumentException(
                    "No existen suficientes letras desde el tema inicial indicado."
            );
        }
    }

    private CodigoAreaExamen parsearCodigoArea(String valorArea) {
        String valor = valorArea.trim().toUpperCase();

        if (valor.equals("A")) {
            return CodigoAreaExamen.AREA_A;
        }

        if (valor.equals("B")) {
            return CodigoAreaExamen.AREA_B;
        }

        if (valor.equals("C")) {
            return CodigoAreaExamen.AREA_C;
        }

        try {
            return CodigoAreaExamen.valueOf(valor);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Área no válida. Use AREA_A, AREA_B o AREA_C."
            );
        }
    }

    private ExamenGenerado buscarExamen(Long examenId) {
        return examenGeneradoRepository.findById(examenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Examen no encontrado con ID: " + examenId
                ));
    }

    private ExamenGeneradoResponse convertirExamenAResponse(
            ExamenGenerado examen
    ) {
        List<String> temas =
                temaExamenRepository.findByExamenOrderByLetraTemaAsc(examen)
                        .stream()
                        .map(TemaExamen::getLetraTema)
                        .toList();

        return ExamenGeneradoResponse.builder()
                .id(examen.getId())
                .nombreExamen(examen.getNombreExamen())
                .codigoArea(examen.getArea().getCodigo())
                .nombreArea(examen.getArea().getNombre())
                .descripcionArea(examen.getArea().getDescripcion())
                .cantidadTemas(examen.getCantidadTemas())
                .temaInicial(examen.getTemaInicial())
                .totalPreguntas(examen.getTotalPreguntas())
                .estado(examen.getEstado())
                .fechaGeneracion(examen.getFechaGeneracion())
                .temasGenerados(temas)
                .build();
    }
}