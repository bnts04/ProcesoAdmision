package com.admision.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
public class ArchivoPreguntaService {

    private static final String CARPETA_UPLOADS = "uploads/preguntas";
    private static final String RUTA_PUBLICA = "/uploads/preguntas/";

    public String guardarImagenPregunta(String codigoPregunta, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        validarArchivoImagen(archivo);

        try {
            Path carpeta = Paths.get(CARPETA_UPLOADS).toAbsolutePath().normalize();
            Files.createDirectories(carpeta);

            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreSeguro = codigoPregunta + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            Path destino = carpeta.resolve(nombreSeguro).normalize();
            archivo.transferTo(destino.toFile());

            return RUTA_PUBLICA + nombreSeguro;

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen de la pregunta.", e);
        }
    }

    public void eliminarImagenPregunta(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.trim().isEmpty()) {
            return;
        }

        if (!imagenUrl.startsWith(RUTA_PUBLICA)) {
            return;
        }

        try {
            String nombreArchivo = imagenUrl.substring(RUTA_PUBLICA.length());

            Path carpeta = Paths.get(CARPETA_UPLOADS).toAbsolutePath().normalize();
            Path archivo = carpeta.resolve(nombreArchivo).normalize();

            if (archivo.startsWith(carpeta)) {
                Files.deleteIfExists(archivo);
            }

        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar la imagen de la pregunta.", e);
        }
    }

    private void validarArchivoImagen(MultipartFile archivo) {
        String contentType = archivo.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPG, PNG o WEBP.");
        }

        long maximoBytes = 5 * 1024 * 1024;

        if (archivo.getSize() > maximoBytes) {
            throw new IllegalArgumentException("La imagen no debe superar los 5 MB.");
        }
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return ".png";
        }

        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf(".")).toLowerCase(Locale.ROOT);

        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".webp")) {
            return ".png";
        }

        return extension;
    }
}