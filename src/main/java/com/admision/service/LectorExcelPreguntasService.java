package com.admision.service;

import com.admision.dto.importacion.FilaPreguntaImportacion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class LectorExcelPreguntasService {

    private static final String NOMBRE_HOJA = "Banco Final";

    private static final int MAXIMO_FILAS = 5000;

    /*
     * Posición de las columnas en el Excel.
     * Apache POI comienza a contar desde cero.
     */
    private static final int COLUMNA_NUMERO = 0;
    private static final int COLUMNA_COMPONENTE = 1;
    private static final int COLUMNA_SUBCURSO = 2;
    private static final int COLUMNA_ENUNCIADO = 3;
    private static final int COLUMNA_IMAGEN = 4;
    private static final int COLUMNA_A = 5;
    private static final int COLUMNA_B = 6;
    private static final int COLUMNA_C = 7;
    private static final int COLUMNA_D = 8;
    private static final int COLUMNA_E = 9;
    private static final int COLUMNA_CORRECTA = 10;
    private static final int COLUMNA_OBSERVACION = 11;

    private static final String[] ENCABEZADOS_ESPERADOS = {
            "Número",
            "Componente",
            "Subcurso",
            "Enunciado",
            "Imagen",
            "A",
            "B",
            "C",
            "D",
            "E",
            "Correcta",
            "Observación"
    };

    /**
     * Lee la hoja "Banco Final" y convierte cada fila
     * en un objeto FilaPreguntaImportacion.
     *
     * Todavía no registra información en la base de datos.
     */
    public List<FilaPreguntaImportacion> leerPreguntas(
            MultipartFile archivoExcel
    ) {
        validarArchivoExcel(archivoExcel);

        try (
                InputStream inputStream = archivoExcel.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            Sheet hoja = workbook.getSheet(NOMBRE_HOJA);

            if (hoja == null) {
                throw new IllegalArgumentException(
                        "El Excel no contiene la hoja obligatoria: "
                                + NOMBRE_HOJA
                );
            }

            FormulaEvaluator evaluator =
                    workbook.getCreationHelper()
                            .createFormulaEvaluator();

            DataFormatter formatter =
                    new DataFormatter(new Locale("es", "PE"));

            validarEncabezados(hoja, formatter, evaluator);

            int ultimaFila = hoja.getLastRowNum();

            /*
             * getLastRowNum() comienza desde cero.
             * Por eso se suma uno para obtener la cantidad real.
             */
            if (ultimaFila + 1 > MAXIMO_FILAS) {
                throw new IllegalArgumentException(
                        "El Excel supera el máximo permitido de "
                                + MAXIMO_FILAS
                                + " filas."
                );
            }

            List<FilaPreguntaImportacion> preguntas =
                    new ArrayList<>();

            /*
             * Se comienza en 1 porque la fila 0
             * contiene los encabezados.
             */
            for (int indiceFila = 1;
                 indiceFila <= ultimaFila;
                 indiceFila++) {

                Row fila = hoja.getRow(indiceFila);

                if (fila == null
                        || filaEstaCompletamenteVacia(
                        fila,
                        formatter,
                        evaluator
                )) {
                    continue;
                }

                FilaPreguntaImportacion pregunta =
                        convertirFila(
                                fila,
                                indiceFila + 1,
                                formatter,
                                evaluator
                        );

                preguntas.add(pregunta);
            }

            if (preguntas.isEmpty()) {
                throw new IllegalArgumentException(
                        "La hoja Banco Final no contiene preguntas."
                );
            }

            return preguntas;

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo leer el archivo Excel de preguntas.",
                    e
            );
        }
    }

    private FilaPreguntaImportacion convertirFila(
            Row fila,
            Integer numeroRealFila,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        return FilaPreguntaImportacion.builder()
                .filaExcel(numeroRealFila)
                .numeroPregunta(
                        obtenerTexto(
                                fila,
                                COLUMNA_NUMERO,
                                formatter,
                                evaluator
                        )
                )
                .componente(
                        obtenerTexto(
                                fila,
                                COLUMNA_COMPONENTE,
                                formatter,
                                evaluator
                        )
                )
                .subcurso(
                        obtenerTexto(
                                fila,
                                COLUMNA_SUBCURSO,
                                formatter,
                                evaluator
                        )
                )
                .enunciado(
                        obtenerTexto(
                                fila,
                                COLUMNA_ENUNCIADO,
                                formatter,
                                evaluator
                        )
                )
                .nombreImagen(
                        obtenerTextoOpcional(
                                fila,
                                COLUMNA_IMAGEN,
                                formatter,
                                evaluator
                        )
                )
                .alternativaA(
                        obtenerTexto(
                                fila,
                                COLUMNA_A,
                                formatter,
                                evaluator
                        )
                )
                .alternativaB(
                        obtenerTexto(
                                fila,
                                COLUMNA_B,
                                formatter,
                                evaluator
                        )
                )
                .alternativaC(
                        obtenerTexto(
                                fila,
                                COLUMNA_C,
                                formatter,
                                evaluator
                        )
                )
                .alternativaD(
                        obtenerTexto(
                                fila,
                                COLUMNA_D,
                                formatter,
                                evaluator
                        )
                )
                .alternativaE(
                        obtenerTexto(
                                fila,
                                COLUMNA_E,
                                formatter,
                                evaluator
                        )
                )
                .respuestaCorrecta(
                        obtenerTexto(
                                fila,
                                COLUMNA_CORRECTA,
                                formatter,
                                evaluator
                        )
                )
                .observacion(
                        obtenerTextoOpcional(
                                fila,
                                COLUMNA_OBSERVACION,
                                formatter,
                                evaluator
                        )
                )
                .build();
    }

    private void validarArchivoExcel(MultipartFile archivoExcel) {
        if (archivoExcel == null || archivoExcel.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe adjuntar el archivo Excel del banco de preguntas."
            );
        }

        String nombreOriginal =
                archivoExcel.getOriginalFilename();

        if (nombreOriginal == null
                || !nombreOriginal
                .toLowerCase(Locale.ROOT)
                .endsWith(".xlsx")) {

            throw new IllegalArgumentException(
                    "El banco de preguntas debe ser un archivo XLSX."
            );
        }
    }

    private void validarEncabezados(
            Sheet hoja,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        Row filaEncabezados = hoja.getRow(0);

        if (filaEncabezados == null) {
            throw new IllegalArgumentException(
                    "El Excel no contiene la fila de encabezados."
            );
        }

        for (int columna = 0;
             columna < ENCABEZADOS_ESPERADOS.length;
             columna++) {

            String encabezadoEncontrado =
                    obtenerTexto(
                            filaEncabezados,
                            columna,
                            formatter,
                            evaluator
                    );

            String encabezadoEsperado =
                    ENCABEZADOS_ESPERADOS[columna];

            if (!normalizarTexto(encabezadoEncontrado)
                    .equals(normalizarTexto(encabezadoEsperado))) {

                throw new IllegalArgumentException(
                        "Encabezado incorrecto en la columna "
                                + convertirNumeroColumnaALetra(columna)
                                + ". Se esperaba \""
                                + encabezadoEsperado
                                + "\" pero se encontró \""
                                + encabezadoEncontrado
                                + "\"."
                );
            }
        }
    }

    private boolean filaEstaCompletamenteVacia(
            Row fila,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        for (int columna = 0;
             columna < ENCABEZADOS_ESPERADOS.length;
             columna++) {

            String contenido =
                    obtenerTexto(
                            fila,
                            columna,
                            formatter,
                            evaluator
                    );

            if (!contenido.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private String obtenerTexto(
            Row fila,
            int columna,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        Cell celda = fila.getCell(
                columna,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );

        if (celda == null) {
            return "";
        }

        String valor =
                formatter.formatCellValue(celda, evaluator);

        return valor == null ? "" : valor.trim();
    }

    private String obtenerTextoOpcional(
            Row fila,
            int columna,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        String valor =
                obtenerTexto(
                        fila,
                        columna,
                        formatter,
                        evaluator
                );

        return valor.isEmpty() ? null : valor;
    }

    /**
     * Se utiliza para comparar encabezados ignorando:
     *
     * - mayúsculas y minúsculas;
     * - tildes;
     * - espacios al inicio y final;
     * - espacios repetidos.
     */
    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        String sinTildes =
                Normalizer.normalize(
                                valor,
                                Normalizer.Form.NFD
                        )
                        .replaceAll("\\p{M}", "");

        return sinTildes
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private String convertirNumeroColumnaALetra(int numeroColumna) {
        StringBuilder resultado = new StringBuilder();

        int numero = numeroColumna + 1;

        while (numero > 0) {
            int residuo = (numero - 1) % 26;

            resultado.insert(
                    0,
                    (char) ('A' + residuo)
            );

            numero = (numero - 1) / 26;
        }

        return resultado.toString();
    }
}