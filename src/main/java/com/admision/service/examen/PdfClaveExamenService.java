package com.admision.service.examen;

import com.admision.dto.pdf.PdfExamenResponse;
import com.admision.entity.*;
import com.admision.enums.TipoExamenPdf;
import com.admision.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.awt.Color;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfClaveExamenService {

    private final ExamenGeneradoRepository examenGeneradoRepository;
    private final TemaExamenRepository temaExamenRepository;
    private final TemaPreguntaRepository temaPreguntaRepository;
    private final PdfExamenGeneradoRepository pdfExamenGeneradoRepository;

    @Value("${app.storage.pdfs}")
    private String carpetaPdfs;

    @Transactional
    public List<PdfExamenResponse> generarPdfClaveTodos(Long examenId) {
        ExamenGenerado examen = obtenerExamen(examenId);
        List<TemaExamen> temas = temaExamenRepository.findByExamenOrderByLetraTemaAsc(examen);
        if (temas.isEmpty()) {
            throw new IllegalStateException("El examen no tiene temas generados.");
        }
        return temas.stream()
                .map(tema -> generarPdfClaveUnTema(examen, tema))
                .toList();
    }

    @Transactional
    public PdfExamenResponse generarPdfClaveTema(Long examenId, String letraTema) {
        ExamenGenerado examen = obtenerExamen(examenId);
        TemaExamen tema = obtenerTema(examen, letraTema);
        return generarPdfClaveUnTema(examen, tema);
    }

    private PdfExamenResponse generarPdfClaveUnTema(ExamenGenerado examen, TemaExamen tema) {
        try {
            Files.createDirectories(Paths.get(carpetaPdfs));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nombreArchivo = "Clave_" + examen.getArea().getCodigo().name()
                    + "_Tema_" + tema.getLetraTema() + "_" + timestamp + ".pdf";
            Path rutaArchivo = Paths.get(carpetaPdfs).resolve(nombreArchivo);

            List<TemaPregunta> preguntas =
                    temaPreguntaRepository.findByTemaOrderByNumeroPreguntaAsc(tema);

            escribirPdfClave(rutaArchivo, examen, tema, preguntas);

            pdfExamenGeneradoRepository.findByExamenAndLetraTemaAndTipo(
                    examen, tema.getLetraTema(), TipoExamenPdf.CLAVE
            ).ifPresent(pdfExamenGeneradoRepository::delete);

            PdfExamenGenerado registro = PdfExamenGenerado.builder()
                    .examen(examen)
                    .letraTema(tema.getLetraTema())
                    .tipo(TipoExamenPdf.CLAVE)
                    .nombreArchivo(nombreArchivo)
                    .rutaArchivo(rutaArchivo.toString())
                    .fechaGeneracion(LocalDateTime.now())
                    .build();

            registro = pdfExamenGeneradoRepository.save(registro);

            return PdfExamenResponse.builder()
                    .id(registro.getId())
                    .examenId(examen.getId())
                    .nombreExamen(examen.getNombreExamen())
                    .area(examen.getArea().getCodigo().name())
                    .letraTema(tema.getLetraTema())
                    .tipo(TipoExamenPdf.CLAVE)
                    .nombreArchivo(nombreArchivo)
                    .urlVer("/api/examenes/pdf/ver/" + nombreArchivo)
                    .urlDescargar("/api/examenes/pdf/descargar/" + nombreArchivo)
                    .fechaGeneracion(registro.getFechaGeneracion())
                    .mensaje("PDF de claves generado correctamente.")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de claves: " + e.getMessage(), e);
        }
    }

    private void escribirPdfClave(
            Path rutaArchivo,
            ExamenGenerado examen,
            TemaExamen tema,
            List<TemaPregunta> preguntas
    ) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo.toFile()));
        document.open();

        Font fuenteTitulo = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);
        Font fuenteSubtitulo = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
        Font fuenteNormal = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font fuenteRespuesta = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);

        Paragraph titulo = new Paragraph("CLAVE DE RESPUESTAS", fuenteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("EXAMEN DE ADMISIÓN", fuenteSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitulo);

        document.add(new Paragraph(" "));

        PdfPTable tablaInfo = new PdfPTable(2);
        tablaInfo.setWidthPercentage(80);
        tablaInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaInfo.setSpacingAfter(15f);

        agregarCelda(tablaInfo, "Examen:", examen.getNombreExamen(), fuenteNormal);
        agregarCelda(tablaInfo, "Área:", examen.getArea().getNombre(), fuenteNormal);
        agregarCelda(tablaInfo, "Tema:", "TEMA " + tema.getLetraTema(), fuenteNormal);
        agregarCelda(tablaInfo, "Total preguntas:", String.valueOf(tema.getTotalPreguntas()), fuenteNormal);

        document.add(tablaInfo);

        document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator()));
        document.add(new Paragraph(" "));

        int columnas = 5;
        PdfPTable tablaClaves = new PdfPTable(columnas * 2);
        tablaClaves.setWidthPercentage(100);
        tablaClaves.setSpacingBefore(10f);

        for (int c = 0; c < columnas; c++) {
            PdfPCell hNum = new PdfPCell(new Phrase("N°", new Font(Font.HELVETICA, 9, Font.BOLD)));
            hNum.setPadding(4f);
            hNum.setHorizontalAlignment(Element.ALIGN_CENTER);
            tablaClaves.addCell(hNum);

            PdfPCell hResp = new PdfPCell(new Phrase("Resp.", new Font(Font.HELVETICA, 9, Font.BOLD)));
            hResp.setBackgroundColor(Color.WHITE);
            hResp.setPadding(4f);
            tablaClaves.addCell(hResp);
        }

        int totalPreguntas = preguntas.size();
        int filas = (int) Math.ceil((double) totalPreguntas / columnas);

        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                int idx = fila + col * filas;
                if (idx < totalPreguntas) {
                    TemaPregunta pregunta = preguntas.get(idx);

                    PdfPCell celdaNum = new PdfPCell(
                            new Phrase(String.valueOf(pregunta.getNumeroPregunta()), fuenteNormal)
                    );
                    celdaNum.setPadding(3f);
                    celdaNum.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tablaClaves.addCell(celdaNum);

                    PdfPCell celdaResp = new PdfPCell(
                            new Phrase(pregunta.getRespuestaCorrectaFinal().name(), fuenteRespuesta)
                    );
                    celdaResp.setPadding(3f);
                    celdaResp.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tablaClaves.addCell(celdaResp);
                } else {
                    tablaClaves.addCell(new PdfPCell(new Phrase("")));
                    tablaClaves.addCell(new PdfPCell(new Phrase("")));
                }
            }
        }

        document.add(tablaClaves);
        document.close();
    }

    private void agregarCelda(PdfPTable tabla, String etiqueta, String valor, Font fuente) {
        Font fuenteEtiqueta = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
        PdfPCell cEtiqueta = new PdfPCell(new Phrase(etiqueta, fuenteEtiqueta));
        cEtiqueta.setBorder(Rectangle.NO_BORDER);
        cEtiqueta.setPadding(3f);
        tabla.addCell(cEtiqueta);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, fuente));
        cValor.setBorder(Rectangle.NO_BORDER);
        cValor.setPadding(3f);
        tabla.addCell(cValor);
    }

    private ExamenGenerado obtenerExamen(Long examenId) {
        return examenGeneradoRepository.findById(examenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Examen no encontrado con ID: " + examenId
                ));
    }

    private TemaExamen obtenerTema(ExamenGenerado examen, String letraTema) {
        return temaExamenRepository.findByExamenAndLetraTemaIgnoreCase(examen, letraTema.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el tema " + letraTema + " para el examen indicado."
                ));
    }
}
