package com.admision.service;

import com.admision.dto.AnulacionPostulanteResponse;
import com.admision.entity.AnulacionPostulante;
import com.admision.entity.ProcesoAdmision;
import com.admision.entity.ResultadoPostulante;
import com.admision.enums.CondicionPostulante;
import com.admision.repository.AnulacionPostulanteRepository;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnulacionPostulanteService {

    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final AnulacionPostulanteRepository anulacionPostulanteRepository;

    @Value("${app.storage.evidencias}")
    private String carpetaEvidencias;

    @Transactional
    public AnulacionPostulanteResponse anularPostulante(
            Long procesoId,
            String codigo,
            String motivo,
            MultipartFile evidencia
    ) {
        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        if (codigo == null || codigo.isBlank()) {
            throw new RuntimeException("Debe ingresar el código del postulante");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new RuntimeException("Debe ingresar el motivo de anulación");
        }

        if (evidencia == null || evidencia.isEmpty()) {
            throw new RuntimeException("Debe adjuntar una foto o evidencia de anulación");
        }

        ResultadoPostulante resultado = resultadoPostulanteRepository
                .findByProcesoIdAndCodigo(procesoId, normalizarCodigo(codigo))
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró postulante con código " + codigo + " en el proceso " + procesoId
                ));

        if (anulacionPostulanteRepository.existsByResultadoPostulanteId(resultado.getId())) {
            throw new RuntimeException("Este postulante ya tiene una anulación registrada");
        }

        validarArchivoEvidencia(evidencia);

        try {
            Files.createDirectories(Paths.get(carpetaEvidencias));

            String nombreOriginal = evidencia.getOriginalFilename() == null
                    ? "evidencia"
                    : evidencia.getOriginalFilename();

            String extension = obtenerExtension(nombreOriginal);

            String nombreGuardado = "anulacion_postulante_"
                    + procesoId
                    + "_"
                    + resultado.getCodigo()
                    + "_"
                    + UUID.randomUUID()
                    + extension;

            Path rutaArchivo = Paths.get(carpetaEvidencias).resolve(nombreGuardado);
            Files.copy(evidencia.getInputStream(), rutaArchivo);

            CondicionPostulante condicionAnterior = resultado.getCondicion();

            resultado.setCondicion(CondicionPostulante.ANULADO);
            resultado.setObservacion(agregarObservacion(
                    resultado.getObservacion(),
                    "Postulante anulado con evidencia adjunta. Motivo: " + motivo.trim()
            ));

            resultadoPostulanteRepository.save(resultado);

            AnulacionPostulante anulacion = AnulacionPostulante.builder()
                    .proceso(proceso)
                    .resultadoPostulante(resultado)
                    .codigo(resultado.getCodigo())
                    .motivo(motivo.trim())
                    .condicionAnterior(condicionAnterior)
                    .puntajeFinalAnterior(resultado.getPuntajeFinal())
                    .nombreArchivo(nombreGuardado)
                    .nombreOriginal(nombreOriginal)
                    .rutaArchivo(rutaArchivo.toString())
                    .contentType(evidencia.getContentType())
                    .tamanoBytes(evidencia.getSize())
                    .build();

            AnulacionPostulante guardada = anulacionPostulanteRepository.save(anulacion);

            return AnulacionPostulanteResponse.fromEntity(
                    guardada,
                    "Postulante anulado correctamente"
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al anular postulante: " + e.getMessage(), e);
        }
    }

    public List<AnulacionPostulanteResponse> listarAnulacionesPorProceso(Long procesoId) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        return anulacionPostulanteRepository.findByProcesoIdOrderByFechaAnulacionDesc(procesoId)
                .stream()
                .map(a -> AnulacionPostulanteResponse.fromEntity(
                        a,
                        "Anulación registrada"
                ))
                .toList();
    }

    public AnulacionPostulanteResponse obtenerAnulacionPorCodigo(Long procesoId, String codigo) {
        AnulacionPostulante anulacion = anulacionPostulanteRepository
                .findByProcesoIdAndCodigo(procesoId, normalizarCodigo(codigo))
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró anulación para el código " + codigo
                ));

        return AnulacionPostulanteResponse.fromEntity(
                anulacion,
                "Anulación encontrada correctamente"
        );
    }

    private void validarArchivoEvidencia(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename() == null
                ? ""
                : archivo.getOriginalFilename().toLowerCase();

        boolean extensionValida =
                nombre.endsWith(".jpg")
                        || nombre.endsWith(".jpeg")
                        || nombre.endsWith(".png")
                        || nombre.endsWith(".pdf");

        if (!extensionValida) {
            throw new RuntimeException("Solo se permiten evidencias JPG, JPEG, PNG o PDF");
        }

        long maximoBytes = 10 * 1024 * 1024;

        if (archivo.getSize() > maximoBytes) {
            throw new RuntimeException("La evidencia no debe superar los 10 MB");
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int index = nombreArchivo.lastIndexOf(".");

        if (index == -1) {
            return "";
        }

        return nombreArchivo.substring(index).toLowerCase();
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