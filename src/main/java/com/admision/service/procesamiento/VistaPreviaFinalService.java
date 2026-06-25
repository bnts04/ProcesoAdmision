package com.admision.service.procesamiento;

import com.admision.dto.*;
import com.admision.entity.CarreraVacante;
import com.admision.entity.ProcesoAdmision;
import com.admision.entity.ResultadoPostulante;
import com.admision.enums.CondicionPostulante;
import com.admision.enums.TipoArchivo;
import com.admision.repository.CarreraVacanteRepository;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import com.admision.service.validacion.ValidacionFuentesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VistaPreviaFinalService {

    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final CarreraVacanteRepository carreraVacanteRepository;
    private final ValidacionFuentesService validacionFuentesService;
    private final DiagnosticoTemaService diagnosticoTemaService;

    public VistaPreviaFinalResponse obtenerVistaPreviaFinal(Long procesoId) {
        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId);
        List<CarreraVacante> vacantes = carreraVacanteRepository.findByActivoTrueOrderByFacultadAscCarreraAsc();

        ValidacionFuentesResponse validacionFuentes = validacionFuentesService.validarFuentes(procesoId);
        DiagnosticoTemaResponse diagnosticoTema = diagnosticoTemaService.diagnosticarTemas(procesoId);

        int totalPostulantes = resultados.size();

        int puntajesCalculados = (int) resultados.stream()
                .filter(r -> Boolean.TRUE.equals(r.getPuntajeCalculado()))
                .count();

        int puntajesNoCalculados = totalPostulantes - puntajesCalculados;

        int totalIngresantes = (int) resultados.stream()
                .filter(r -> r.getCondicion() == CondicionPostulante.INGRESO)
                .count();

        int totalNoIngresantes = (int) resultados.stream()
                .filter(r -> r.getCondicion() == CondicionPostulante.NO_INGRESO)
                .count();

        int totalPendientes = (int) resultados.stream()
                .filter(r -> r.getCondicion() == CondicionPostulante.PENDIENTE)
                .count();

        int totalCarreras = (int) resultados.stream()
                .map(r -> r.getCarrera() == null ? "" : r.getCarrera().trim().toUpperCase())
                .filter(c -> !c.isBlank() && !c.equals("PENDIENTE"))
                .distinct()
                .count();

        int totalVacantes = vacantes.stream()
                .mapToInt(CarreraVacante::getVacantes)
                .sum();

        int nombresCompletadosDesdePdf = (int) resultados.stream()
                .filter(r -> r.getApellidosNombres() != null)
                .filter(r -> !r.getApellidosNombres().isBlank())
                .filter(r -> !r.getApellidosNombres().equalsIgnoreCase("PENDIENTE"))
                .count();

        int nombresPendientes = totalPostulantes - nombresCompletadosDesdePdf;

        boolean pdfGuiaDisponible = validacionFuentes.getDetalles().stream()
                .filter(d -> d.getTipoArchivo().equals(TipoArchivo.PDF_RESULTADOS.name()))
                .anyMatch(d -> Boolean.TRUE.equals(d.getValido()));

        boolean pdfGuiaGlobalUtilizado = validacionFuentes.getDetalles().stream()
                .filter(d -> d.getTipoArchivo().equals(TipoArchivo.PDF_RESULTADOS.name()))
                .anyMatch(d -> Boolean.TRUE.equals(d.getPdfGlobalUtilizado()));

        boolean dbfProcesados = totalPostulantes > 0 && puntajesCalculados > 0;

        int observacionesCriticas = 0;
        List<String> observaciones = new ArrayList<>();

        if (Boolean.TRUE.equals(validacionFuentes.getValido())) {
            observaciones.add("Fuentes validadas correctamente.");
        } else {
            observacionesCriticas++;
            observaciones.add("Existen fuentes con errores u observaciones.");
        }

        if (dbfProcesados) {
            observaciones.add("Resultados DBF procesados correctamente.");
        } else {
            observacionesCriticas++;
            observaciones.add("Aún no existen resultados procesados desde DBF.");
        }

        if (pdfGuiaDisponible) {
            if (pdfGuiaGlobalUtilizado) {
                observaciones.add("PDF guía global disponible y reutilizado.");
            } else {
                observaciones.add("PDF guía del proceso disponible.");
            }
        } else {
            observacionesCriticas++;
            observaciones.add("No existe PDF guía disponible.");
        }

        if (diagnosticoTema.getTemaTomadoDesdeIdentifi() > 0) {
            observaciones.add("Se usó TEMA desde IDENTIFI en "
                    + diagnosticoTema.getTemaTomadoDesdeIdentifi()
                    + " registro(s).");
        }

        if (diagnosticoTema.getTemasInvalidos() > 0) {
            observacionesCriticas++;
            observaciones.add("Existen registros con TEMA inválido: "
                    + diagnosticoTema.getTemasInvalidos());
        }

        if (nombresPendientes > 0) {
            observacionesCriticas++;
            observaciones.add("Existen nombres pendientes por completar: " + nombresPendientes);
        } else if (totalPostulantes > 0) {
            observaciones.add("Nombres, facultades y carreras completados desde PDF guía.");
        }

        if (totalPendientes > 0) {
            observaciones.add("Existen postulantes con condición pendiente: " + totalPendientes);
        }

        String mensaje = observacionesCriticas == 0
                ? "Vista previa final correcta. El proceso está listo para generar PDF."
                : "Vista previa final generada con observaciones. Revisar antes de generar PDF.";

        return VistaPreviaFinalResponse.builder()
                .procesoId(proceso.getId())
                .nombreProceso(proceso.getNombreProceso())
                .modalidad(proceso.getModalidad())
                .estadoProceso(proceso.getEstado())
                .fuentesValidas(validacionFuentes.getValido())
                .dbfProcesados(dbfProcesados)
                .pdfGuiaDisponible(pdfGuiaDisponible)
                .pdfGuiaGlobalUtilizado(pdfGuiaGlobalUtilizado)
                .totalPostulantes(totalPostulantes)
                .puntajesCalculados(puntajesCalculados)
                .puntajesNoCalculados(puntajesNoCalculados)
                .totalCarreras(totalCarreras)
                .totalVacantes(totalVacantes)
                .totalIngresantes(totalIngresantes)
                .totalNoIngresantes(totalNoIngresantes)
                .totalPendientes(totalPendientes)
                .nombresCompletadosDesdePdf(nombresCompletadosDesdePdf)
                .nombresPendientes(nombresPendientes)
                .temasValidos(diagnosticoTema.getTemasValidos())
                .temasInvalidos(diagnosticoTema.getTemasInvalidos())
                .temasTomadosDesdeIdentifi(diagnosticoTema.getTemaTomadoDesdeIdentifi())
                .observacionesCriticas(observacionesCriticas)
                .observaciones(observaciones)
                .mensaje(mensaje)
                .build();
    }
}