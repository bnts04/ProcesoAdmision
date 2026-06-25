package com.admision.service;

import com.admision.dto.area.AreaExamenResponse;
import com.admision.dto.area.ConfiguracionAreaResponse;
import com.admision.dto.area.DistribucionAreaExamenResponse;
import com.admision.dto.area.ResumenComponenteAreaResponse;
import com.admision.entity.AreaExamen;
import com.admision.entity.ConfiguracionAreaExamen;
import com.admision.enums.CodigoAreaExamen;
import com.admision.enums.ComponentePregunta;
import com.admision.enums.EstadoPregunta;
import com.admision.repository.AreaExamenRepository;
import com.admision.repository.ConfiguracionAreaExamenRepository;
import com.admision.repository.PreguntaBancoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfiguracionAreaExamenService {

    private final AreaExamenRepository areaExamenRepository;
    private final ConfiguracionAreaExamenRepository configuracionAreaExamenRepository;
    private final PreguntaBancoRepository preguntaBancoRepository;

    @Transactional(readOnly = true)
    public List<AreaExamenResponse> listarAreas() {
        return areaExamenRepository.findByActivoTrueOrderByIdAsc()
                .stream()
                .map(this::convertirAreaAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DistribucionAreaExamenResponse obtenerConfiguracionArea(String codigoAreaTexto) {
        CodigoAreaExamen codigoArea = parsearCodigoArea(codigoAreaTexto);

        AreaExamen area = areaExamenRepository.findByCodigo(codigoArea)
                .orElseThrow(() -> new IllegalArgumentException("Área de examen no encontrada: " + codigoAreaTexto));

        List<ConfiguracionAreaExamen> configuraciones =
                configuracionAreaExamenRepository.findByAreaOrderByIdAsc(area);

        List<ConfiguracionAreaResponse> detalle = configuraciones.stream()
                .map(config -> {
                    Long disponibles = preguntaBancoRepository.countByComponenteAndSubcursoAndEstado(
                            config.getComponente(),
                            config.getSubcurso(),
                            EstadoPregunta.ACTIVA
                    );

                    return ConfiguracionAreaResponse.builder()
                            .componente(config.getComponente())
                            .nombreComponente(config.getComponente().getNombre())
                            .subcurso(config.getSubcurso())
                            .nombreSubcurso(config.getSubcurso().getNombre())
                            .cantidadRequerida(config.getCantidadPreguntas())
                            .cantidadDisponible(disponibles)
                            .suficiente(disponibles >= config.getCantidadPreguntas())
                            .build();
                })
                .toList();

        List<ResumenComponenteAreaResponse> resumenComponentes = Arrays.stream(ComponentePregunta.values())
                .map(componente -> {
                    Integer requerido = detalle.stream()
                            .filter(item -> item.getComponente() == componente)
                            .mapToInt(ConfiguracionAreaResponse::getCantidadRequerida)
                            .sum();

                    Long disponible = detalle.stream()
                            .filter(item -> item.getComponente() == componente)
                            .mapToLong(ConfiguracionAreaResponse::getCantidadDisponible)
                            .sum();

                    return ResumenComponenteAreaResponse.builder()
                            .componente(componente)
                            .nombreComponente(componente.getNombre())
                            .cantidadRequerida(requerido)
                            .cantidadDisponible(disponible)
                            .suficiente(disponible >= requerido)
                            .build();
                })
                .filter(item -> item.getCantidadRequerida() > 0)
                .toList();

        Integer totalRequerido = detalle.stream()
                .mapToInt(ConfiguracionAreaResponse::getCantidadRequerida)
                .sum();

        Long totalDisponible = detalle.stream()
                .mapToLong(ConfiguracionAreaResponse::getCantidadDisponible)
                .sum();

        Boolean bancoSuficiente = detalle.stream()
                .allMatch(ConfiguracionAreaResponse::getSuficiente);

        return DistribucionAreaExamenResponse.builder()
                .area(convertirAreaAResponse(area))
                .totalPreguntasRequeridas(totalRequerido)
                .totalPreguntasDisponibles(totalDisponible)
                .bancoSuficiente(bancoSuficiente)
                .resumenComponentes(resumenComponentes)
                .detalleSubcursos(detalle)
                .build();
    }

    private AreaExamenResponse convertirAreaAResponse(AreaExamen area) {
        return AreaExamenResponse.builder()
                .id(area.getId())
                .codigo(area.getCodigo())
                .nombre(area.getNombre())
                .descripcion(area.getDescripcion())
                .activo(area.getActivo())
                .build();
    }

    private CodigoAreaExamen parsearCodigoArea(String codigoAreaTexto) {
        if (codigoAreaTexto == null || codigoAreaTexto.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar el área del examen.");
        }

        String valor = codigoAreaTexto.trim().toUpperCase();

        if (valor.equals("A")) {
            return CodigoAreaExamen.AREA_A;
        }

        if (valor.equals("B")) {
            return CodigoAreaExamen.AREA_B;
        }

        if (valor.equals("C")) {
            return CodigoAreaExamen.AREA_C;
        }

        return CodigoAreaExamen.valueOf(valor);
    }
}