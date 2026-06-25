package com.admision.service.examen;

import com.admision.dto.vistaprevia.VistaPreviaAlternativaResponse;
import com.admision.dto.vistaprevia.VistaPreviaExamenResponse;
import com.admision.dto.vistaprevia.VistaPreviaPreguntaResponse;
import com.admision.entity.ExamenGenerado;
import com.admision.entity.TemaExamen;
import com.admision.entity.TemaAlternativa;
import com.admision.entity.TemaPregunta;
import com.admision.repository.ExamenGeneradoRepository;
import com.admision.repository.TemaAlternativaRepository;
import com.admision.repository.TemaExamenRepository;
import com.admision.repository.TemaPreguntaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VistaPreviaExamenService {

        private final ExamenGeneradoRepository examenGeneradoRepository;
        private final TemaExamenRepository temaExamenRepository;
        private final TemaPreguntaRepository temaPreguntaRepository;
        private final TemaAlternativaRepository temaAlternativaRepository;

        @Transactional(readOnly = true)
        public VistaPreviaExamenResponse obtenerVistaPreviaTema(Long examenId, String letraTema) {
                ExamenGenerado examen = obtenerExamen(examenId);
                TemaExamen tema = obtenerTema(examen, letraTema);
                List<VistaPreviaPreguntaResponse> preguntas = construirPreguntas(tema);
                return VistaPreviaExamenResponse.builder()
                                .examenId(examen.getId())
                                .nombreExamen(examen.getNombreExamen())
                                .area(examen.getArea().getCodigo())
                                .nombreArea(examen.getArea().getNombre())
                                .letraTema(tema.getLetraTema())
                                .totalPreguntas(tema.getTotalPreguntas())
                                .fechaGeneracion(tema.getFechaGeneracion())
                                .preguntas(preguntas)
                                .build();
        }

        @Transactional(readOnly = true)
        public List<VistaPreviaExamenResponse> obtenerVistaPreviaCompleta(Long examenId) {
                ExamenGenerado examen = obtenerExamen(examenId);
                List<TemaExamen> temas = temaExamenRepository.findByExamenOrderByLetraTemaAsc(examen);
                if (temas.isEmpty()) {
                        throw new IllegalStateException("El examen con ID " + examenId + " no tiene temas generados.");
                }
                return temas.stream()
                                .map(tema -> {
                                        List<VistaPreviaPreguntaResponse> preguntas = construirPreguntas(tema);
                                        return VistaPreviaExamenResponse.builder()
                                                        .examenId(examen.getId())
                                                        .nombreExamen(examen.getNombreExamen())
                                                        .area(examen.getArea().getCodigo())
                                                        .nombreArea(examen.getArea().getNombre())
                                                        .letraTema(tema.getLetraTema())
                                                        .totalPreguntas(tema.getTotalPreguntas())
                                                        .fechaGeneracion(tema.getFechaGeneracion())
                                                        .preguntas(preguntas)
                                                        .build();
                                })
                                .toList();
        }

        private List<VistaPreviaPreguntaResponse> construirPreguntas(TemaExamen tema) {
                return temaPreguntaRepository.findByTemaOrderByNumeroPreguntaAsc(tema)
                                .stream()
                                .map(pregunta -> {
                                        List<VistaPreviaAlternativaResponse> alternativas = temaAlternativaRepository
                                                        .findByTemaPreguntaOrderByLetraFinalAsc(pregunta)
                                                        .stream()
                                                        .map(alt -> VistaPreviaAlternativaResponse.builder()
                                                                        .letra(alt.getLetraFinal())
                                                                        .texto(alt.getTexto())
                                                                        .build())
                                                        .toList();
                                        return VistaPreviaPreguntaResponse.builder()
                                                        .numeroPregunta(pregunta.getNumeroPregunta())
                                                        .codigoPregunta(pregunta.getCodigoPregunta())
                                                        .componente(pregunta.getComponente())
                                                        .nombreComponente(pregunta.getComponente().getNombre())
                                                        .subcurso(pregunta.getSubcurso())
                                                        .nombreSubcurso(pregunta.getSubcurso().getNombre())
                                                        .enunciado(pregunta.getEnunciado())
                                                        .imagenUrl(pregunta.getImagenUrl())
                                                        .respuestaCorrectaFinal(pregunta.getRespuestaCorrectaFinal())
                                                        .alternativas(alternativas)
                                                        .build();
                                })
                                .toList();
        }

        private ExamenGenerado obtenerExamen(Long examenId) {
                return examenGeneradoRepository.findById(examenId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Examen no encontrado con ID: " + examenId));
        }

        private TemaExamen obtenerTema(ExamenGenerado examen, String letraTema) {
                if (letraTema == null || letraTema.trim().isEmpty()) {
                        throw new IllegalArgumentException("Debe indicar la letra del tema.");
                }
                return temaExamenRepository.findByExamenAndLetraTemaIgnoreCase(examen, letraTema.trim())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No existe el tema " + letraTema + " para el examen indicado."));
        }
}
