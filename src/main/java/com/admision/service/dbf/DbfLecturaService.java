package com.admision.service.dbf;

import com.admision.dto.DbfLecturaResponse;
import com.admision.entity.ArchivoCargado;
import com.admision.enums.TipoArchivo;
import com.admision.repository.ArchivoCargadoRepository;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DbfLecturaService {

    private final ArchivoCargadoRepository archivoCargadoRepository;

    public DbfLecturaResponse leerDbf(Long procesoId, TipoArchivo tipoArchivo, Integer limite) {
        ArchivoCargado archivo = archivoCargadoRepository
                .findTopByProcesoIdAndTipoArchivoOrderByFechaCargaDesc(procesoId, tipoArchivo)
                .orElseThrow(() -> new RuntimeException("No se encontró archivo " + tipoArchivo + " para este proceso"));

        if (!archivo.getNombreOriginal().toLowerCase().endsWith(".dbf")) {
            throw new RuntimeException("El archivo más reciente de tipo " + tipoArchivo + " no es DBF");
        }

        int maximo = limite != null && limite > 0 ? limite : 20;

        try (InputStream inputStream = new FileInputStream(Paths.get(archivo.getRutaArchivo()).toFile())) {

            DBFReader reader = new DBFReader(inputStream);

            /*
             * Los DBF antiguos suelen usar codificación latin.
             * Si los acentos se ven mal, probamos con windows-1252.
             */
            reader.setCharactersetName("ISO-8859-1");

            int totalColumnas = reader.getFieldCount();

            List<String> columnas = new ArrayList<>();

            for (int i = 0; i < totalColumnas; i++) {
                DBFField field = reader.getField(i);
                columnas.add(field.getName().trim().toUpperCase());
            }

            List<Map<String, String>> registros = new ArrayList<>();

            Object[] fila;

            while ((fila = reader.nextRecord()) != null) {
                if (registros.size() >= maximo) {
                    break;
                }

                Map<String, String> registro = new LinkedHashMap<>();

                for (int i = 0; i < totalColumnas; i++) {
                    String columna = columnas.get(i);
                    Object valor = fila[i];

                    registro.put(columna, valor == null ? "" : valor.toString().trim());
                }

                registros.add(registro);
            }

            return DbfLecturaResponse.builder()
                    .procesoId(procesoId)
                    .tipoArchivo(tipoArchivo.name())
                    .nombreArchivo(archivo.getNombreOriginal())
                    .totalColumnas(totalColumnas)
                    .totalRegistrosLeidos(registros.size())
                    .columnas(columnas)
                    .registros(registros)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error al leer DBF " + tipoArchivo + ": " + e.getMessage(), e);
        }
    }
}