package com.admision.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class ArchivoPreguntaService {

    private static final String CARPETA_UPLOADS = "uploads/preguntas";
    private static final String RUTA_PUBLICA = "/uploads/preguntas/";

    private static final long TAMANO_MAXIMO_BYTES =
            5L * 1024L * 1024L;

    /**
     * Se utiliza cuando una imagen se registra manualmente
     * desde el frontend o desde Postman.
     */
    public String guardarImagenPregunta(
            String codigoPregunta,
            MultipartFile archivo
    ) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        validarArchivoImagen(archivo);

        try {
            return guardarContenidoImagen(
                    codigoPregunta,
                    archivo.getOriginalFilename(),
                    archivo.getBytes()
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo leer la imagen de la pregunta.",
                    e
            );
        }
    }

    /**
     * Se utilizará en la importación masiva.
     * La imagen será extraída del ZIP como arreglo de bytes.
     */
    public String guardarImagenPreguntaDesdeBytes(
            String codigoPregunta,
            String nombreOriginal,
            byte[] contenido
    ) {
        if (contenido == null || contenido.length == 0) {
            return null;
        }

        validarImagenDesdeBytes(nombreOriginal, contenido);

        return guardarContenidoImagen(
                codigoPregunta,
                nombreOriginal,
                contenido
        );
    }

    /**
     * Elimina físicamente la imagen guardada en uploads/preguntas.
     */
    public void eliminarImagenPregunta(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.trim().isEmpty()) {
            return;
        }

        if (!imagenUrl.startsWith(RUTA_PUBLICA)) {
            return;
        }

        try {
            String nombreArchivo =
                    imagenUrl.substring(RUTA_PUBLICA.length());

            Path carpeta = obtenerCarpetaUploads();
            Path archivo = carpeta.resolve(nombreArchivo).normalize();

            /*
             * Evita que una ruta manipulada pueda acceder
             * fuera de uploads/preguntas.
             */
            if (!archivo.startsWith(carpeta)) {
                throw new IllegalArgumentException(
                        "La ruta de la imagen no es válida."
                );
            }

            Files.deleteIfExists(archivo);

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo eliminar la imagen de la pregunta.",
                    e
            );
        }
    }

    /**
     * Método común utilizado tanto por el registro manual
     * como por el importador masivo.
     */
    private String guardarContenidoImagen(
            String codigoPregunta,
            String nombreOriginal,
            byte[] contenido
    ) {
        if (codigoPregunta == null
                || codigoPregunta.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código de la pregunta es obligatorio para guardar la imagen."
            );
        }

        try {
            Path carpeta = obtenerCarpetaUploads();
            Files.createDirectories(carpeta);

            String extension =
                    obtenerExtensionValida(nombreOriginal);

            String nombreSeguro =
                    codigoPregunta.trim().toUpperCase(Locale.ROOT)
                            + "_"
                            + UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            + extension;

            Path destino =
                    carpeta.resolve(nombreSeguro).normalize();

            if (!destino.startsWith(carpeta)) {
                throw new IllegalArgumentException(
                        "La ruta de destino de la imagen no es válida."
                );
            }

            Files.write(
                    destino,
                    contenido,
                    StandardOpenOption.CREATE_NEW
            );

            return RUTA_PUBLICA + nombreSeguro;

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo guardar la imagen de la pregunta.",
                    e
            );
        }
    }

    /**
     * Valida una imagen recibida mediante MultipartFile.
     */
    private void validarArchivoImagen(MultipartFile archivo) {
        String contentType = archivo.getContentType();

        if (contentType == null
                || !esContentTypePermitido(contentType)) {
            throw new IllegalArgumentException(
                    "Solo se permiten imágenes JPG, JPEG, PNG o WEBP."
            );
        }

        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new IllegalArgumentException(
                    "La imagen no debe superar los 5 MB."
            );
        }

        obtenerExtensionValida(
                archivo.getOriginalFilename()
        );
    }

    /**
     * Valida una imagen extraída del ZIP.
     * Como no tenemos MultipartFile, se verifica el nombre,
     * la extensión y el tamaño de los bytes.
     */
    private void validarImagenDesdeBytes(
            String nombreOriginal,
            byte[] contenido
    ) {
        obtenerExtensionValida(nombreOriginal);

        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new IllegalArgumentException(
                    "La imagen no debe superar los 5 MB: "
                            + nombreOriginal
            );
        }
    }

    private boolean esContentTypePermitido(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/jpg")
                || contentType.equalsIgnoreCase("image/png")
                || contentType.equalsIgnoreCase("image/webp");
    }

    /**
     * Obtiene y valida la extensión del archivo.
     */
    private String obtenerExtensionValida(String nombreOriginal) {
        if (nombreOriginal == null
                || nombreOriginal.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La imagen no tiene un nombre válido."
            );
        }

        String nombre =
                nombreOriginal.trim().toLowerCase(Locale.ROOT);

        int posicionPunto = nombre.lastIndexOf(".");

        if (posicionPunto < 0) {
            throw new IllegalArgumentException(
                    "La imagen no tiene extensión: "
                            + nombreOriginal
            );
        }

        String extension =
                nombre.substring(posicionPunto);

        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".webp")) {
            throw new IllegalArgumentException(
                    "Solo se permiten imágenes JPG, JPEG, PNG o WEBP: "
                            + nombreOriginal
            );
        }

        return extension;
    }

    private Path obtenerCarpetaUploads() {
        return Paths.get(CARPETA_UPLOADS)
                .toAbsolutePath()
                .normalize();
    }
}