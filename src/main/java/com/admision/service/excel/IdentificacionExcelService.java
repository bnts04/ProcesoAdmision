package com.admision.service.excel;

import com.admision.dto.IdentificacionPostulanteExcelResponse;
import com.admision.entity.ArchivoCargado;
import com.admision.enums.TipoArchivo;
import com.admision.repository.ArchivoCargadoRepository;
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

            Map<String, Integer> columnas = obtenerColumnas(headerRow);

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

                String litho = obtenerValor(row, columnas.get("LITHO"), formatter);
                String tema = obtenerValor(row, columnas.get("TEMA"), formatter);
                String codigo = obtenerValor(row, columnas.get("CODIGO"), formatter);
                String secuencia = obtenerValor(row, columnas.get("SECUENCIA"), formatter);

                identificaciones.add(IdentificacionPostulanteExcelResponse.builder()
                        .litho(litho)
                        .tema(tema)
                        .codigo(codigo)
                        .secuencia(secuencia)
                        .build());
            }

            return identificaciones;

        } catch (Exception e) {
            throw new RuntimeException("Error al leer archivo IDENTIFI: " + e.getMessage(), e);
        }
    }

    private Map<String, Integer> obtenerColumnas(Row headerRow) {
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

    private String normalizarEncabezado(String encabezado) {
        if (encabezado == null) {
            return "";
        }

        String limpio = encabezado.trim();

        /*
         * Algunos encabezados pueden venir desde DBF convertidos así:
         * LITHO,C,6
         * TEMA,C,1
         * CODIGO,C,8
         * SECUENCIA,N,5
         *
         * Por eso tomamos solo la primera parte antes de la coma.
         */
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

    private String obtenerValor(Row row, Integer indiceColumna, DataFormatter formatter) {
        if (indiceColumna == null) {
            return "";
        }

        Cell cell = row.getCell(indiceColumna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
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