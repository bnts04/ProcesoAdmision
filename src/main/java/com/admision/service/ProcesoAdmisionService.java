package com.admision.service;

import com.admision.dto.ActualizarCalificacionRequest;
import com.admision.dto.ConfiguracionCalificacionResponse;
import com.admision.dto.CrearProcesoRequest;
import com.admision.entity.ProcesoAdmision;
import com.admision.enums.EstadoProceso;
import com.admision.repository.ProcesoAdmisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesoAdmisionService {

    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    private static final BigDecimal DEFAULT_PUNTAJE_CORRECTA = new BigDecimal("20.0000");
    private static final BigDecimal DEFAULT_PUNTAJE_INCORRECTA = new BigDecimal("-1.2500");
    private static final BigDecimal DEFAULT_PUNTAJE_BLANCA = new BigDecimal("1.2500");
    private static final BigDecimal DEFAULT_FACTOR_ESCALA = new BigDecimal("100.0000");

    @Transactional
    public ProcesoAdmision crearProceso(CrearProcesoRequest request) {
        ProcesoAdmision proceso = ProcesoAdmision.builder()
                .nombreProceso(request.getNombreProceso())
                .modalidad(request.getModalidad())
                .estado(EstadoProceso.PENDIENTE)
                .totalPostulantes(0)
                .totalIngresantes(0)
                .totalNoIngresantes(0)
                .codigoVerificacion(generarCodigoVerificacion(request.getNombreProceso()))
                .puntajeCorrecta(valorODefecto(request.getPuntajeCorrecta(), DEFAULT_PUNTAJE_CORRECTA))
                .puntajeIncorrecta(valorODefecto(request.getPuntajeIncorrecta(), DEFAULT_PUNTAJE_INCORRECTA))
                .puntajeBlanca(valorODefecto(request.getPuntajeBlanca(), DEFAULT_PUNTAJE_BLANCA))
                .factorEscala(valorODefecto(request.getFactorEscala(), DEFAULT_FACTOR_ESCALA))
                .build();

        validarConfiguracionCalificacion(
                proceso.getPuntajeCorrecta(),
                proceso.getPuntajeIncorrecta(),
                proceso.getPuntajeBlanca(),
                proceso.getFactorEscala()
        );

        return procesoAdmisionRepository.save(proceso);
    }

    @Transactional(readOnly = true)
    public List<ProcesoAdmision> listarProcesos() {
        return procesoAdmisionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProcesoAdmision obtenerProcesoPorId(Long id) {
        return procesoAdmisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));
    }

    @Transactional(readOnly = true)
    public ConfiguracionCalificacionResponse obtenerConfiguracionCalificacion(Long procesoId) {
        ProcesoAdmision proceso = obtenerProcesoPorId(procesoId);

        return convertirConfiguracionResponse(
                proceso,
                "Configuración de calificación obtenida correctamente."
        );
    }

    @Transactional
    public ConfiguracionCalificacionResponse actualizarConfiguracionCalificacion(
            Long procesoId,
            ActualizarCalificacionRequest request
    ) {
        ProcesoAdmision proceso = obtenerProcesoPorId(procesoId);

        BigDecimal puntajeCorrecta = valorODefecto(request.getPuntajeCorrecta(), proceso.getPuntajeCorrecta());
        BigDecimal puntajeIncorrecta = valorODefecto(request.getPuntajeIncorrecta(), proceso.getPuntajeIncorrecta());
        BigDecimal puntajeBlanca = valorODefecto(request.getPuntajeBlanca(), proceso.getPuntajeBlanca());
        BigDecimal factorEscala = valorODefecto(request.getFactorEscala(), proceso.getFactorEscala());

        validarConfiguracionCalificacion(
                puntajeCorrecta,
                puntajeIncorrecta,
                puntajeBlanca,
                factorEscala
        );

        proceso.setPuntajeCorrecta(puntajeCorrecta);
        proceso.setPuntajeIncorrecta(puntajeIncorrecta);
        proceso.setPuntajeBlanca(puntajeBlanca);
        proceso.setFactorEscala(factorEscala);

        ProcesoAdmision actualizado = procesoAdmisionRepository.save(proceso);

        return convertirConfiguracionResponse(
                actualizado,
                "Configuración de calificación actualizada correctamente."
        );
    }

    private ConfiguracionCalificacionResponse convertirConfiguracionResponse(
            ProcesoAdmision proceso,
            String mensaje
    ) {
        return ConfiguracionCalificacionResponse.builder()
                .procesoId(proceso.getId())
                .nombreProceso(proceso.getNombreProceso())
                .modalidad(proceso.getModalidad())
                .estadoProceso(proceso.getEstado() != null ? proceso.getEstado().name() : null)
                .puntajeCorrecta(proceso.getPuntajeCorrecta())
                .puntajeIncorrecta(proceso.getPuntajeIncorrecta())
                .puntajeBlanca(proceso.getPuntajeBlanca())
                .factorEscala(proceso.getFactorEscala())
                .mensaje(mensaje)
                .build();
    }

    private void validarConfiguracionCalificacion(
            BigDecimal puntajeCorrecta,
            BigDecimal puntajeIncorrecta,
            BigDecimal puntajeBlanca,
            BigDecimal factorEscala
    ) {
        if (puntajeCorrecta == null) {
            throw new RuntimeException("El puntaje por respuesta correcta es obligatorio.");
        }

        if (puntajeIncorrecta == null) {
            throw new RuntimeException("El puntaje por respuesta incorrecta es obligatorio.");
        }

        if (puntajeBlanca == null) {
            throw new RuntimeException("El puntaje por respuesta blanca o nula es obligatorio.");
        }

        if (factorEscala == null) {
            throw new RuntimeException("El factor de escala es obligatorio.");
        }

        if (puntajeCorrecta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El puntaje por respuesta correcta debe ser mayor a 0.");
        }

        if (factorEscala.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El factor de escala debe ser mayor a 0.");
        }
    }

    private BigDecimal valorODefecto(BigDecimal valor, BigDecimal defecto) {
        return valor != null ? valor : defecto;
    }

    private String generarCodigoVerificacion(String nombreProceso) {
        String limpio = nombreProceso
                .toUpperCase()
                .replace("ADMISIÓN", "ADM")
                .replace("ADMISION", "ADM")
                .replaceAll("[^A-Z0-9]", "-")
                .replaceAll("-+", "-");

        return limpio + "-0001";
    }
}