package com.admision.service.reporte;

import com.admision.enums.TipoArchivo;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import com.admision.dto.DiagnosticoTemaResponse;
import com.admision.dto.ReporteTecnicoResponse;
import com.admision.entity.ArchivoCargado;
import com.admision.entity.CarreraVacante;
import com.admision.entity.ProcesoAdmision;
import com.admision.entity.ResultadoPostulante;
import com.admision.enums.CondicionPostulante;
import com.admision.repository.ArchivoCargadoRepository;
import com.admision.repository.CarreraVacanteRepository;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import com.admision.service.procesamiento.DiagnosticoTemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteTecnicoService {

    private final ProcesoAdmisionRepository procesoAdmisionRepository;
    private final ArchivoCargadoRepository archivoCargadoRepository;
    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final CarreraVacanteRepository carreraVacanteRepository;
    private final DiagnosticoTemaService diagnosticoTemaService;

    public ReporteTecnicoResponse generarReporteTecnico(Long procesoId) {
        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        List<ArchivoCargado> archivos = archivoCargadoRepository.findByProcesoId(procesoId);
        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId);
        List<CarreraVacante> vacantes = carreraVacanteRepository.findByActivoTrueOrderByFacultadAscCarreraAsc();

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

        int registrosConCarreraPendiente = (int) resultados.stream()
                .filter(r -> r.getCarrera() == null
                        || r.getCarrera().isBlank()
                        || r.getCarrera().equalsIgnoreCase("PENDIENTE"))
                .count();

        int registrosConObservaciones = (int) resultados.stream()
                .filter(r -> r.getObservacion() != null && !r.getObservacion().isBlank())
                .count();

        int totalVacantesConfiguradas = vacantes.stream()
                .mapToInt(CarreraVacante::getVacantes)
                .sum();

        int totalCarrerasDetectadas = (int) resultados.stream()
                .map(r -> r.getCarrera() == null ? "" : r.getCarrera().trim().toUpperCase())
                .filter(c -> !c.isBlank() && !c.equals("PENDIENTE"))
                .distinct()
                .count();

        Map<TipoArchivo, ArchivoCargado> archivosActivosPorTipo = archivos.stream()
                .collect(Collectors.toMap(
                        ArchivoCargado::getTipoArchivo,
                        a -> a,
                        (a1, a2) -> a1.getFechaCarga().isAfter(a2.getFechaCarga()) ? a1 : a2
                ));

        List<String> archivosCargados = archivosActivosPorTipo.values()
                .stream()
                .sorted(Comparator.comparing(a -> a.getTipoArchivo().name()))
                .map(a -> a.getTipoArchivo()
                        + " | "
                        + a.getNombreOriginal()
                        + " | ACTIVO")
                .toList();

        List<String> observaciones = new ArrayList<>();

        observaciones.add("Archivos activos utilizados: " + archivosActivosPorTipo.size());
        observaciones.add("Postulantes procesados: " + totalPostulantes);
        observaciones.add("Puntajes calculados correctamente: " + puntajesCalculados);

        if (puntajesNoCalculados > 0) {
            observaciones.add("Existen " + puntajesNoCalculados + " registros sin puntaje calculado.");
        }

        observaciones.add("Diagnóstico TEMA: "
                + diagnosticoTema.getTemasValidos()
                + " válidos, "
                + diagnosticoTema.getTemasInvalidos()
                + " inválidos.");

        if (diagnosticoTema.getTemaTomadoDesdeIdentifi() > 0) {
            observaciones.add("Se usó TEMA desde IDENTIFI en "
                    + diagnosticoTema.getTemaTomadoDesdeIdentifi()
                    + " registro(s).");
        }

        if (diagnosticoTema.getConflictosTema() > 0) {
            observaciones.add("Existen conflictos de TEMA: " + diagnosticoTema.getConflictosTema());
        }

        if (registrosConCarreraPendiente > 0) {
            observaciones.add("Existen registros con carrera pendiente: " + registrosConCarreraPendiente);
        } else {
            observaciones.add("Carrera y facultad asignadas correctamente.");
        }

        observaciones.add("Vacantes configuradas: " + totalVacantesConfiguradas);
        observaciones.add("Ingresantes: " + totalIngresantes);
        observaciones.add("No ingresantes: " + totalNoIngresantes);

        String mensaje = puntajesNoCalculados == 0
                && registrosConCarreraPendiente == 0
                && diagnosticoTema.getTemasInvalidos() == 0
                ? "Reporte técnico generado correctamente. El proceso no presenta errores críticos."
                : "Reporte técnico generado con observaciones. Revisar detalles.";

        return ReporteTecnicoResponse.builder()
                .procesoId(proceso.getId())
                .nombreProceso(proceso.getNombreProceso())
                .modalidad(proceso.getModalidad())
                .estadoProceso(proceso.getEstado())
                .totalArchivosCargados(archivosActivosPorTipo.size())
                .archivosCargados(archivosCargados)
                .totalPostulantes(totalPostulantes)
                .puntajesCalculados(puntajesCalculados)
                .puntajesNoCalculados(puntajesNoCalculados)
                .totalIngresantes(totalIngresantes)
                .totalNoIngresantes(totalNoIngresantes)
                .totalPendientes(totalPendientes)
                .totalCarrerasDetectadas(totalCarrerasDetectadas)
                .totalCarrerasConVacantes(vacantes.size())
                .totalVacantesConfiguradas(totalVacantesConfiguradas)
                .registrosConCarreraPendiente(registrosConCarreraPendiente)
                .registrosConObservaciones(registrosConObservaciones)
                .observaciones(observaciones)
                .mensaje(mensaje)
                .build();
    }
}