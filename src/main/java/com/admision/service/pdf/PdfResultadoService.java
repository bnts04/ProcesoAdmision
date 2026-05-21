package com.admision.service.pdf;

import com.admision.dto.PdfGeneradoResponse;
import com.admision.entity.PdfGenerado;
import com.admision.entity.ProcesoAdmision;
import com.admision.entity.ResultadoPostulante;
import com.admision.enums.CondicionPostulante;
import com.admision.repository.PdfGeneradoRepository;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfResultadoService {

    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final PdfGeneradoRepository pdfGeneradoRepository;

    @Value("${app.storage.pdfs}")
    private String carpetaPdfs;

    public PdfGeneradoResponse generarPdfGeneral(Long procesoId) {
        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId);

        if (resultados.isEmpty()) {
            throw new RuntimeException("No existen resultados para generar PDF");
        }

        try {
            Files.createDirectories(Paths.get(carpetaPdfs));

            String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nombreArchivo = "resultados_por_carrera_proceso_" + procesoId + "_" + fechaHora + ".pdf";
            Path rutaArchivo = Paths.get(carpetaPdfs).resolve(nombreArchivo);

            Document document = new Document(PageSize.A4, 28, 28, 45, 35);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo.toFile()));
            writer.setPageEvent(new FooterPagina());

            document.open();

            Map<String, List<ResultadoPostulante>> resultadosPorCarrera = resultados.stream()
                    .filter(r -> r.getCarrera() != null && !r.getCarrera().isBlank())
                    .filter(r -> !r.getCarrera().equalsIgnoreCase("PENDIENTE"))
                    .collect(Collectors.groupingBy(ResultadoPostulante::getCarrera));

            List<String> carrerasOrdenadas = resultadosPorCarrera.keySet()
                    .stream()
                    .sorted()
                    .toList();

            boolean primeraCarrera = true;

            for (String carrera : carrerasOrdenadas) {
                List<ResultadoPostulante> grupo = ordenarResultados(resultadosPorCarrera.get(carrera));

                if (!primeraCarrera) {
                    document.newPage();
                }

                agregarEncabezado(document, proceso, grupo.get(0));
                agregarTablaResultados(document, grupo);

                primeraCarrera = false;
            }

            document.close();

            registrarPdfGenerado(
                    proceso,
                    "GENERAL_POR_CARRERA",
                    nombreArchivo,
                    rutaArchivo.toString(),
                    null
            );

            return construirPdfGeneradoResponse(
                    procesoId,
                    "GENERAL_POR_CARRERA",
                    nombreArchivo,
                    rutaArchivo.toString(),
                    "PDF de resultados por carrera generado correctamente"
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de resultados: " + e.getMessage(), e);
        }
    }

    public PdfGeneradoResponse generarPdfPorCarrera(Long procesoId, String nombreCarrera) {
        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        if (nombreCarrera == null || nombreCarrera.isBlank()) {
            throw new RuntimeException("Debe enviar el nombre de la carrera");
        }

        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId)
                .stream()
                .filter(r -> normalizarTexto(r.getCarrera()).equals(normalizarTexto(nombreCarrera)))
                .toList();

        if (resultados.isEmpty()) {
            throw new RuntimeException("No existen resultados para la carrera: " + nombreCarrera);
        }

        List<ResultadoPostulante> resultadosOrdenados = ordenarResultados(resultados);

        try {
            Files.createDirectories(Paths.get(carpetaPdfs));

            String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String carreraArchivo = normalizarNombreArchivo(nombreCarrera);

            String nombreArchivo = "resultados_" + carreraArchivo + "_proceso_" + procesoId + "_" + fechaHora + ".pdf";
            Path rutaArchivo = Paths.get(carpetaPdfs).resolve(nombreArchivo);

            Document document = new Document(PageSize.A4, 28, 28, 45, 35);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo.toFile()));
            writer.setPageEvent(new FooterPagina());

            document.open();

            agregarEncabezado(document, proceso, resultadosOrdenados.get(0));
            agregarTablaResultados(document, resultadosOrdenados);

            document.close();

            registrarPdfGenerado(
                    proceso,
                    "CARRERA",
                    nombreArchivo,
                    rutaArchivo.toString(),
                    resultadosOrdenados.get(0).getCarrera()
            );

            return construirPdfGeneradoResponse(
                    procesoId,
                    "CARRERA",
                    nombreArchivo,
                    rutaArchivo.toString(),
                    "PDF de carrera generado correctamente"
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF por carrera: " + e.getMessage(), e);
        }
    }

    private List<ResultadoPostulante> ordenarResultados(List<ResultadoPostulante> resultados) {
        return resultados.stream()
                .sorted(
                        Comparator.comparing(
                                        ResultadoPostulante::getOme,
                                        Comparator.nullsLast(Integer::compareTo)
                                )
                                .thenComparing(
                                        ResultadoPostulante::getPuntajeFinal,
                                        Comparator.nullsLast(Comparator.reverseOrder())
                                )
                                .thenComparing(
                                        ResultadoPostulante::getCodigo,
                                        Comparator.nullsLast(String::compareTo)
                                )
                )
                .toList();
    }

    private void agregarEncabezado(
            Document document,
            ProcesoAdmision proceso,
            ResultadoPostulante primerResultado
    ) throws DocumentException {

        Font universidadFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

        Paragraph universidad = new Paragraph("UNIVERSIDAD NACIONAL \"SAN LUIS GONZAGA\"", universidadFont);
        universidad.setAlignment(Element.ALIGN_CENTER);
        universidad.setSpacingAfter(2);
        document.add(universidad);

        Paragraph comision = new Paragraph("COMISION EJECUTIVA CENTRAL DE ADMISION", tituloFont);
        comision.setAlignment(Element.ALIGN_CENTER);
        comision.setSpacingAfter(2);
        document.add(comision);

        Paragraph procesoTexto = new Paragraph(valor(proceso.getNombreProceso()).toUpperCase(), tituloFont);
        procesoTexto.setAlignment(Element.ALIGN_CENTER);
        procesoTexto.setSpacingAfter(6);
        document.add(procesoTexto);

        Paragraph resultados = new Paragraph("RESULTADOS POR CARRERA PROFESIONAL", tituloFont);
        resultados.setAlignment(Element.ALIGN_CENTER);
        resultados.setSpacingAfter(8);
        document.add(resultados);

        Paragraph modalidad = new Paragraph("Modalidad: " + valor(proceso.getModalidad()).toUpperCase(), normalFont);
        modalidad.setAlignment(Element.ALIGN_LEFT);
        modalidad.setSpacingAfter(3);
        document.add(modalidad);

        Paragraph facultad = new Paragraph();
        facultad.add(new Chunk("Facultad: ", labelFont));
        facultad.add(new Chunk(valor(primerResultado.getFacultad()).toUpperCase(), normalFont));
        facultad.setSpacingAfter(3);
        document.add(facultad);

        Paragraph carrera = new Paragraph();
        carrera.add(new Chunk("Carrera Profesional: ", labelFont));
        carrera.add(new Chunk(valor(primerResultado.getCarrera()).toUpperCase(), normalFont));
        carrera.setSpacingAfter(8);
        document.add(carrera);
    }

    private void agregarTablaResultados(Document document, List<ResultadoPostulante> resultados) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{14, 43, 13, 9, 14, 7});
        table.setHeaderRows(1);

        agregarCeldaHeader(table, "CODIGO", headerFont);
        agregarCeldaHeader(table, "NOMBRE", headerFont);
        agregarCeldaHeader(table, "PUNTAJE", headerFont);
        agregarCeldaHeader(table, "MERITO", headerFont);
        agregarCeldaHeader(table, "CONDICION", headerFont);
        agregarCeldaHeader(table, "SEC", headerFont);

        int secuencia = 1;

        for (ResultadoPostulante r : resultados) {
            agregarCeldaBody(table, valor(r.getCodigo()), bodyFont, Element.ALIGN_CENTER);
            agregarCeldaBody(table, valor(r.getApellidosNombres()).toUpperCase(), bodyFont, Element.ALIGN_LEFT);
            agregarCeldaBody(table, formatoPuntaje(r.getPuntajeFinal()), bodyFont, Element.ALIGN_RIGHT);
            agregarCeldaBody(table, formatoMerito(r.getOme()), bodyFont, Element.ALIGN_CENTER);
            agregarCeldaBody(table, formatoCondicion(r.getCondicion()), bodyFont, Element.ALIGN_CENTER);
            agregarCeldaBody(table, String.format("%04d", secuencia++), bodyFont, Element.ALIGN_CENTER);
        }

        document.add(table);
    }

    private void agregarCeldaHeader(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new Color(240, 240, 240));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void agregarCeldaBody(PdfPTable table, String texto, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private String formatoPuntaje(BigDecimal puntaje) {
        if (puntaje == null) {
            return "0.0000";
        }

        return puntaje.setScale(4, RoundingMode.HALF_UP).toString();
    }

    private String formatoMerito(Integer merito) {
        if (merito == null) {
            return "";
        }

        return String.format("%03d", merito);
    }

    private String formatoCondicion(CondicionPostulante condicion) {
        if (condicion == null) {
            return "";
        }

        if (condicion == CondicionPostulante.NO_INGRESO) {
            return "NO INGRESO";
        }

        return condicion.name();
    }

    private String valor(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor.trim().toUpperCase();

        limpio = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        limpio = limpio.replaceAll("\\s+", " ");

        return limpio;
    }

    private String normalizarNombreArchivo(String valor) {
        String limpio = normalizarTexto(valor);

        limpio = limpio.replaceAll("[^A-Z0-9]+", "_");
        limpio = limpio.replaceAll("_+", "_");

        if (limpio.startsWith("_")) {
            limpio = limpio.substring(1);
        }

        if (limpio.endsWith("_")) {
            limpio = limpio.substring(0, limpio.length() - 1);
        }

        if (limpio.isBlank()) {
            limpio = "carrera";
        }

        return limpio.toLowerCase();
    }

    private PdfGeneradoResponse construirPdfGeneradoResponse(
            Long procesoId,
            String tipoPdf,
            String nombreArchivo,
            String rutaArchivo,
            String mensaje
    ) {
        return PdfGeneradoResponse.builder()
                .procesoId(procesoId)
                .tipoPdf(tipoPdf)
                .nombreArchivo(nombreArchivo)
                .rutaArchivo(rutaArchivo)
                .urlVer("/api/pdf/ver/" + nombreArchivo)
                .urlDescargar("/api/pdf/descargar/" + nombreArchivo)
                .mensaje(mensaje)
                .build();
    }

    private void registrarPdfGenerado(
            ProcesoAdmision proceso,
            String tipoPdf,
            String nombreArchivo,
            String rutaArchivo,
            String carrera
    ) {
        PdfGenerado pdfGenerado = PdfGenerado.builder()
                .proceso(proceso)
                .tipoPdf(tipoPdf)
                .nombreArchivo(nombreArchivo)
                .rutaArchivo(rutaArchivo)
                .carrera(carrera)
                .build();

        pdfGeneradoRepository.save(pdfGenerado);
    }

    private static class FooterPagina extends PdfPageEventHelper {

        private final Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy"));
            String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            int pagina = writer.getPageNumber();

            Phrase footer = new Phrase(
                    "UNICA    PAGINA: " + pagina + "    FECHA: " + fecha + "    HORA: " + hora,
                    footerFont
            );

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_CENTER,
                    footer,
                    (document.right() + document.left()) / 2,
                    document.bottom() - 15,
                    0
            );
        }
    }
}