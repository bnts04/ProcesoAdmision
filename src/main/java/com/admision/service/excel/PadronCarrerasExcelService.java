package com.admision.service.excel;

import com.admision.dto.PadronCarreraExcelResponse;
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
public class PadronCarrerasExcelService {

    private final ArchivoCargadoRepository archivoCargadoRepository;

    public List<PadronCarreraExcelResponse> leerPadronCarreras(Long procesoId, Integer limite) {
        ArchivoCargado archivoPadron = archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, TipoArchivo.PADRON_CARRERAS)
                .orElseThrow(() -> new RuntimeException("No se encontró archivo PADRON_CARRERAS para este proceso"));

        try (InputStream inputStream = new FileInputStream(Paths.get(archivoPadron.getRutaArchivo()).toFile());
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = obtenerHojaPrincipal(workbook);

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new RuntimeException("El archivo PADRON_CARRERAS no tiene encabezados");
            }

            Map<String, Integer> columnas = obtenerColumnas(headerRow);

            validarColumnaObligatoria(columnas, "CODIGO");
            validarColumnaObligatoria(columnas, "CARRERA");
            validarColumnaObligatoria(columnas, "FACULTAD");

            List<PadronCarreraExcelResponse> registros = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();

            int maximo = limite != null && limite > 0 ? limite : Integer.MAX_VALUE;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                if (registros.size() >= maximo) {
                    break;
                }

                Row row = sheet.getRow(i);

                if (row == null || filaVacia(row, formatter)) {
                    continue;
                }

                String litho = columnas.containsKey("LITHO")
                        ? obtenerValor(row, columnas.get("LITHO"), formatter)
                        : "";

                String tema = columnas.containsKey("TEMA")
                        ? obtenerValor(row, columnas.get("TEMA"), formatter)
                        : "";

                String codigo = obtenerValor(row, columnas.get("CODIGO"), formatter);
                String carrera = obtenerValor(row, columnas.get("CARRERA"), formatter);
                String facultad = obtenerValor(row, columnas.get("FACULTAD"), formatter);

                if (codigo.isBlank()) {
                    continue;
                }

                registros.add(PadronCarreraExcelResponse.builder()
                        .litho(litho)
                        .tema(tema)
                        .codigo(codigo)
                        .carrera(carrera)
                        .facultad(facultad)
                        .build());
            }

            return registros;

        } catch (Exception e) {
            throw new RuntimeException("Error al leer archivo PADRON_CARRERAS: " + e.getMessage(), e);
        }
    }

    private Sheet obtenerHojaPrincipal(Workbook workbook) {
        Sheet hoja1 = workbook.getSheet("Hoja1");

        if (hoja1 != null) {
            return hoja1;
        }

        Sheet primeraHoja = workbook.getSheetAt(0);

        if (primeraHoja == null) {
            throw new RuntimeException("El archivo PADRON_CARRERAS no tiene hojas");
        }

        return primeraHoja;
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

        if (limpio.contains(",")) {
            limpio = limpio.substring(0, limpio.indexOf(","));
        }

        return limpio.trim().toUpperCase();
    }

    private void validarColumnaObligatoria(Map<String, Integer> columnas, String nombreColumna) {
        if (!columnas.containsKey(nombreColumna)) {
            throw new RuntimeException("El archivo PADRON_CARRERAS no contiene la columna obligatoria " + nombreColumna);
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