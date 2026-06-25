package com.admision.service.excel;

import com.admision.dto.RespuestaPostulanteExcelResponse;
import com.admision.entity.ArchivoCargado;
import com.admision.enums.TipoArchivo;
import com.admision.repository.ArchivoCargadoRepository;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RespuestasExcelService {

    private final ArchivoCargadoRepository archivoCargadoRepository;

    public List<RespuestaPostulanteExcelResponse> leerRespuestas(Long procesoId, Integer limite) {
        ArchivoCargado archivoRespuestas = archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, TipoArchivo.RESPUEST)
                .orElseThrow(() -> new RuntimeException("No se encontró archivo RESPUEST para este proceso"));

        String nombreArchivo = archivoRespuestas.getNombreOriginal().toLowerCase();

        if (nombreArchivo.endsWith(".dbf")) {
            return leerRespuestasDesdeDbf(archivoRespuestas, limite);
        }

        if (nombreArchivo.endsWith(".xls") || nombreArchivo.endsWith(".xlsx")) {
            return leerRespuestasDesdeExcel(archivoRespuestas, limite);
        }

        throw new RuntimeException("Formato no soportado para RESPUEST: " + archivoRespuestas.getNombreOriginal());
    }

    private List<RespuestaPostulanteExcelResponse> leerRespuestasDesdeExcel(
            ArchivoCargado archivoRespuestas,
            Integer limite
    ) {
        try (InputStream inputStream = new FileInputStream(Paths.get(archivoRespuestas.getRutaArchivo()).toFile());
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {
                throw new RuntimeException("El archivo RESPUEST no tiene hojas");
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new RuntimeException("El archivo RESPUEST no tiene encabezados");
            }

            Map<String, Integer> columnas = obtenerColumnasExcel(headerRow);

            validarColumnaObligatoria(columnas, "LITHO");
            validarColumnaObligatoria(columnas, "TEMA");

            List<RespuestaPostulanteExcelResponse> respuestasPostulantes = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();

            int maximo = limite != null && limite > 0 ? limite : Integer.MAX_VALUE;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                if (respuestasPostulantes.size() >= maximo) {
                    break;
                }

                Row row = sheet.getRow(i);

                if (row == null || filaVacia(row, formatter)) {
                    continue;
                }

                String litho = obtenerValorExcel(row, columnas.get("LITHO"), formatter);
                String tema = obtenerValorExcel(row, columnas.get("TEMA"), formatter);

                Map<String, String> respuestas = new LinkedHashMap<>();

                for (int pregunta = 1; pregunta <= 100; pregunta++) {
                    String nombreColumna = String.format("PREG_%03d", pregunta);

                    if (!columnas.containsKey(nombreColumna)) {
                        throw new RuntimeException("El archivo RESPUEST no contiene la columna " + nombreColumna);
                    }

                    String respuesta = obtenerValorExcel(row, columnas.get(nombreColumna), formatter);
                    respuestas.put(nombreColumna, respuesta);
                }

                respuestasPostulantes.add(RespuestaPostulanteExcelResponse.builder()
                        .litho(litho)
                        .tema(tema)
                        .totalPreguntas(respuestas.size())
                        .respuestas(respuestas)
                        .build());
            }

            return respuestasPostulantes;

        } catch (Exception e) {
            throw new RuntimeException("Error al leer archivo RESPUEST Excel: " + e.getMessage(), e);
        }
    }

    private List<RespuestaPostulanteExcelResponse> leerRespuestasDesdeDbf(
            ArchivoCargado archivoRespuestas,
            Integer limite
    ) {
        try (InputStream inputStream = new FileInputStream(Paths.get(archivoRespuestas.getRutaArchivo()).toFile())) {

            DBFReader reader = new DBFReader(inputStream);
            reader.setCharactersetName("ISO-8859-1");

            Map<String, Integer> columnas = obtenerColumnasDbf(reader);

            validarColumnaObligatoria(columnas, "LITHO");
            validarColumnaObligatoria(columnas, "TEMA");

            List<RespuestaPostulanteExcelResponse> respuestasPostulantes = new ArrayList<>();

            int maximo = limite != null && limite > 0 ? limite : Integer.MAX_VALUE;

            Object[] fila;

            while ((fila = reader.nextRecord()) != null) {
                if (respuestasPostulantes.size() >= maximo) {
                    break;
                }

                String litho = obtenerValorDbf(fila, columnas.get("LITHO"));
                String tema = obtenerValorDbf(fila, columnas.get("TEMA"));

                Map<String, String> respuestas = new LinkedHashMap<>();

                for (int pregunta = 1; pregunta <= 100; pregunta++) {
                    String nombreColumna = String.format("PREG_%03d", pregunta);

                    if (!columnas.containsKey(nombreColumna)) {
                        throw new RuntimeException("El archivo RESPUEST.DBF no contiene la columna " + nombreColumna);
                    }

                    String respuesta = obtenerValorDbf(fila, columnas.get(nombreColumna));
                    respuestas.put(nombreColumna, respuesta);
                }

                respuestasPostulantes.add(RespuestaPostulanteExcelResponse.builder()
                        .litho(litho)
                        .tema(tema)
                        .totalPreguntas(respuestas.size())
                        .respuestas(respuestas)
                        .build());
            }

            return respuestasPostulantes;

        } catch (Exception e) {
            throw new RuntimeException("Error al leer archivo RESPUEST.DBF: " + e.getMessage(), e);
        }
    }

    private Map<String, Integer> obtenerColumnasExcel(Row headerRow) {
        Map<String, Integer> columnas = new HashMap<>();
        DataFormatter formatter = new DataFormatter();

        for (Cell cell : headerRow) {
            String nombreColumna = normalizarEncabezado(formatter.formatCellValue(cell));

            if (!nombreColumna.isBlank()) {
                columnas.put(nombreColumna, cell.getColumnIndex());
            }
        }

        return columnas;
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

    private void validarColumnaObligatoria(Map<String, Integer> columnas, String nombreColumna) {
        if (!columnas.containsKey(nombreColumna)) {
            throw new RuntimeException("El archivo RESPUEST no contiene la columna obligatoria " + nombreColumna);
        }
    }

    private String obtenerValorExcel(Row row, Integer indiceColumna, DataFormatter formatter) {
        if (indiceColumna == null) {
            return "";
        }

        Cell cell = row.getCell(indiceColumna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    private String obtenerValorDbf(Object[] fila, Integer indiceColumna) {
        if (indiceColumna == null || fila == null || indiceColumna >= fila.length) {
            return "";
        }

        Object valor = fila[indiceColumna];

        if (valor == null) {
            return "";
        }

        return valor.toString().trim();
    }

    private boolean filaVacia(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            String valor = formatter.formatCellValue(cell).trim();

            if (!valor.isBlank()) {
                return false;
            }
        }

        return true;
    }
}