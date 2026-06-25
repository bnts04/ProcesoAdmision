package com.admision.controller;

import com.admision.dto.banco.ActualizarPreguntaRequest;
import com.admision.dto.banco.CrearPreguntaRequest;
import com.admision.dto.banco.PreguntaBancoResponse;
import com.admision.dto.banco.ResumenBancoPreguntasResponse;
import com.admision.enums.ComponentePregunta;
import com.admision.enums.SubcursoPregunta;
import com.admision.service.BancoPreguntasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.admision.dto.importacion.ResultadoImportacionBancoResponse;
import com.admision.service.ImportadorBancoPreguntasService;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banco-preguntas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BancoPreguntasController {

    private final BancoPreguntasService bancoPreguntasService;
    private final ImportadorBancoPreguntasService importadorBancoPreguntasService;

    @PostMapping
    public ResponseEntity<PreguntaBancoResponse> registrarPregunta(
            @RequestBody CrearPreguntaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bancoPreguntasService.registrarPregunta(request));
    }

    @PostMapping(value = "/con-imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PreguntaBancoResponse> registrarPreguntaConImagen(
            @ModelAttribute CrearPreguntaRequest request,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bancoPreguntasService.registrarPregunta(request, imagen));
    }

    @GetMapping
    public ResponseEntity<List<PreguntaBancoResponse>> listarPreguntas(
            @RequestParam(required = false) ComponentePregunta componente,
            @RequestParam(required = false) SubcursoPregunta subcurso
    ) {
        return ResponseEntity.ok(bancoPreguntasService.listarPreguntas(componente, subcurso));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PreguntaBancoResponse> obtenerPregunta(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(bancoPreguntasService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PreguntaBancoResponse> actualizarPregunta(
            @PathVariable Long id,
            @RequestBody ActualizarPreguntaRequest request
    ) {
        return ResponseEntity.ok(bancoPreguntasService.actualizarPregunta(id, request));
    }

    @PutMapping(value = "/{id}/con-imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PreguntaBancoResponse> actualizarPreguntaConImagen(
            @PathVariable Long id,
            @ModelAttribute ActualizarPreguntaRequest request,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen
    ) {
        return ResponseEntity.ok(bancoPreguntasService.actualizarPregunta(id, request, imagen));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> desactivarPregunta(
            @PathVariable Long id
    ) {
        bancoPreguntasService.desactivarPregunta(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mensaje", "Pregunta desactivada correctamente.");
        response.put("preguntaId", id);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<PreguntaBancoResponse> eliminarImagenPregunta(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(bancoPreguntasService.eliminarImagenPregunta(id));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenBancoPreguntasResponse> obtenerResumenBanco() {
        return ResponseEntity.ok(bancoPreguntasService.obtenerResumenBanco());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorValidacion(
            IllegalArgumentException ex
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", true);
        response.put("mensaje", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping(
            value = "/importar-inicial",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResultadoImportacionBancoResponse> importarBancoInicial(
            @RequestParam("archivoExcel")
            MultipartFile archivoExcel,

            @RequestParam(
                    value = "archivoImagenes",
                    required = false
            )
            MultipartFile archivoImagenes
    ) {
        ResultadoImportacionBancoResponse resultado =
                importadorBancoPreguntasService.importarBanco(
                        archivoExcel,
                        archivoImagenes
                );

        return ResponseEntity.ok(resultado);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGeneral(
            RuntimeException ex
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", true);
        response.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}