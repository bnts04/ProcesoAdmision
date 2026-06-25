package com.admision.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class LectorImagenesZipService {

    private static final long TAMANO_MAXIMO_IMAGEN =
            5L * 1024L * 1024L;

    private static final long TAMANO_MAXIMO_TOTAL_DESCOMPRIMIDO =
            100L * 1024L * 1024L;

    private static final int MAXIMO_ARCHIVOS = 500;

    private static final Set<String> EXTENSIONES_PERMITIDAS =
            Set.of(".jpg", ".jpeg", ".png", ".webp");

    /**
     * Lee las imágenes del ZIP y devuelve un mapa:
     *
     * nombre normalizado de la imagen -> contenido en bytes.
     *
     * Ejemplo:
     * pregunta_19.png -> byte[]
     */
    public Map<String, byte[]> leerImagenes(
            MultipartFile archivoZip
    ) {
        if (archivoZip == null || archivoZip.isEmpty()) {
            return new LinkedHashMap<>();
        }

        validarArchivoZip(archivoZip);

        Map<String, byte[]> imagenes = new LinkedHashMap<>();

        long totalDescomprimido = 0L;
        int cantidadArchivos = 0;

        try (
                InputStream inputStream = archivoZip.getInputStream();
                ZipInputStream zipInputStream =
                        new ZipInputStream(inputStream)
        ) {
            ZipEntry entrada;

            while ((entrada = zipInputStream.getNextEntry()) != null) {

                if (entrada.isDirectory()) {
                    zipInputStream.closeEntry();
                    continue;
                }

                String nombreArchivo =
                        obtenerNombreArchivo(entrada.getName());

                /*
                 * Ignora archivos internos generados por macOS,
                 * por ejemplo __MACOSX o .DS_Store.
                 */
                if (debeIgnorarse(nombreArchivo, entrada.getName())) {
                    zipInputStream.closeEntry();
                    continue;
                }

                validarExtensionImagen(nombreArchivo);

                cantidadArchivos++;

                if (cantidadArchivos > MAXIMO_ARCHIVOS) {
                    throw new IllegalArgumentException(
                            "El ZIP supera el máximo permitido de "
                                    + MAXIMO_ARCHIVOS
                                    + " archivos."
                    );
                }

                byte[] contenido = leerContenidoEntrada(
                        zipInputStream,
                        nombreArchivo
                );

                totalDescomprimido += contenido.length;

                if (totalDescomprimido
                        > TAMANO_MAXIMO_TOTAL_DESCOMPRIMIDO) {
                    throw new IllegalArgumentException(
                            "El contenido descomprimido del ZIP "
                                    + "supera los 100 MB permitidos."
                    );
                }

                String nombreNormalizado =
                        normalizarNombre(nombreArchivo);

                if (imagenes.containsKey(nombreNormalizado)) {
                    throw new IllegalArgumentException(
                            "El ZIP contiene imágenes duplicadas "
                                    + "con el mismo nombre: "
                                    + nombreArchivo
                    );
                }

                imagenes.put(nombreNormalizado, contenido);

                zipInputStream.closeEntry();
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo leer el archivo ZIP de imágenes.",
                    e
            );
        }

        return imagenes;
    }

    /**
     * Permite buscar una imagen ignorando mayúsculas,
     * minúsculas y espacios exteriores.
     */
    public byte[] buscarImagen(
            Map<String, byte[]> imagenes,
            String nombreImagen
    ) {
        if (imagenes == null
                || nombreImagen == null
                || nombreImagen.trim().isEmpty()) {
            return null;
        }

        return imagenes.get(
                normalizarNombre(nombreImagen)
        );
    }

    public boolean existeImagen(
            Map<String, byte[]> imagenes,
            String nombreImagen
    ) {
        return buscarImagen(imagenes, nombreImagen) != null;
    }

    private byte[] leerContenidoEntrada(
            ZipInputStream zipInputStream,
            String nombreArchivo
    ) throws IOException {

        ByteArrayOutputStream salida =
                new ByteArrayOutputStream();

        byte[] buffer = new byte[8192];
        int cantidadLeida;
        long totalImagen = 0L;

        while ((cantidadLeida = zipInputStream.read(buffer)) != -1) {
            totalImagen += cantidadLeida;

            if (totalImagen > TAMANO_MAXIMO_IMAGEN) {
                throw new IllegalArgumentException(
                        "La imagen supera los 5 MB permitidos: "
                                + nombreArchivo
                );
            }

            salida.write(buffer, 0, cantidadLeida);
        }

        byte[] contenido = salida.toByteArray();

        if (contenido.length == 0) {
            throw new IllegalArgumentException(
                    "La imagen está vacía dentro del ZIP: "
                            + nombreArchivo
            );
        }

        return contenido;
    }

    private void validarArchivoZip(MultipartFile archivoZip) {
        String nombreOriginal =
                archivoZip.getOriginalFilename();

        if (nombreOriginal == null
                || !nombreOriginal
                .toLowerCase(Locale.ROOT)
                .endsWith(".zip")) {

            throw new IllegalArgumentException(
                    "El archivo de imágenes debe tener formato ZIP."
            );
        }
    }

    private void validarExtensionImagen(
            String nombreArchivo
    ) {
        String nombre =
                nombreArchivo.toLowerCase(Locale.ROOT);

        boolean extensionPermitida =
                EXTENSIONES_PERMITIDAS.stream()
                        .anyMatch(nombre::endsWith);

        if (!extensionPermitida) {
            throw new IllegalArgumentException(
                    "El ZIP contiene un archivo no permitido: "
                            + nombreArchivo
                            + ". Solo se aceptan JPG, JPEG, PNG o WEBP."
            );
        }
    }

    private String obtenerNombreArchivo(String rutaEntrada) {
        if (rutaEntrada == null
                || rutaEntrada.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El ZIP contiene una entrada sin nombre."
            );
        }

        String rutaNormalizada =
                rutaEntrada.replace("\\", "/");

        int ultimaBarra =
                rutaNormalizada.lastIndexOf("/");

        String nombre = ultimaBarra >= 0
                ? rutaNormalizada.substring(ultimaBarra + 1)
                : rutaNormalizada;

        if (nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El ZIP contiene una entrada con nombre inválido."
            );
        }

        return nombre.trim();
    }

    private boolean debeIgnorarse(
            String nombreArchivo,
            String rutaCompleta
    ) {
        String nombre =
                nombreArchivo.toLowerCase(Locale.ROOT);

        String ruta =
                rutaCompleta.toLowerCase(Locale.ROOT);

        return nombre.equals(".ds_store")
                || nombre.startsWith("._")
                || ruta.contains("__macosx/");
    }

    private String normalizarNombre(String nombreArchivo) {
        return nombreArchivo
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}