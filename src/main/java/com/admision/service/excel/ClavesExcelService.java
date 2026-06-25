package com.admision.service.excel;

import com.admision.dto.ClaveTemaResponse;
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
public class ClavesExcelService {

    private final ArchivoCargadoRepository archivoCargadoRepository;

    public List<ClaveTemaResponse> leerClaves(Long procesoId) {
        ArchivoCargado archivoClaves = archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, TipoArchivo.CLAVES)
                .orElseThrow(() -> new RuntimeException("No se encontró archivo CLAVES para este proceso"));

        String nombreArchivo = archivoClaves.getNombreOriginal().toLowerCase();

        if (nombreArchivo.endsWith(".dbf")) {
            return leerClavesDesdeDbf(archivoClaves);
        }

        if (nombreArchivo.endsWith(".xls") || nombreArchivo.endsWith(".xlsx")) {
            return leerClavesDesdeExcel(archivoClaves);
        }

        throw new RuntimeException("Formato no soportado para CLAVES: " + archivoClaves.getNombreOriginal());
    }

    private List<ClaveTemaResponse> leerClavesDesdeExcel(ArchivoCargado archivoClaves) {
        try (InputStream inputStream = new FileInputStream(Paths.get(archivoClaves.getRutaArchivo()).toFile());
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {
                throw new RuntimeException("El archivo CLAVES no tiene hojas");
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new RuntimeException("El archivo CLAVES no tiene encabezados");
            }

            Map<String, Integer> columnas = obtenerColumnasExcel(headerRow);

            validarColumnaObligatoria(columnas, "LITHO");
            validarColumnaObligatoria(columnas, "TEMA");

            List<ClaveTemaResponse> claves = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null || filaVacia(row, formatter)) {
                    continue;
                }

                String litho = obtenerValorExcel(row, columnas.get("LITHO"), formatter);
                String tema = obtenerValorExcel(row, columnas.get("TEMA"), formatter);
                String secuencia = columnas.containsKey("SECUENCIA")
                        ? obtenerValorExcel(row, columnas.get("SECUENCIA"), formatter)
                        : null;

                Map<String, String> respuestas = new LinkedHashMap<>();

                for (int pregunta = 1; pregunta <= 100; pregunta++) {
                    String nombreColumna = String.format("PREG_%03d", pregunta);

                    if (!columnas.containsKey(nombreColumna)) {
                        throw new RuntimeException("El archivo CLAVES no contiene la columna " + nombreColumna);
                    }

                    String respuesta = obtenerValorExcel(row, columnas.get(nombreColumna), formatter);
                    respuestas.put(nombreColumna, respuesta);
                }

                claves.add(ClaveTemaResponse.builder()
                        .litho(litho)
                        .tema(tema)
                        .secuencia(secuencia)
                        .totalPreguntas(respuestas.size())
                        .respuestas(respuestas)
                        .build());
            }

            return claves;

        } catch (Exception e) {
            throw new RuntimeException("Error al leer archivo CLAVES Excel: " + e.getMessage(), e);
        }
    }

    private List<ClaveTemaResponse> leerClavesDesdeDbf(ArchivoCargado archivoClaves) {
        try (InputStream inputStream = new FileInputStream(Paths.get(archivoClaves.getRutaArchivo()).toFile())) {

            DBFReader reader = new DBFReader(inputStream);
            reader.setCharactersetName("ISO-8859-1");

            Map<String, Integer> columnas = obtenerColumnasDbf(reader);

            validarColumnaObligatoria(columnas, "LITHO");
            validarColumnaObligatoria(columnas, "TEMA");

            List<ClaveTemaResponse> claves = new ArrayList<>();

            Object[] fila;

            while ((fila = reader.nextRecord()) != null) {
                String litho = obtenerValorDbf(fila, columnas.get("LITHO"));
                String tema = obtenerValorDbf(fila, columnas.get("TEMA"));
                String secuencia = columnas.containsKey("SECUENCIA")
                        ? obtenerValorDbf(fila, columnas.get("SECUENCIA"))
                        : null;

                Map<String, String> respuestas = new LinkedHashMap<>();

                for (int pregunta = 1; pregunta <= 100; pregunta++) {
                    String nombreColumna = String.format("PREG_%03d", pregunta);

                    if (!columnas.containsKey(nombreColumna)) {
                        throw new RuntimeException("El archivo CLAVES.DBF no contiene la columna " + nombreColumna);
                    }

                    String respuesta = obtenerValorDbf(fila, columnas.get(nombreColumna));
                    respuestas.put(nombreColumna, respuesta);
                }

                claves.add(ClaveTemaResponse.builder()
                        .litho(litho)
                        .tema(tema)
                        .secuencia(secuencia)
                        .totalPreguntas(respuestas.size())
                        .respuestas(respuestas)
                        .build());
            }

            return claves;

        } catch (Exception e) {
            throw new RuntimeException("Error al leer archivo CLAVES.DBF: " + e.getMessage(), e);
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
            throw new RuntimeException("El archivo CLAVES no contiene la columna obligatoria " + nombreColumna);
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