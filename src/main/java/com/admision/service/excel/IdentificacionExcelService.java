package com.admision.service.excel;

import com.admision.dto.IdentificacionPostulanteExcelResponse;
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
public class IdentificacionExcelService {

    private final ArchivoCargadoRepository archivoCargadoRepository;

    public List<IdentificacionPostulanteExcelResponse> leerIdentificaciones(Long procesoId, Integer limite) {
        ArchivoCargado archivoIdentifi = archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, TipoArchivo.IDENTIFI)
                .orElseThrow(() -> new RuntimeException("No se encontró archivo IDENTIFI para este proceso"));

        String nombreArchivo = archivoIdentifi.getNombreOriginal().toLowerCase();

        if (nombreArchivo.endsWith(".dbf")) {
            return leerIdentificacionesDesdeDbf(archivoIdentifi, limite);
        }

        if (nombreArchivo.endsWith(".xls") || nombreArchivo.endsWith(".xlsx")) {
            return leerIdentificacionesDesdeExcel(archivoIdentifi, limite);
        }

        throw new RuntimeException("Formato no soportado para IDENTIFI: " + archivoIdentifi.getNombreOriginal());
    }

    private List<IdentificacionPostulanteExcelResponse> leerIdentificacionesDesdeExcel(
            ArchivoCargado archivoIdentifi,
            Integer limite
    ) {
        try (InputStream inputStream = new FileInputStream(Paths.get(archivoIdentifi.getRutaArchivo()).toFile());
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {
                throw new RuntimeException("El archivo IDENTIFI no tiene hojas");
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new RuntimeException("El archivo IDENTIFI no tiene encabezados");
            }

            Map<String, Integer> columnas = obtenerColumnasExcel(headerRow);

            validarColumnaObligatoria(columnas, "LITHO");
            validarColumnaObligatoria(columnas, "TEMA");
            validarColumnaObligatoria(columnas, "CODIGO");
            validarColumnaObligatoria(columnas, "SECUENCIA");

            List<IdentificacionPostulanteExcelResponse> identificaciones = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();

            int maximo = limite != null && limite > 0 ? limite : Integer.MAX_VALUE;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                if (identificaciones.size() >= maximo) {
                    break;
                }

                Row row = sheet.getRow(i);

                if (row == null || filaVacia(row, formatter)) {
                    continue;
                }

                String litho = obtenerValorExcel(row, columnas.get("LITHO"), formatter);
                String tema = obtenerValorExcel(row, columnas.get("TEMA"), formatter);
                String codigo = obtenerValorExcel(row, columnas.get("CODIGO"), formatter);
                String secuencia = obtenerValorExcel(row, columnas.get("SECUENCIA"), formatter);

                identificaciones.add(IdentificacionPostulanteExcelResponse.builder()
                        .litho(litho)
                        .tema(tema)
                        .codigo(codigo)
                        .secuencia(secuencia)
                        .build());
            }

            return identificaciones;

        } catch (Exception e) {
            throw new RuntimeException("Error al leer archivo IDENTIFI Excel: " + e.getMessage(), e);
        }
    }

    private List<IdentificacionPostulanteExcelResponse> leerIdentificacionesDesdeDbf(
            ArchivoCargado archivoIdentifi,
            Integer limite
    ) {
        try (InputStream inputStream = new FileInputStream(Paths.get(archivoIdentifi.getRutaArchivo()).toFile())) {

            DBFReader reader = new DBFReader(inputStream);
            reader.setCharactersetName("ISO-8859-1");

            Map<String, Integer> columnas = obtenerColumnasDbf(reader);

            validarColumnaObligatoria(columnas, "LITHO");
            validarColumnaObligatoria(columnas, "TEMA");
            validarColumnaObligatoria(columnas, "CODIGO");
            validarColumnaObligatoria(columnas, "SECUENCIA");

            List<IdentificacionPostulanteExcelResponse> identificaciones = new ArrayList<>();

            int maximo = limite != null && limite > 0 ? limite : Integer.MAX_VALUE;

            Object[] fila;

            while ((fila = reader.nextRecord()) != null) {
                if (identificaciones.size() >= maximo) {
                    break;
                }

                String litho = obtenerValorDbf(fila, columnas.get("LITHO"));
                String tema = obtenerValorDbf(fila, columnas.get("TEMA"));
                String codigo = obtenerValorDbf(fila, columnas.get("CODIGO"));
                String secuencia = obtenerValorDbf(fila, columnas.get("SECUENCIA"));

                identificaciones.add(IdentificacionPostulanteExcelResponse.builder()
                        .litho(litho)
                        .tema(tema)
                        .codigo(codigo)
                        .secuencia(secuencia)
                        .build());
            }

            return identificaciones;

        } catch (Exception e) {
            throw new RuntimeException("Error al leer archivo IDENTIFI.DBF: " + e.getMessage(), e);
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
            throw new RuntimeException("El archivo IDENTIFI no contiene la columna obligatoria " + nombreColumna);
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