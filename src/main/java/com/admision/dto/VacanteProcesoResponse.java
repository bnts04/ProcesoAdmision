package com.admision.dto;

import com.admision.entity.VacanteProceso;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VacanteProcesoResponse {

    private Long id;
    private Long procesoId;
    private String facultad;
    private String carrera;
    private Integer vacantes;

    public static VacanteProcesoResponse fromEntity(VacanteProceso vacanteProceso) {
        return VacanteProcesoResponse.builder()
                .id(vacanteProceso.getId())
                .procesoId(vacanteProceso.getProceso().getId())
                .facultad(vacanteProceso.getFacultad())
                .carrera(vacanteProceso.getCarrera())
                .vacantes(vacanteProceso.getVacantes())
                .build();
    }
}