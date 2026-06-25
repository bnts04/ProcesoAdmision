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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfExamenService {

    private final ExamenGeneradoRepository examenGeneradoRepository;
    private final TemaExamenRepository temaExamenRepository;
    private final TemaPreguntaRepository temaPreguntaRepository;
    private final TemaAlternativaRepository temaAlternativaRepository;
    private final PdfExamenGeneradoRepository pdfExamenGeneradoRepository;

    @Value("${app.storage.pdfs}")
    private String carpetaPdfs;

    @Value("${app.storage.imagenes}")
    private String carpetaImagenes;

    private static final Color NEGRO = Color.BLACK;

    @Transactional
    public List<PdfExamenResponse> generarPdfExamenTodos(Long examenId) {
        ExamenGenerado examen = obtenerExamen(examenId);
        List<TemaExamen> temas = temaExamenRepository.findByExamenOrderByLetraTemaAsc(examen);
        if (temas.isEmpty())
            throw new IllegalStateException("El examen no tiene temas generados.");
        return temas.stream().map(t -> generarPdfExamenUnTema(examen, t)).toList();
    }

    @Transactional
    public PdfExamenResponse generarPdfExamenTema(Long examenId, String letraTema) {
        ExamenGenerado examen = obtenerExamen(examenId);
        TemaExamen tema = obtenerTema(examen, letraTema);
        return generarPdfExamenUnTema(examen, tema);
    }

    private PdfExamenResponse generarPdfExamenUnTema(ExamenGenerado examen, TemaExamen tema) {
        try {
            Files.createDirectories(Paths.get(carpetaPdfs));

            String nombreArchivo = construirNombreArchivo(examen, tema.getLetraTema());
            Path rutaArchivo = Paths.get(carpetaPdfs).resolve(nombreArchivo);

            List<TemaPregunta> preguntas = temaPreguntaRepository.findByTemaOrderByNumeroPreguntaAsc(tema);

            escribirPdf(rutaArchivo, examen, tema, preguntas);

            pdfExamenGeneradoRepository
                    .findByExamenAndLetraTemaAndTipo(examen, tema.getLetraTema(), TipoExamenPdf.EXAMEN)
                    .ifPresent(pdfExamenGeneradoRepository::delete);

            PdfExamenGenerado reg = pdfExamenGeneradoRepository.save(
                    PdfExamenGenerado.builder()
                            .examen(examen)
                            .letraTema(tema.getLetraTema())
                            .tipo(TipoExamenPdf.EXAMEN)
                            .nombreArchivo(nombreArchivo)
                            .rutaArchivo(rutaArchivo.toString())
                            .fechaGeneracion(LocalDateTime.now())
                            .build());

            return construirResponse(reg, examen);

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF del examen: " + e.getMessage(), e);
        }
    }

    private void escribirPdf(
            Path rutaArchivo,
            ExamenGenerado examen,
            TemaExamen tema,
            List<TemaPregunta> preguntas) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 70, 40);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(rutaArchivo.toFile()));

        writer.setPageEvent(new EncabezadoPieEvento(examen, tema));

        doc.open();

        PdfPTable tablaCuerpo = new PdfPTable(3);
        tablaCuerpo.setWidthPercentage(100);
        tablaCuerpo.setWidths(new float[] { 50f, 1f, 50f });
        tablaCuerpo.setSpacingBefore(2f);

        PdfPCell celdaIzq = new PdfPCell();
        celdaIzq.setBorder(Rectangle.NO_BORDER);
        celdaIzq.setPadding(0);
        celdaIzq.addElement(construirColumnaPreguntas(preguntas, 0));
        PdfPCell celdaSep = new PdfPCell();
        celdaSep.setBorder(Rectangle.NO_BORDER);
        PdfPCell celdaDer = new PdfPCell();
        celdaDer.setBorder(Rectangle.NO_BORDER);
        celdaDer.setPadding(0);
        celdaDer.addElement(construirColumnaPreguntas(preguntas, 1));

        tablaCuerpo.addCell(celdaIzq);
        tablaCuerpo.addCell(celdaSep);
        tablaCuerpo.addCell(celdaDer);

        doc.add(tablaCuerpo);
        doc.close();
    }

    private PdfPTable construirColumnaPreguntas(List<TemaPregunta> todas, int columna) {

        int total = todas.size();

        int alturaActual = 0;
        int puntoCorte = total / 2;

        for (int i = 0; i < todas.size(); i++) {

            TemaPregunta p = todas.get(i);

            int alturaPregunta = 5;

            alturaPregunta += p.getEnunciado().length() / 40;

            List<TemaAlternativa> alts = temaAlternativaRepository.findByTemaPreguntaOrderByLetraFinalAsc(p);

            boolean alternativasLargas = alts.stream()
                    .anyMatch(a -> a.getTexto().length() > 20);

            if (alternativasLargas) {
                alturaPregunta += 5;
            }

            alturaActual += alturaPregunta;

            if (alturaActual > 300) {
                puntoCorte = i;
                break;
            }
        }

        int inicio = (columna == 0) ? 0 : puntoCorte;
        int fin = (columna == 0) ? puntoCorte : total;

        List<TemaPregunta> subLista = todas.subList(inicio, fin);

        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);

        Font fEnunciado = new Font(Font.HELVETICA, 9.5f, Font.NORMAL, NEGRO);
        Font fNegrita = new Font(Font.HELVETICA, 9.5f, Font.BOLD, NEGRO);
        Font fAlternativa = new Font(Font.HELVETICA, 8.8f, Font.NORMAL, NEGRO);

        for (TemaPregunta pregunta : subLista) {
            PdfPCell celdaPregunta = new PdfPCell();
            celdaPregunta.setBorder(Rectangle.NO_BORDER);
            celdaPregunta.setPaddingBottom(10f);
            celdaPregunta.setPaddingLeft(3f);
            celdaPregunta.setPaddingRight(3f);

            Paragraph enunciado = new Paragraph();
            enunciado.setLeading(13f);
            enunciado.setSpacingBefore(6f);

            enunciado.add(new Chunk(
                    pregunta.getNumeroPregunta() + ") ",
                    fNegrita));
            String texto = pregunta.getEnunciado();

            String[] lineas = texto.split("\n");

            for (int i = 0; i < lineas.length; i++) {
                enunciado.add(new Chunk(lineas[i], fEnunciado));

                if (i < lineas.length - 1) {
                    enunciado.add(Chunk.NEWLINE);
                }
            }

            celdaPregunta.addElement(enunciado);
            String imagenUrl = pregunta.getImagenUrl();
            if (imagenUrl != null && !imagenUrl.isBlank()) {
                try {
                    String nombreImagen = imagenUrl.contains("/")
                            ? imagenUrl.substring(imagenUrl.lastIndexOf("/") + 1)
                            : imagenUrl;

                    Path rutaImagen = Paths.get(carpetaImagenes).resolve(nombreImagen).normalize();

                    if (java.nio.file.Files.exists(rutaImagen)) {
                        com.lowagie.text.Image img = com.lowagie.text.Image
                                .getInstance(rutaImagen.toAbsolutePath().toString());

                        float maxAncho = 220f;
                        if (img.getWidth() > maxAncho) {
                            img.scaleToFit(100f, 100f);
                        }

                        img.setSpacingBefore(3f);
                        img.setSpacingAfter(3f);
                        celdaPregunta.addElement(img);
                    }
                } catch (Exception ignored) {
                }
            }

            List<TemaAlternativa> alts = temaAlternativaRepository.findByTemaPreguntaOrderByLetraFinalAsc(pregunta);

            boolean alternativasLargas = alts.stream()
                    .anyMatch(a -> a.getTexto().length() > 20);
            if (alternativasLargas) {

                for (TemaAlternativa alt : alts) {

                    Paragraph pAlt = new Paragraph();
                    pAlt.setLeading(11f);

                    pAlt.add(new Chunk(
                            alt.getLetraFinal().name() + ") ",
                            fNegrita));

                    pAlt.add(new Chunk(
                            alt.getTexto(),
                            fAlternativa));

                    celdaPregunta.addElement(pAlt);
                }

            } else {

                PdfPTable tablaAlts = new PdfPTable(5);
                tablaAlts.setWidthPercentage(100);
                tablaAlts.setSpacingBefore(4f);

                for (TemaAlternativa alt : alts) {

                    PdfPCell celdaAlt = new PdfPCell();
                    celdaAlt.setBorder(Rectangle.NO_BORDER);
                    celdaAlt.setPadding(1f);

                    Paragraph pAlt = new Paragraph();
                    pAlt.setLeading(11f);

                    pAlt.add(new Chunk(
                            alt.getLetraFinal().name() + ") ",
                            fNegrita));

                    pAlt.add(new Chunk(
                            alt.getTexto(),
                            fAlternativa));

                    celdaAlt.addElement(pAlt);

                    tablaAlts.addCell(celdaAlt);
                }

                celdaPregunta.addElement(tablaAlts);
            }
            tabla.addCell(celdaPregunta);
        }
        return tabla;
    }

    private static class EncabezadoPieEvento extends PdfPageEventHelper {

        private final ExamenGenerado examen;
        private final TemaExamen tema;

        EncabezadoPieEvento(ExamenGenerado examen, TemaExamen tema) {
            this.examen = examen;
            this.tema = tema;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {

            PdfContentByte cb = writer.getDirectContent();

            float izq = doc.left();
            float der = doc.right();
            float y = doc.top() + 25f;
            Font fEncabezado = new Font(
                    Font.HELVETICA,
                    14f,
                    Font.BOLD,
                    NEGRO);

            String textoIzq = examen.getNombreExamen().toUpperCase();

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_LEFT,
                    new Phrase(textoIzq, fEncabezado),
                    izq,
                    y,
                    0);

            String textoDer = "TEMA " + tema.getLetraTema().toUpperCase();

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_RIGHT,
                    new Phrase(textoDer, fEncabezado),
                    der,
                    y,
                    0);

            Font fPie = new Font(Font.HELVETICA, 9f, Font.BOLD, NEGRO);

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_RIGHT,
                    new Phrase("Página: " + writer.getPageNumber(), fPie),
                    der,
                    doc.bottom() - 10f,
                    0);
        }

    }

    private String construirNombreArchivo(ExamenGenerado examen, String letraTema) {
        String area = examen.getArea().getCodigo().name();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "Examen_" + area + "_Tema_" + letraTema + "_" + ts + ".pdf";
    }

    private PdfExamenResponse construirResponse(PdfExamenGenerado reg, ExamenGenerado examen) {
        return PdfExamenResponse.builder()
                .id(reg.getId())
                .examenId(examen.getId())
                .nombreExamen(examen.getNombreExamen())
                .area(examen.getArea().getCodigo().name())
                .letraTema(reg.getLetraTema())
                .tipo(TipoExamenPdf.EXAMEN)
                .nombreArchivo(reg.getNombreArchivo())
                .urlVer("/api/examenes/pdf/ver/" + reg.getNombreArchivo())
                .urlDescargar("/api/examenes/pdf/descargar/" + reg.getNombreArchivo())
                .fechaGeneracion(reg.getFechaGeneracion())
                .mensaje("PDF generado correctamente.")
                .build();
    }

    private ExamenGenerado obtenerExamen(Long examenId) {
        return examenGeneradoRepository.findById(examenId)
                .orElseThrow(() -> new IllegalArgumentException("Examen no encontrado: " + examenId));
    }

    private TemaExamen obtenerTema(ExamenGenerado examen, String letraTema) {
        return temaExamenRepository.findByExamenAndLetraTemaIgnoreCase(examen, letraTema.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el tema " + letraTema + " para el examen indicado."));
    }
}
