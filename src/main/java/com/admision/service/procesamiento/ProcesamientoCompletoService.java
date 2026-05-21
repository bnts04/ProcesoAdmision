package com.admision.service.procesamiento;

import com.admision.dto.*;
import com.admision.service.pdf.ActualizacionDesdePdfService;
import com.admision.service.validacion.ValidacionFuentesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesamientoCompletoService {

    private final ValidacionFuentesService validacionFuentesService;
    private final GuardarResultadosService guardarResultadosService;
    private final ActualizacionDesdePdfService actualizacionDesdePdfService;
    private final OrdenMeritoService ordenMeritoService;
    private final CondicionIngresoService condicionIngresoService;
    private final VistaPreviaFinalService vistaPreviaFinalService;

    public ProcesamientoCompletoResponse ejecutarTodo(Long procesoId) {

        List<String> pasosEjecutados = new ArrayList<>();
        List<String> observaciones = new ArrayList<>();

        /*
         * 1. Validar fuentes:
         *    - CLAVES.DBF
         *    - RESPUEST.DBF
         *    - IDENTIFI.DBF
         *    - PDF guía del proceso o PDF guía global
         */
        ValidacionFuentesResponse validacionFuentes =
                validacionFuentesService.validarFuentesYActualizarEstado(procesoId);

        pasosEjecutados.add("Fuentes validadas");

        if (!Boolean.TRUE.equals(validacionFuentes.getValido())) {
            observaciones.add("No se continuó el procesamiento porque existen fuentes con errores.");

            return ProcesamientoCompletoResponse.builder()
                    .procesoId(procesoId)
                    .ejecutadoCorrectamente(false)
                    .validacionFuentes(validacionFuentes)
                    .actualizacionDesdePdf(null)
                    .condicionIngreso(null)
                    .vistaPreviaFinal(null)
                    .pasosEjecutados(pasosEjecutados)
                    .observaciones(observaciones)
                    .mensaje("Procesamiento detenido. Revise la validación de fuentes.")
                    .build();
        }

        /*
         * 2. Guardar puntajes desde DBF.
         *    Usamos limite = 0 para procesar todos los registros.
         */
        guardarResultadosService.guardarResultadosCalculados(procesoId, 0);
        pasosEjecutados.add("Puntajes calculados y guardados desde DBF");

        /*
         * 3. Completar nombres, facultad y carrera desde PDF guía.
         *    Si el proceso actual no tiene PDF, usa el PDF guía global.
         */
        ActualizacionDesdePdfResponse actualizacionDesdePdf =
                actualizacionDesdePdfService.actualizarDatosDesdePdf(procesoId);

        pasosEjecutados.add("Datos completados desde PDF guía");

        if (actualizacionDesdePdf.getTotalNoEncontrados() > 0) {
            observaciones.add("Existen códigos no encontrados en PDF guía: "
                    + actualizacionDesdePdf.getTotalNoEncontrados());
        }

        /*
         * 4. Calcular orden de mérito.
         */
        ordenMeritoService.calcularOrdenMerito(procesoId);
        pasosEjecutados.add("Orden de mérito OME y OMG calculado");

        /*
         * 5. Calcular condición de ingreso.
         */
        CondicionIngresoResponse condicionIngreso =
                condicionIngresoService.calcularCondicionIngreso(procesoId);

        pasosEjecutados.add("Condición INGRESO / NO_INGRESO calculada");

        /*
         * 6. Generar vista previa final.
         */
        VistaPreviaFinalResponse vistaPreviaFinal =
                vistaPreviaFinalService.obtenerVistaPreviaFinal(procesoId);

        pasosEjecutados.add("Vista previa final generada");

        boolean correcto = vistaPreviaFinal.getObservacionesCriticas() == 0;

        if (!correcto) {
            observaciones.add("La vista previa final contiene observaciones críticas.");
        }

        return ProcesamientoCompletoResponse.builder()
                .procesoId(procesoId)
                .ejecutadoCorrectamente(correcto)
                .validacionFuentes(validacionFuentes)
                .actualizacionDesdePdf(actualizacionDesdePdf)
                .condicionIngreso(condicionIngreso)
                .vistaPreviaFinal(vistaPreviaFinal)
                .pasosEjecutados(pasosEjecutados)
                .observaciones(observaciones)
                .mensaje(correcto
                        ? "Procesamiento completo ejecutado correctamente. El proceso está listo para generar PDF."
                        : "Procesamiento completo ejecutado con observaciones. Revise la vista previa final.")
                .build();
    }
}