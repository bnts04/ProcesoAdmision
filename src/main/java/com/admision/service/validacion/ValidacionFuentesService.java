package com.admision.service.validacion;

import com.admision.dto.DetalleValidacionFuenteResponse;
import com.admision.dto.PostulantePdfGuiaResponse;
import com.admision.dto.ValidacionFuentesResponse;
import com.admision.entity.ArchivoCargado;
import com.admision.enums.EstadoValidacion;
import com.admision.enums.TipoArchivo;
import com.admision.repository.ArchivoCargadoRepository;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.service.pdf.PdfGuiaLecturaService;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ValidacionFuentesService {

    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final ArchivoCargadoRepository archivoCargadoRepository;
    private final PdfGuiaLecturaService pdfGuiaLecturaService;

    public ValidacionFuentesResponse validarFuentes(Long procesoId) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        List<DetalleValidacionFuenteResponse> detalles = new ArrayList<>();

        detalles.add(validarDbf(procesoId, TipoArchivo.CLAVES, columnasClaves()));
        detalles.add(validarDbf(procesoId, TipoArchivo.RESPUEST, columnasRespuest()));
        detalles.add(validarDbf(procesoId, TipoArchivo.IDENTIFI, columnasIdentifi()));
        detalles.add(validarPdfGuia(procesoId));

        int totalValidas = (int) detalles.stream()
                .filter(d -> Boolean.TRUE.equals(d.getValido()))
                .count();

        int totalErrores = detalles.size() - totalValidas;

        return ValidacionFuentesResponse.builder()
                .procesoId(procesoId)
                .valido(totalErrores == 0)
                .totalFuentesRevisadas(detalles.size())
                .totalFuentesValidas(totalValidas)
                .totalFuentesConError(totalErrores)
                .detalles(detalles)
                .mensaje(totalErrores == 0
                        ? "Fuentes validadas correctamente"
                        : "Existen fuentes con observaciones o errores")
                .build();
    }

    @Transactional
    public ValidacionFuentesResponse validarFuentesYActualizarEstado(Long procesoId) {
        ValidacionFuentesResponse response = validarFuentes(procesoId);

        for (DetalleValidacionFuenteResponse detalle : response.getDetalles()) {
            TipoArchivo tipoArchivo = TipoArchivo.valueOf(detalle.getTipoArchivo());

            Optional<ArchivoCargado> archivoOpt;

            if (tipoArchivo == TipoArchivo.PDF_RESULTADOS) {
                archivoOpt = obtenerPdfGuiaDisponibleParaActualizar(procesoId);
            } else {
                archivoOpt = archivoCargadoRepository
                        .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, tipoArchivo);
            }

            if (archivoOpt.isEmpty()) {
                continue;
            }

            ArchivoCargado archivo = archivoOpt.get();

            if (Boolean.TRUE.equals(detalle.getValido())) {
                archivo.setEstadoValidacion(EstadoValidacion.VALIDADO);
                archivo.setObservacion("Archivo validado correctamente");
            } else {
                archivo.setEstadoValidacion(EstadoValidacion.ERROR);
                archivo.setObservacion(String.join(" | ", detalle.getObservaciones()));
            }

            archivoCargadoRepository.save(archivo);
        }

        return response;
    }

    private DetalleValidacionFuenteResponse validarDbf(
            Long procesoId,
            TipoArchivo tipoArchivo,
            List<String> columnasObligatorias
    ) {
        List<String> observaciones = new ArrayList<>();

        Optional<ArchivoCargado> archivoOpt = archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, tipoArchivo);

        if (archivoOpt.isEmpty()) {
            return DetalleValidacionFuenteResponse.builder()
                    .tipoArchivo(tipoArchivo.name())
                    .nombreArchivo(null)
                    .procesoArchivoUsado(procesoId)
                    .valido(false)
                    .pdfGlobalUtilizado(false)
                    .totalColumnas(0)
                    .totalRegistros(0)
                    .observaciones(List.of("No se encontró archivo " + tipoArchivo + " para este proceso"))
                    .build();
        }

        ArchivoCargado archivo = archivoOpt.get();

        if (!archivo.getNombreOriginal().toLowerCase().endsWith(".dbf")) {
            return DetalleValidacionFuenteResponse.builder()
                    .tipoArchivo(tipoArchivo.name())
                    .nombreArchivo(archivo.getNombreOriginal())
                    .procesoArchivoUsado(archivo.getProceso().getId())
                    .valido(false)
                    .pdfGlobalUtilizado(false)
                    .totalColumnas(0)
                    .totalRegistros(0)
                    .observaciones(List.of("El archivo activo no es DBF: " + archivo.getNombreOriginal()))
                    .build();
        }

        try (InputStream inputStream = new FileInputStream(Paths.get(archivo.getRutaArchivo()).toFile())) {
            DBFReader reader = new DBFReader(inputStream);
            reader.setCharactersetName("ISO-8859-1");

            Map<String, Integer> columnas = obtenerColumnasDbf(reader);

            for (String columna : columnasObligatorias) {
                if (!columnas.containsKey(columna)) {
                    observaciones.add("Falta columna obligatoria: " + columna);
                }
            }

            int totalRegistros = contarRegistros(reader);

            if (totalRegistros == 0) {
                observaciones.add("El archivo no tiene registros");
            }

            return DetalleValidacionFuenteResponse.builder()
                    .tipoArchivo(tipoArchivo.name())
                    .nombreArchivo(archivo.getNombreOriginal())
                    .procesoArchivoUsado(archivo.getProceso().getId())
                    .valido(observaciones.isEmpty())
                    .pdfGlobalUtilizado(false)
                    .totalColumnas(columnas.size())
                    .totalRegistros(totalRegistros)
                    .observaciones(observaciones.isEmpty()
                            ? List.of("Archivo DBF validado correctamente")
                            : observaciones)
                    .build();

        } catch (Exception e) {
            return DetalleValidacionFuenteResponse.builder()
                    .tipoArchivo(tipoArchivo.name())
                    .nombreArchivo(archivo.getNombreOriginal())
                    .procesoArchivoUsado(archivo.getProceso().getId())
                    .valido(false)
                    .pdfGlobalUtilizado(false)
                    .totalColumnas(0)
                    .totalRegistros(0)
                    .observaciones(List.of("Error al leer DBF: " + e.getMessage()))
                    .build();
        }
    }

    private DetalleValidacionFuenteResponse validarPdfGuia(Long procesoId) {
        try {
            List<PostulantePdfGuiaResponse> registrosPdf =
                    pdfGuiaLecturaService.obtenerTodosLosRegistros(procesoId);

            boolean valido = !registrosPdf.isEmpty();

            boolean pdfGlobalUtilizado = !existePdfEnProceso(procesoId);

            return DetalleValidacionFuenteResponse.builder()
                    .tipoArchivo(TipoArchivo.PDF_RESULTADOS.name())
                    .nombreArchivo(pdfGlobalUtilizado
                            ? "PDF guía global disponible"
                            : "PDF guía del proceso actual")
                    .procesoArchivoUsado(obtenerProcesoPdfUsado(procesoId))
                    .valido(valido)
                    .pdfGlobalUtilizado(pdfGlobalUtilizado)
                    .totalColumnas(null)
                    .totalRegistros(registrosPdf.size())
                    .observaciones(valido
                            ? List.of(pdfGlobalUtilizado
                            ? "PDF guía global leído correctamente"
                            : "PDF guía del proceso leído correctamente")
                            : List.of("El PDF guía no contiene registros"))
                    .build();

        } catch (Exception e) {
            return DetalleValidacionFuenteResponse.builder()
                    .tipoArchivo(TipoArchivo.PDF_RESULTADOS.name())
                    .nombreArchivo(null)
                    .procesoArchivoUsado(null)
                    .valido(false)
                    .pdfGlobalUtilizado(true)
                    .totalColumnas(null)
                    .totalRegistros(0)
                    .observaciones(List.of("No se pudo leer PDF guía: " + e.getMessage()))
                    .build();
        }
    }

    private Optional<ArchivoCargado> obtenerPdfGuiaDisponibleParaActualizar(Long procesoId) {
        Optional<ArchivoCargado> pdfDelProceso = archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, TipoArchivo.PDF_RESULTADOS);

        if (pdfDelProceso.isPresent()) {
            return pdfDelProceso;
        }

        return archivoCargadoRepository.findTopByTipoArchivoOrderByFechaCargaDesc(TipoArchivo.PDF_RESULTADOS);
    }

    private boolean existePdfEnProceso(Long procesoId) {
        return archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, TipoArchivo.PDF_RESULTADOS)
                .isPresent();
    }

    private Long obtenerProcesoPdfUsado(Long procesoId) {
        Optional<ArchivoCargado> pdfDelProceso = archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, TipoArchivo.PDF_RESULTADOS);

        if (pdfDelProceso.isPresent()) {
            return pdfDelProceso.get().getProceso().getId();
        }

        return archivoCargadoRepository
                .findTopByTipoArchivoOrderByFechaCargaDesc(TipoArchivo.PDF_RESULTADOS)
                .map(a -> a.getProceso().getId())
                .orElse(null);
    }

    private Map<String, Integer> obtenerColumnasDbf(DBFReader reader) {
        Map<String, Integer> columnas = new HashMap<>();

        for (int i = 0; i < reader.getFieldCount(); i++) {
            DBFField field = reader.getField(i);
            String nombreColumna = normalizarEncabezado(field.getName());

            if (!nombreColumna.isBlank()) {
                columnas.put(nombreColumna, i);
            }
        }

        return columnas;
    }

    private int contarRegistros(DBFReader reader) {
        int total = 0;

        try {
            Object[] fila;

            while ((fila = reader.nextRecord()) != null) {
                total++;
            }

            return total;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo contar registros DBF: " + e.getMessage(), e);
        }
    }

    private List<String> columnasClaves() {
        List<String> columnas = new ArrayList<>();

        columnas.add("LITHO");
        columnas.add("TEMA");

        for (int i = 1; i <= 100; i++) {
            columnas.add(String.format("PREG_%03d", i));
        }

        return columnas;
    }

    private List<String> columnasRespuest() {
        List<String> columnas = new ArrayList<>();

        columnas.add("LITHO");
        columnas.add("TEMA");

        for (int i = 1; i <= 100; i++) {
            columnas.add(String.format("PREG_%03d", i));
        }

        return columnas;
    }

    private List<String> columnasIdentifi() {
        return List.of("LITHO", "TEMA", "CODIGO", "SECUENCIA");
    }

    private String normalizarEncabezado(String encabezado) {
        if (encabezado == null) {
            return "";
        }

        String limpio = encabezado.trim();

        if (limpio.contains(",")) {
            limpio = limpio.substring(0, limpio.indexOf(","));
        }

        return limpio.trim().toUpperCase();
    }
}