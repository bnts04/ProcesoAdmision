package com.admision.service.examen;

import com.admision.dto.historial.DetalleExamenGeneradoResponse;
import com.admision.dto.historial.HistorialExamenResponse;
import com.admision.dto.historial.PdfGeneradoHistorialResponse;
import com.admision.dto.pdf.PdfExamenResponse;
import com.admision.dto.tema.TemaExamenResponse;
import com.admision.entity.ExamenGenerado;
import com.admision.entity.PdfExamenGenerado;
import com.admision.entity.TemaExamen;
import com.admision.enums.TipoExamenPdf;
import com.admision.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialExamenService {

        private final ExamenGeneradoRepository examenGeneradoRepository;
        private final TemaExamenRepository temaExamenRepository;
        private final PdfExamenGeneradoRepository pdfExamenGeneradoRepository;

        @Transactional(readOnly = true)
        public List<HistorialExamenResponse> listarHistorial() {
                return examenGeneradoRepository.findAllByOrderByFechaGeneracionDesc()
                                .stream()
                                .map(this::construirHistorialResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public DetalleExamenGeneradoResponse obtenerDetalle(Long examenId) {
                ExamenGenerado examen = obtenerExamen(examenId);
                List<TemaExamen> temas = temaExamenRepository.findByExamenOrderByLetraTemaAsc(examen);
                List<PdfExamenGenerado> pdfs = pdfExamenGeneradoRepository
                                .findByExamenOrderByFechaGeneracionDesc(examen);
                List<TemaExamenResponse> temasResponse = temas.stream()
                                .map(tema -> TemaExamenResponse.builder()
                                                .id(tema.getId())
                                                .examenId(examen.getId())
                                                .nombreExamen(examen.getNombreExamen())
                                                .letraTema(tema.getLetraTema())
                                                .totalPreguntas(tema.getTotalPreguntas())
                                                .fechaGeneracion(tema.getFechaGeneracion())
                                                .build())
                                .toList();
                List<PdfGeneradoHistorialResponse> pdfsResponse = pdfs.stream()
                                .map(this::construirPdfHistorialResponse)
                                .toList();
                return DetalleExamenGeneradoResponse.builder()
                                .examenId(examen.getId())
                                .nombreExamen(examen.getNombreExamen())
                                .area(examen.getArea().getCodigo())
                                .nombreArea(examen.getArea().getNombre())
                                .descripcionArea(examen.getArea().getDescripcion())
                                .cantidadTemas(examen.getCantidadTemas())
                                .temaInicial(examen.getTemaInicial())
                                .totalPreguntas(examen.getTotalPreguntas())
                                .estado(examen.getEstado())
                                .fechaGeneracion(examen.getFechaGeneracion())
                                .temas(temasResponse)
                                .pdfs(pdfsResponse)
                                .build();
        }

        @Transactional(readOnly = true)
        public List<PdfGeneradoHistorialResponse> listarPdfsExamen(Long examenId) {
                ExamenGenerado examen = obtenerExamen(examenId);
                return pdfExamenGeneradoRepository.findByExamenOrderByFechaGeneracionDesc(examen)
                                .stream()
                                .map(this::construirPdfHistorialResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TemaExamenResponse> listarTemasExamen(Long examenId) {
                ExamenGenerado examen = obtenerExamen(examenId);
                return temaExamenRepository.findByExamenOrderByLetraTemaAsc(examen)
                                .stream()
                                .map(tema -> TemaExamenResponse.builder()
                                                .id(tema.getId())
                                                .examenId(examen.getId())
                                                .nombreExamen(examen.getNombreExamen())
                                                .letraTema(tema.getLetraTema())
                                                .totalPreguntas(tema.getTotalPreguntas())
                                                .fechaGeneracion(tema.getFechaGeneracion())
                                                .build())
                                .toList();
        }

        private HistorialExamenResponse construirHistorialResponse(ExamenGenerado examen) {
                List<TemaExamen> temas = temaExamenRepository.findByExamenOrderByLetraTemaAsc(examen);
                List<String> letras = temas.stream().map(TemaExamen::getLetraTema).toList();
                boolean hayPdfExamen = pdfExamenGeneradoRepository
                                .findByExamenAndTipo(examen, TipoExamenPdf.EXAMEN)
                                .stream().findAny().isPresent();
                boolean hayPdfClave = pdfExamenGeneradoRepository
                                .findByExamenAndTipo(examen, TipoExamenPdf.CLAVE)
                                .stream().findAny().isPresent();
                return HistorialExamenResponse.builder()
                                .examenId(examen.getId())
                                .nombreExamen(examen.getNombreExamen())
                                .area(examen.getArea().getCodigo())
                                .nombreArea(examen.getArea().getNombre())
                                .temas(letras)
                                .totalPreguntas(examen.getTotalPreguntas())
                                .fechaGeneracion(examen.getFechaGeneracion())
                                .estado(examen.getEstado())
                                .pdfExamenDisponible(hayPdfExamen)
                                .pdfClaveDisponible(hayPdfClave)
                                .build();
        }

        private PdfGeneradoHistorialResponse construirPdfHistorialResponse(PdfExamenGenerado pdf) {
                return PdfGeneradoHistorialResponse.builder()
                                .id(pdf.getId())
                                .letraTema(pdf.getLetraTema())
                                .tipo(pdf.getTipo())
                                .nombreArchivo(pdf.getNombreArchivo())
                                .urlVer("/api/examenes/pdf/ver/" + pdf.getNombreArchivo())
                                .urlDescargar("/api/examenes/pdf/descargar/" + pdf.getNombreArchivo())
                                .fechaGeneracion(pdf.getFechaGeneracion())
                                .build();
        }

        private ExamenGenerado obtenerExamen(Long examenId) {
                return examenGeneradoRepository.findById(examenId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Examen no encontrado con ID: " + examenId));
        }
}
