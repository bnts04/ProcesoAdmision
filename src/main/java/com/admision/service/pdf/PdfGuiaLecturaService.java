package com.admision.service.pdf;

import com.admision.dto.LecturaPdfGuiaResponse;
import com.admision.dto.PostulantePdfGuiaResponse;
import com.admision.entity.ArchivoCargado;
import com.admision.enums.TipoArchivo;
import com.admision.repository.ArchivoCargadoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PdfGuiaLecturaService {

    private final ArchivoCargadoRepository archivoCargadoRepository;

    /*
     * Formato esperado:
     * 77533284 NAVARRETE ASCENCIO, FRANCESCA DUBRASKA 1198.375 001 INGRESO0001
     * 75102373 MATTA MENDIETA, KEICY MILAGROS 0.000 AUSENTE0047
     */
    private static final Pattern LINEA_POSTULANTE = Pattern.compile(
            "^(\\d{8})\\s+(.+?)\\s+(\\d+\\.\\d{3})\\s+(?:(\\d{3})\\s+)?(INGRESO|NO INGRESO|AUSENTE)(\\d{4})$"
    );

    public LecturaPdfGuiaResponse leerPdfGuia(Long procesoId, Integer limite) {
        ArchivoCargado archivoPdf = obtenerPdfGuiaDisponible(procesoId);

        try {
            File file = Paths.get(archivoPdf.getRutaArchivo()).toFile();

            List<PostulantePdfGuiaResponse> registros = extraerRegistrosDesdePdf(file);

            int maximo = limite != null && limite > 0 ? limite : registros.size();

            List<PostulantePdfGuiaResponse> mostrados = registros.stream()
                    .limit(maximo)
                    .toList();

            boolean pdfDelProcesoActual = archivoPdf.getProceso().getId().equals(procesoId);
            boolean pdfGlobalUtilizado = !pdfDelProcesoActual;

            return LecturaPdfGuiaResponse.builder()
                    .procesoId(procesoId)
                    .procesoPdfUsado(archivoPdf.getProceso().getId())
                    .nombreArchivo(archivoPdf.getNombreOriginal())
                    .pdfDelProcesoActual(pdfDelProcesoActual)
                    .pdfGlobalUtilizado(pdfGlobalUtilizado)
                    .totalRegistrosLeidos(registros.size())
                    .totalMostrados(mostrados.size())
                    .registros(mostrados)
                    .mensaje(pdfDelProcesoActual
                            ? "PDF guía leído desde el proceso actual"
                            : "PDF guía global reutilizado correctamente")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error al leer PDF guía: " + e.getMessage(), e);
        }
    }

    public List<PostulantePdfGuiaResponse> obtenerTodosLosRegistros(Long procesoId) {
        ArchivoCargado archivoPdf = obtenerPdfGuiaDisponible(procesoId);

        try {
            File file = Paths.get(archivoPdf.getRutaArchivo()).toFile();
            return extraerRegistrosDesdePdf(file);
        } catch (Exception e) {
            throw new RuntimeException("Error al leer PDF guía: " + e.getMessage(), e);
        }
    }

    private ArchivoCargado obtenerPdfGuiaDisponible(Long procesoId) {
        /*
         * 1. Primero busca PDF_RESULTADOS en el proceso actual.
         * 2. Si no existe, usa el último PDF_RESULTADOS guardado en el sistema.
         */
        return archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, TipoArchivo.PDF_RESULTADOS)
                .orElseGet(() -> archivoCargadoRepository
                        .findTopByTipoArchivoOrderByFechaCargaDesc(TipoArchivo.PDF_RESULTADOS)
                        .orElseThrow(() -> new RuntimeException(
                                "No existe PDF guía disponible. Debe cargar al menos un PDF_RESULTADOS en el sistema."
                        ))
                );
    }

    private List<PostulantePdfGuiaResponse> extraerRegistrosDesdePdf(File file) throws Exception {
        List<PostulantePdfGuiaResponse> registros = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String texto = stripper.getText(document);

            String facultadActual = "";
            String carreraActual = "";

            String[] lineas = texto.split("\\R");

            for (String lineaOriginal : lineas) {
                String linea = normalizarEspacios(lineaOriginal);

                if (linea.isBlank()) {
                    continue;
                }

                if (linea.toUpperCase().startsWith("FACULTAD:")) {
                    facultadActual = linea.substring(linea.indexOf(":") + 1).trim();
                    continue;
                }

                if (linea.toUpperCase().startsWith("CARRERA PROFESIONAL:")) {
                    carreraActual = linea.substring(linea.indexOf(":") + 1).trim();
                    continue;
                }

                Matcher matcher = LINEA_POSTULANTE.matcher(linea);

                if (matcher.matches()) {
                    String codigo = matcher.group(1);
                    String nombre = matcher.group(2).trim();
                    String puntaje = matcher.group(3);
                    String merito = matcher.group(4) == null ? "" : matcher.group(4);
                    String condicion = matcher.group(5);
                    String secuencia = matcher.group(6);

                    registros.add(PostulantePdfGuiaResponse.builder()
                            .codigo(codigo)
                            .nombre(nombre)
                            .facultad(facultadActual)
                            .carrera(carreraActual)
                            .puntajePdf(puntaje)
                            .meritoPdf(merito)
                            .condicionPdf(condicion)
                            .secuenciaPdf(secuencia)
                            .build());
                }
            }
        }

        return registros;
    }

    private String normalizarEspacios(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.trim().replaceAll("\\s+", " ");
    }
}