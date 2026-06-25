package com.admision.service;

import com.admision.dto.importacion.FilaPreguntaImportacion;
import com.admision.entity.AlternativaPregunta;
import com.admision.entity.PreguntaBanco;
import com.admision.enums.ComponentePregunta;
import com.admision.enums.EstadoPregunta;
import com.admision.enums.LetraAlternativa;
import com.admision.enums.SubcursoPregunta;
import com.admision.exception.PreguntaDuplicadaImportacionException;
import com.admision.repository.PreguntaBancoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImportacionFilaPreguntaService {

    private final PreguntaBancoRepository preguntaBancoRepository;
    private final ArchivoPreguntaService archivoPreguntaService;

    /**
     * Cada fila se procesa en una transacción independiente.
     *
     * Si una pregunta falla, únicamente se revierte esa pregunta
     * y la importación puede continuar con la siguiente fila.
     *
     * @return true cuando la pregunta fue importada con imagen.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean importarFila(
            FilaPreguntaImportacion fila,
            byte[] contenidoImagen
    ) {
        validarFilaBasica(fila);

        ComponentePregunta componente =
                convertirComponente(fila.getComponente());

        SubcursoPregunta subcurso =
                convertirSubcurso(fila.getSubcurso());

        validarRelacionComponenteSubcurso(
                componente,
                subcurso
        );

        LetraAlternativa respuestaCorrecta =
                convertirRespuestaCorrecta(
                        fila.getRespuestaCorrecta()
                );

        validarAlternativas(
                fila.getAlternativaA(),
                fila.getAlternativaB(),
                fila.getAlternativaC(),
                fila.getAlternativaD(),
                fila.getAlternativaE()
        );

        String enunciado = fila.getEnunciado().trim();

        if (preguntaBancoRepository
                .existsByEnunciadoNormalizado(enunciado)) {

            throw new PreguntaDuplicadaImportacionException(
                    "Ya existe una pregunta con el mismo enunciado."
            );
        }

        boolean tieneNombreImagen =
                !esVacio(fila.getNombreImagen());

        boolean tieneContenidoImagen =
                contenidoImagen != null
                        && contenidoImagen.length > 0;

        if (tieneNombreImagen && !tieneContenidoImagen) {
            throw new IllegalArgumentException(
                    "La pregunta indica una imagen, pero no se recibió su contenido: "
                            + fila.getNombreImagen()
            );
        }

        PreguntaBanco pregunta = PreguntaBanco.builder()
                .componente(componente)
                .subcurso(subcurso)
                .enunciado(enunciado)
                .observacion(
                        limpiarTextoOpcional(
                                fila.getObservacion()
                        )
                )
                .estado(EstadoPregunta.ACTIVA)
                .build();

        pregunta.setAlternativas(
                crearAlternativas(
                        pregunta,
                        fila,
                        respuestaCorrecta
                )
        );

        /*
         * Primer guardado para obtener el ID autogenerado.
         */
        PreguntaBanco guardada =
                preguntaBancoRepository.saveAndFlush(pregunta);

        guardada.setCodigo(
                generarCodigo(guardada.getId())
        );

        String imagenUrlGuardada = null;

        try {
            if (tieneContenidoImagen) {
                imagenUrlGuardada =
                        archivoPreguntaService
                                .guardarImagenPreguntaDesdeBytes(
                                        guardada.getCodigo(),
                                        fila.getNombreImagen(),
                                        contenidoImagen
                                );

                guardada.setImagenUrl(imagenUrlGuardada);
            }

            preguntaBancoRepository.saveAndFlush(guardada);

            return tieneContenidoImagen;

        } catch (RuntimeException ex) {
            /*
             * Si el archivo físico llegó a guardarse pero después
             * ocurrió un error en PostgreSQL, se elimina la imagen
             * para evitar archivos huérfanos.
             */
            if (imagenUrlGuardada != null) {
                archivoPreguntaService.eliminarImagenPregunta(
                        imagenUrlGuardada
                );
            }

            throw ex;
        }
    }

    private void validarFilaBasica(
            FilaPreguntaImportacion fila
    ) {
        if (fila == null) {
            throw new IllegalArgumentException(
                    "La fila de importación no puede ser nula."
            );
        }

        if (esVacio(fila.getComponente())) {
            throw new IllegalArgumentException(
                    "El componente es obligatorio."
            );
        }

        if (esVacio(fila.getSubcurso())) {
            throw new IllegalArgumentException(
                    "El subcurso es obligatorio."
            );
        }

        if (esVacio(fila.getEnunciado())) {
            throw new IllegalArgumentException(
                    "El enunciado es obligatorio."
            );
        }

        if (esVacio(fila.getRespuestaCorrecta())) {
            throw new IllegalArgumentException(
                    "La respuesta correcta es obligatoria."
            );
        }
    }

    private void validarAlternativas(
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD,
            String alternativaE
    ) {
        if (esVacio(alternativaA)
                || esVacio(alternativaB)
                || esVacio(alternativaC)
                || esVacio(alternativaD)
                || esVacio(alternativaE)) {

            throw new IllegalArgumentException(
                    "La pregunta debe contener las cinco alternativas: A, B, C, D y E."
            );
        }

        /*
         * También evita alternativas exactamente repetidas.
         */
        Set<String> alternativasNormalizadas =
                new HashSet<>();

        alternativasNormalizadas.add(
                normalizarTextoComparacion(alternativaA)
        );
        alternativasNormalizadas.add(
                normalizarTextoComparacion(alternativaB)
        );
        alternativasNormalizadas.add(
                normalizarTextoComparacion(alternativaC)
        );
        alternativasNormalizadas.add(
                normalizarTextoComparacion(alternativaD)
        );
        alternativasNormalizadas.add(
                normalizarTextoComparacion(alternativaE)
        );

        if (alternativasNormalizadas.size() != 5) {
            throw new IllegalArgumentException(
                    "La pregunta contiene alternativas duplicadas."
            );
        }
    }

    private List<AlternativaPregunta> crearAlternativas(
            PreguntaBanco pregunta,
            FilaPreguntaImportacion fila,
            LetraAlternativa respuestaCorrecta
    ) {
        List<AlternativaPregunta> alternativas =
                new ArrayList<>();

        alternativas.add(
                crearAlternativa(
                        pregunta,
                        LetraAlternativa.A,
                        fila.getAlternativaA(),
                        respuestaCorrecta
                )
        );

        alternativas.add(
                crearAlternativa(
                        pregunta,
                        LetraAlternativa.B,
                        fila.getAlternativaB(),
                        respuestaCorrecta
                )
        );

        alternativas.add(
                crearAlternativa(
                        pregunta,
                        LetraAlternativa.C,
                        fila.getAlternativaC(),
                        respuestaCorrecta
                )
        );

        alternativas.add(
                crearAlternativa(
                        pregunta,
                        LetraAlternativa.D,
                        fila.getAlternativaD(),
                        respuestaCorrecta
                )
        );

        alternativas.add(
                crearAlternativa(
                        pregunta,
                        LetraAlternativa.E,
                        fila.getAlternativaE(),
                        respuestaCorrecta
                )
        );

        return alternativas;
    }

    private AlternativaPregunta crearAlternativa(
            PreguntaBanco pregunta,
            LetraAlternativa letra,
            String texto,
            LetraAlternativa respuestaCorrecta
    ) {
        return AlternativaPregunta.builder()
                .pregunta(pregunta)
                .letraOriginal(letra)
                .texto(texto.trim())
                .esCorrecta(letra == respuestaCorrecta)
                .build();
    }

    private ComponentePregunta convertirComponente(
            String valor
    ) {
        String clave = normalizarClaveEnum(valor);

        return switch (clave) {
            case "CTA",
                 "CIENCIA_TECNOLOGIA_Y_AMBIENTE",
                 "CIENCIAS_NATURALES" ->
                    ComponentePregunta.CTA;

            case "HUMANIDADES" ->
                    ComponentePregunta.HUMANIDADES;

            case "MATEMATICA" ->
                    ComponentePregunta.MATEMATICA;

            case "RAZONAMIENTO_VERBAL",
                 "RV" ->
                    ComponentePregunta.RAZONAMIENTO_VERBAL;

            case "RAZONAMIENTO_MATEMATICO",
                 "RM" ->
                    ComponentePregunta.RAZONAMIENTO_MATEMATICO;

            default -> throw new IllegalArgumentException(
                    "Componente no reconocido: " + valor
            );
        };
    }

    private SubcursoPregunta convertirSubcurso(
            String valor
    ) {
        String clave = normalizarClaveEnum(valor);

        return switch (clave) {
            case "BIOLOGIA" ->
                    SubcursoPregunta.BIOLOGIA;

            case "QUIMICA" ->
                    SubcursoPregunta.QUIMICA;

            case "FISICA" ->
                    SubcursoPregunta.FISICA;

            case "HISTORIA" ->
                    SubcursoPregunta.HISTORIA;

            case "GEOGRAFIA" ->
                    SubcursoPregunta.GEOGRAFIA;

            case "ECONOMIA" ->
                    SubcursoPregunta.ECONOMIA;

            case "EDUCACION_CIVICA",
                 "CIVICA" ->
                    SubcursoPregunta.EDUCACION_CIVICA;

            case "PSICOLOGIA" ->
                    SubcursoPregunta.PSICOLOGIA;

            case "LENGUAJE" ->
                    SubcursoPregunta.LENGUAJE;

            case "LITERATURA" ->
                    SubcursoPregunta.LITERATURA;

            case "TRIGONOMETRIA" ->
                    SubcursoPregunta.TRIGONOMETRIA;

            case "GEOMETRIA" ->
                    SubcursoPregunta.GEOMETRIA;

            case "ALGEBRA" ->
                    SubcursoPregunta.ALGEBRA;

            case "ARITMETICA" ->
                    SubcursoPregunta.ARITMETICA;

            case "RAZONAMIENTO_VERBAL",
                 "RV" ->
                    SubcursoPregunta.RAZONAMIENTO_VERBAL;

            case "RAZONAMIENTO_MATEMATICO",
                 "RM" ->
                    SubcursoPregunta.RAZONAMIENTO_MATEMATICO;

            default -> throw new IllegalArgumentException(
                    "Subcurso no reconocido: " + valor
            );
        };
    }

    private LetraAlternativa convertirRespuestaCorrecta(
            String valor
    ) {
        String clave =
                valor.trim().toUpperCase(Locale.ROOT);

        try {
            return LetraAlternativa.valueOf(clave);

        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "La respuesta correcta debe ser A, B, C, D o E."
            );
        }
    }

    private void validarRelacionComponenteSubcurso(
            ComponentePregunta componente,
            SubcursoPregunta subcurso
    ) {
        if (subcurso.getComponente() != componente) {
            throw new IllegalArgumentException(
                    "El subcurso "
                            + subcurso.getNombre()
                            + " no pertenece al componente "
                            + componente.getNombre()
                            + "."
            );
        }
    }

    private String generarCodigo(Long id) {
        return String.format("P%06d", id);
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String limpiarTextoOpcional(String valor) {
        if (esVacio(valor)) {
            return null;
        }

        return valor.trim();
    }

    private String normalizarTextoComparacion(
            String valor
    ) {
        if (valor == null) {
            return "";
        }

        return valor
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Ejemplos:
     *
     * "Matemática"              -> MATEMATICA
     * "Razonamiento Verbal"     -> RAZONAMIENTO_VERBAL
     * "Educación Cívica"        -> EDUCACION_CIVICA
     */
    private String normalizarClaveEnum(String valor) {
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
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}