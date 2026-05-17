package com.admision.service.procesamiento;

import com.admision.dto.ResultadoPostulanteVistaResponse;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultadoConsultaService {

    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    public List<ResultadoPostulanteVistaResponse> listarResultadosPorProceso(Long procesoId, Integer limite) {
        if (!procesoAdmisionRepository.existsById(procesoId)) {
            throw new RuntimeException("Proceso de admisión no encontrado");
        }

        int maximo = limite != null && limite > 0 ? limite : Integer.MAX_VALUE;

        return resultadoPostulanteRepository.findByProcesoIdOrderByPuntajeFinalDesc(procesoId)
                .stream()
                .limit(maximo)
                .map(resultado -> ResultadoPostulanteVistaResponse.fromEntity(resultado, procesoId))
                .toList();
    }
}