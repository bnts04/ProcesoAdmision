package com.admision.service.examen;

import com.admision.dto.pdf.PdfExamenResponse;
import com.admision.entity.ExamenGenerado;
import com.admision.entity.PdfExamenGenerado;
import com.admision.entity.TemaAlternativa;
import com.admision.entity.TemaExamen;
import com.admision.entity.TemaPregunta;
import com.admision.enums.TipoExamenPdf;
import com.admision.repository.ExamenGeneradoRepository;
import com.admision.repository.PdfExamenGeneradoRepository;
import com.admision.repository.TemaAlternativaRepository;
import com.admision.repository.TemaExamenRepository;
import com.admision.repository.TemaPreguntaRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
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

    private static final float ESPACIO_ENTRE_COLUMNAS = 18f;
    private static final float ESPACIO_ENTRE_PREGUNTAS = 5f;

    @Transactional
    public List<PdfExamenResponse> generarPdfExamenTodos(Long examenId) {
        ExamenGenerado examen = obtenerExamen(examenId);

        List<TemaExamen> temas =
                temaExamenRepository.findByExamenOrderByLetraTemaAsc(examen);

        if (temas.isEmpty()) {
            throw new IllegalStateException(
                    "El examen no tiene temas generados."
            );
        }

        return temas.stream()
                .map(t -> generarPdfExamenUnTema(examen, t))
                .toList();
    }

    @Transactional
    public PdfExamenResponse generarPdfExamenTema(
            Long examenId,
            String letraTema
    ) {
        ExamenGenerado examen = obtenerExamen(examenId);
        TemaExamen tema = obtenerTema(examen, letraTema);

        return generarPdfExamenUnTema(examen, tema);
    }

    private PdfExamenResponse generarPdfExamenUnTema(
            ExamenGenerado examen,
            TemaExamen tema
    ) {
        try {
            Files.createDirectories(Paths.get(carpetaPdfs));

            String nombreArchivo =
                    construirNombreArchivo(
                            examen,
                            tema.getLetraTema()
                    );

            Path rutaArchivo =
                    Paths.get(carpetaPdfs)
                            .resolve(nombreArchivo)
                            .normalize();

            List<TemaPregunta> preguntas =
                    temaPreguntaRepository
                            .findByTemaOrderByNumeroPreguntaAsc(tema);

            escribirPdf(
                    rutaArchivo,
                    examen,
                    tema,
                    preguntas
            );

            pdfExamenGeneradoRepository
                    .findByExamenAndLetraTemaAndTipo(
                            examen,
                            tema.getLetraTema(),
                            TipoExamenPdf.EXAMEN
                    )
                    .ifPresent(pdfExamenGeneradoRepository::delete);

            PdfExamenGenerado registro =
                    pdfExamenGeneradoRepository.save(
                            PdfExamenGenerado.builder()
                                    .examen(examen)
                                    .letraTema(tema.getLetraTema())
                                    .tipo(TipoExamenPdf.EXAMEN)
                                    .nombreArchivo(nombreArchivo)
                                    .rutaArchivo(rutaArchivo.toString())
                                    .fechaGeneracion(LocalDateTime.now())
                                    .build()
                    );

            return construirResponse(registro, examen);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al generar PDF del examen: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private void escribirPdf(
            Path rutaArchivo,
            ExamenGenerado examen,
            TemaExamen tema,
            List<TemaPregunta> preguntas
    ) throws Exception {

        Document doc =
                new Document(
                        PageSize.A4,
                        36,
                        36,
                        72,
                        38
                );

        PdfWriter writer =
                PdfWriter.getInstance(
                        doc,
                        new FileOutputStream(rutaArchivo.toFile())
                );

        writer.setPageEvent(
                new EncabezadoPieEvento(examen, tema)
        );

        doc.open();

        PdfContentByte contenido =
                writer.getDirectContent();

        float anchoDisponible =
                doc.right() - doc.left();

        float anchoColumna =
                (anchoDisponible - ESPACIO_ENTRE_COLUMNAS) / 2f;

        float columnaIzquierdaX =
                doc.left();

        float columnaDerechaX =
                doc.left()
                        + anchoColumna
                        + ESPACIO_ENTRE_COLUMNAS;

        float ySuperior =
                doc.top();

        float yInferior =
                doc.bottom();

        int columnaActual = 0;
        float yActual = ySuperior;

        for (TemaPregunta pregunta : preguntas) {
            PdfPTable bloque =
                    construirBloquePregunta(
                            pregunta,
                            anchoColumna
                    );

            float alturaBloque =
                    calcularAlturaBloque(bloque);

            float altoDisponibleEnColumna =
                    yActual - yInferior;

            if (alturaBloque > altoDisponibleEnColumna) {
                if (columnaActual == 0) {
                    columnaActual = 1;
                    yActual = ySuperior;
                } else {
                    doc.newPage();
                    columnaActual = 0;
                    yActual = ySuperior;
                }
            }

            float posicionX =
                    columnaActual == 0
                            ? columnaIzquierdaX
                            : columnaDerechaX;

            bloque.writeSelectedRows(
                    0,
                    -1,
                    posicionX,
                    yActual,
                    contenido
            );

            yActual -= alturaBloque + ESPACIO_ENTRE_PREGUNTAS;
        }

        doc.close();
    }

    private PdfPTable construirBloquePregunta(
            TemaPregunta pregunta,
            float anchoColumna
    ) {
        Font fEnunciado =
                new Font(
                        Font.HELVETICA,
                        8.7f,
                        Font.NORMAL,
                        NEGRO
                );

        Font fNegrita =
                new Font(
                        Font.HELVETICA,
                        8.7f,
                        Font.BOLD,
                        NEGRO
                );

        Font fAlternativa =
                new Font(
                        Font.HELVETICA,
                        8.1f,
                        Font.NORMAL,
                        NEGRO
                );

        PdfPTable bloque =
                new PdfPTable(1);

        bloque.setTotalWidth(anchoColumna);
        bloque.setLockedWidth(true);
        bloque.setSplitRows(false);
        bloque.setSplitLate(false);

        PdfPCell celdaPregunta =
                new PdfPCell();

        celdaPregunta.setBorder(Rectangle.NO_BORDER);
        celdaPregunta.setPaddingTop(1.5f);
        celdaPregunta.setPaddingBottom(2f);
        celdaPregunta.setPaddingLeft(1.5f);
        celdaPregunta.setPaddingRight(1.5f);
        celdaPregunta.setUseAscender(true);
        celdaPregunta.setUseDescender(true);

        Paragraph enunciado =
                new Paragraph();

        enunciado.setLeading(10.5f);
        enunciado.setSpacingBefore(0f);
        enunciado.setSpacingAfter(2f);

        enunciado.add(
                new Chunk(
                        pregunta.getNumeroPregunta() + ") ",
                        fNegrita
                )
        );

        String texto =
                pregunta.getEnunciado() == null
                        ? ""
                        : pregunta.getEnunciado();

        String[] lineas =
                texto.split("\n");

        for (int i = 0; i < lineas.length; i++) {
            enunciado.add(
                    new Chunk(
                            lineas[i],
                            fEnunciado
                    )
            );

            if (i < lineas.length - 1) {
                enunciado.add(Chunk.NEWLINE);
            }
        }

        celdaPregunta.addElement(enunciado);

        agregarImagenPreguntaSiExiste(
                celdaPregunta,
                pregunta,
                anchoColumna
        );

        List<TemaAlternativa> alternativas =
                temaAlternativaRepository
                        .findByTemaPreguntaOrderByLetraFinalAsc(
                                pregunta
                        );

        agregarAlternativas(
                celdaPregunta,
                alternativas,
                fNegrita,
                fAlternativa
        );

        bloque.addCell(celdaPregunta);
        bloque.calculateHeights(true);

        return bloque;
    }

    private float calcularAlturaBloque(
            PdfPTable bloque
    ) {
        bloque.calculateHeights(true);
        return bloque.getTotalHeight();
    }

    private void agregarImagenPreguntaSiExiste(
            PdfPCell celdaPregunta,
            TemaPregunta pregunta,
            float anchoColumna
    ) {
        String imagenUrl =
                pregunta.getImagenUrl();

        if (imagenUrl == null || imagenUrl.isBlank()) {
            return;
        }

        try {
            Path rutaImagen =
                    resolverRutaImagen(imagenUrl);

            if (rutaImagen == null
                    || !Files.exists(rutaImagen)) {

                System.out.println(
                        "Imagen no encontrada para PDF: "
                                + imagenUrl
                );
                return;
            }

            Image img =
                    Image.getInstance(
                            rutaImagen
                                    .toAbsolutePath()
                                    .toString()
                    );

            /*
             * La imagen queda dentro de la columna,
             * no ocupa toda la página.
             */
            float anchoMaximo =
                    Math.min(
                            anchoColumna - 12f,
                            205f
                    );

            float altoMaximo =
                    125f;

            img.scaleToFit(
                    anchoMaximo,
                    altoMaximo
            );

            img.setAlignment(Image.ALIGN_CENTER);
            img.setSpacingBefore(2f);
            img.setSpacingAfter(3f);

            celdaPregunta.addElement(img);

        } catch (Exception e) {
            System.out.println(
                    "No se pudo insertar imagen en PDF: "
                            + imagenUrl
                            + " | "
                            + e.getMessage()
            );
        }
    }

    private Path resolverRutaImagen(
            String imagenUrl
    ) {
        if (imagenUrl == null || imagenUrl.isBlank()) {
            return null;
        }

        String valor =
                imagenUrl
                        .trim()
                        .replace("\\", "/");

        String nombreImagen =
                valor.contains("/")
                        ? valor.substring(
                        valor.lastIndexOf("/") + 1
                )
                        : valor;

        Path rutaConfigurada =
                Paths.get(carpetaImagenes)
                        .resolve(nombreImagen)
                        .toAbsolutePath()
                        .normalize();

        if (Files.exists(rutaConfigurada)) {
            return rutaConfigurada;
        }

        Path rutaPreguntas =
                Paths.get("uploads", "preguntas")
                        .resolve(nombreImagen)
                        .toAbsolutePath()
                        .normalize();

        if (Files.exists(rutaPreguntas)) {
            return rutaPreguntas;
        }

        String rutaRelativa =
                valor.startsWith("/")
                        ? valor.substring(1)
                        : valor;

        Path rutaDesdeUrl =
                Paths.get(rutaRelativa)
                        .toAbsolutePath()
                        .normalize();

        if (Files.exists(rutaDesdeUrl)) {
            return rutaDesdeUrl;
        }

        return null;
    }

    private void agregarAlternativas(
            PdfPCell celdaPregunta,
            List<TemaAlternativa> alternativas,
            Font fNegrita,
            Font fAlternativa
    ) {
        if (alternativas == null || alternativas.isEmpty()) {
            return;
        }

        /*
         * Solo usamos alternativas horizontales cuando TODAS son muy cortas.
         * Si tienen espacios o son medianas, pasan a formato vertical.
         */
        boolean alternativasHorizontales =
                alternativas.stream()
                        .allMatch(a -> {
                            if (a.getTexto() == null) {
                                return false;
                            }

                            String texto = a.getTexto().trim();

                            return texto.length() <= 7
                                    && !texto.contains(" ");
                        });

        if (alternativasHorizontales) {
            PdfPTable tablaAlts =
                    new PdfPTable(5);

            tablaAlts.setWidthPercentage(100);
            tablaAlts.setSpacingBefore(2f);

            for (TemaAlternativa alt : alternativas) {
                PdfPCell celdaAlt =
                        new PdfPCell();

                celdaAlt.setBorder(Rectangle.NO_BORDER);
                celdaAlt.setPadding(0.7f);
                celdaAlt.setUseAscender(true);
                celdaAlt.setUseDescender(true);

                Paragraph pAlt =
                        new Paragraph();

                pAlt.setLeading(9.3f);

                pAlt.add(
                        new Chunk(
                                alt.getLetraFinal().name() + ") ",
                                fNegrita
                        )
                );

                pAlt.add(
                        new Chunk(
                                alt.getTexto().trim(),
                                fAlternativa
                        )
                );

                celdaAlt.addElement(pAlt);
                tablaAlts.addCell(celdaAlt);
            }

            celdaPregunta.addElement(tablaAlts);

        } else {
            for (TemaAlternativa alt : alternativas) {
                Paragraph pAlt =
                        new Paragraph();

                pAlt.setLeading(9.5f);
                pAlt.setSpacingBefore(0.2f);

                pAlt.add(
                        new Chunk(
                                alt.getLetraFinal().name() + ") ",
                                fNegrita
                        )
                );

                pAlt.add(
                        new Chunk(
                                alt.getTexto() == null
                                        ? ""
                                        : alt.getTexto().trim(),
                                fAlternativa
                        )
                );

                celdaPregunta.addElement(pAlt);
            }
        }
    }

    private static class EncabezadoPieEvento
            extends PdfPageEventHelper {

        private final ExamenGenerado examen;
        private final TemaExamen tema;

        EncabezadoPieEvento(
                ExamenGenerado examen,
                TemaExamen tema
        ) {
            this.examen = examen;
            this.tema = tema;
        }

        @Override
        public void onEndPage(
                PdfWriter writer,
                Document document
        ) {
            PdfContentByte cb =
                    writer.getDirectContent();

            Rectangle pagina =
                    writer.getPageSize();

            float anchoPagina =
                    pagina.getWidth();

            float altoPagina =
                    pagina.getHeight();

            float centro =
                    anchoPagina / 2f;

            float margenDerecho =
                    anchoPagina - 36f;

            float yTitulo =
                    altoPagina - 42f;

            Font fTitulo =
                    new Font(
                            Font.HELVETICA,
                            13.5f,
                            Font.BOLD,
                            NEGRO
                    );

            Font fSubtitulo =
                    new Font(
                            Font.HELVETICA,
                            10.5f,
                            Font.BOLD,
                            NEGRO
                    );

            String nombreExamen =
                    examen.getNombreExamen() == null
                            ? "EXAMEN"
                            : examen.getNombreExamen()
                            .toUpperCase();

            String area =
                    examen.getArea()
                            .getCodigo()
                            .name()
                            .replace("AREA_", "");

            String subtitulo =
                    "ÁREA "
                            + area
                            + " - TEMA "
                            + tema.getLetraTema()
                            .toUpperCase();

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_CENTER,
                    new Phrase(
                            nombreExamen,
                            fTitulo
                    ),
                    centro,
                    yTitulo,
                    0
            );

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_CENTER,
                    new Phrase(
                            subtitulo,
                            fSubtitulo
                    ),
                    centro,
                    yTitulo - 14f,
                    0
            );

            Font fPie =
                    new Font(
                            Font.HELVETICA,
                            8.5f,
                            Font.BOLD,
                            NEGRO
                    );

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_RIGHT,
                    new Phrase(
                            "Página: "
                                    + writer.getPageNumber(),
                            fPie
                    ),
                    margenDerecho,
                    28f,
                    0
            );
        }
    }

    private String construirNombreArchivo(
            ExamenGenerado examen,
            String letraTema
    ) {
        String area =
                examen.getArea()
                        .getCodigo()
                        .name();

        String timestamp =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter
                                        .ofPattern(
                                                "yyyyMMdd_HHmmss"
                                        )
                        );

        return "Examen_"
                + area
                + "_Tema_"
                + letraTema
                + "_"
                + timestamp
                + ".pdf";
    }

    private PdfExamenResponse construirResponse(
            PdfExamenGenerado registro,
            ExamenGenerado examen
    ) {
        return PdfExamenResponse.builder()
                .id(registro.getId())
                .examenId(examen.getId())
                .nombreExamen(examen.getNombreExamen())
                .area(
                        examen.getArea()
                                .getCodigo()
                                .name()
                )
                .letraTema(registro.getLetraTema())
                .tipo(TipoExamenPdf.EXAMEN)
                .nombreArchivo(registro.getNombreArchivo())
                .urlVer(
                        "/api/examenes/pdf/ver/"
                                + registro.getNombreArchivo()
                )
                .urlDescargar(
                        "/api/examenes/pdf/descargar/"
                                + registro.getNombreArchivo()
                )
                .fechaGeneracion(
                        registro.getFechaGeneracion()
                )
                .mensaje("PDF generado correctamente.")
                .build();
    }

    private ExamenGenerado obtenerExamen(
            Long examenId
    ) {
        return examenGeneradoRepository
                .findById(examenId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Examen no encontrado: "
                                        + examenId
                        )
                );
    }

    private TemaExamen obtenerTema(
            ExamenGenerado examen,
            String letraTema
    ) {
        return temaExamenRepository
                .findByExamenAndLetraTemaIgnoreCase(
                        examen,
                        letraTema.trim()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el tema "
                                        + letraTema
                                        + " para el examen indicado."
                        )
                );
    }
}