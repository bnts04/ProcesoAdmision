package com.admision.service;

import com.admision.dto.importacion.FilaPreguntaImportacion;
import com.admision.dto.importacion.IncidenciaImportacionResponse;
import com.admision.dto.importacion.ResultadoImportacionBancoResponse;
import com.admision.exception.PreguntaDuplicadaImportacionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportadorBancoPreguntasService {

    private final LectorExcelPreguntasService lectorExcelPreguntasService;
    private final LectorImagenesZipService lectorImagenesZipService;
    private final ImportacionFilaPreguntaService importacionFilaPreguntaService;

    public ResultadoImportacionBancoResponse importarBanco(
            MultipartFile archivoExcel,
            MultipartFile archivoImagenes
    ) {
        /*
         * Primero se lee completamente el Excel.
         * Todavía no se registra ninguna pregunta.
         */
        List<FilaPreguntaImportacion> filas =
                lectorExcelPreguntasService
                        .leerPreguntas(archivoExcel);

        /*
         * Verifica si alguna fila del Excel indica
         * que la pregunta tiene una imagen.
         */
        boolean excelContieneImagenes =
                filas.stream()
                        .anyMatch(fila ->
                                fila.getNombreImagen() != null
                                        && !fila.getNombreImagen()
                                        .trim()
                                        .isEmpty()
                        );

        /*
         * El ZIP es opcional cuando todas las preguntas
         * del Excel son únicamente de texto.
         */
        boolean zipNoFueAdjuntado =
                archivoImagenes == null
                        || archivoImagenes.isEmpty();

        /*
         * Cuando el Excel contiene nombres de imágenes,
         * el ZIP sí se vuelve obligatorio.
         *
         * Esta validación ocurre antes de guardar preguntas,
         * evitando una importación parcial por ausencia del ZIP.
         */
        if (excelContieneImagenes && zipNoFueAdjuntado) {
            throw new IllegalArgumentException(
                    "El Excel contiene preguntas con imagen. "
                            + "Debe adjuntar también el archivo ZIP de imágenes."
            );
        }

        /*
         * Si no se adjuntó ZIP, el servicio devolverá un mapa vacío.
         * Si se adjuntó, cargará sus imágenes temporalmente en memoria.
         */
        Map<String, byte[]> imagenes =
                lectorImagenesZipService
                        .leerImagenes(archivoImagenes);

        int importadas = 0;
        int omitidas = 0;
        int conImagen = 0;
        int sinImagen = 0;
        int duplicadas = 0;
        int errores = 0;

        List<IncidenciaImportacionResponse> incidencias =
                new ArrayList<>();

        for (FilaPreguntaImportacion fila : filas) {
            try {
                byte[] contenidoImagen = null;

                /*
                 * Solo busca una imagen en el ZIP cuando la columna
                 * Imagen de esta fila contiene un nombre.
                 */
                if (fila.getNombreImagen() != null
                        && !fila.getNombreImagen()
                        .trim()
                        .isEmpty()) {

                    contenidoImagen =
                            lectorImagenesZipService
                                    .buscarImagen(
                                            imagenes,
                                            fila.getNombreImagen()
                                    );

                    /*
                     * El ZIP fue adjuntado, pero no contiene
                     * la imagen indicada por esta pregunta.
                     */
                    if (contenidoImagen == null) {
                        throw new IllegalArgumentException(
                                "No se encontró en el ZIP la imagen indicada: "
                                        + fila.getNombreImagen()
                        );
                    }
                }

                boolean fueImportadaConImagen =
                        importacionFilaPreguntaService
                                .importarFila(
                                        fila,
                                        contenidoImagen
                                );

                importadas++;

                if (fueImportadaConImagen) {
                    conImagen++;
                } else {
                    sinImagen++;
                }

            } catch (PreguntaDuplicadaImportacionException ex) {
                duplicadas++;
                omitidas++;

                incidencias.add(
                        crearIncidencia(
                                fila,
                                "DUPLICADA",
                                ex.getMessage()
                        )
                );

            } catch (IllegalArgumentException ex) {
                errores++;
                omitidas++;

                incidencias.add(
                        crearIncidencia(
                                fila,
                                "ERROR",
                                ex.getMessage()
                        )
                );

            } catch (RuntimeException ex) {
                errores++;
                omitidas++;

                String mensaje =
                        ex.getMessage() == null
                                || ex.getMessage().trim().isEmpty()
                                ? "Ocurrió un error inesperado al importar la pregunta."
                                : ex.getMessage();

                incidencias.add(
                        crearIncidencia(
                                fila,
                                "ERROR",
                                mensaje
                        )
                );
            }
        }

        String mensaje;

        if (omitidas == 0) {
            mensaje =
                    "Importación del banco finalizada correctamente.";
        } else {
            mensaje =
                    "Importación finalizada con incidencias. "
                            + "Revise el detalle de las filas omitidas.";
        }

        return ResultadoImportacionBancoResponse.builder()
                .mensaje(mensaje)
                .totalFilasLeidas(filas.size())
                .preguntasImportadas(importadas)
                .preguntasOmitidas(omitidas)
                .preguntasConImagen(conImagen)
                .preguntasSinImagen(sinImagen)
                .preguntasDuplicadas(duplicadas)
                .totalErrores(errores)
                .incidencias(incidencias)
                .build();
    }

    private IncidenciaImportacionResponse crearIncidencia(
            FilaPreguntaImportacion fila,
            String tipo,
            String motivo
    ) {
        return IncidenciaImportacionResponse.builder()
                .filaExcel(fila.getFilaExcel())
                .numeroPregunta(fila.getNumeroPregunta())
                .tipo(tipo)
                .motivo(motivo)
                .build();
    }
}