package com.admision.dto;

import com.admision.entity.CarreraVacante;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CarreraVacanteResponse {

    private Long id;
    private String facultad;
    private String carrera;
    private Integer vacantes;
    private Boolean activo;
    private LocalDateTime fechaRegistro;

    public static CarreraVacanteResponse fromEntity(CarreraVacante carreraVacante) {
        return CarreraVacanteResponse.builder()
                .id(carreraVacante.getId())
                .facultad(carreraVacante.getFacultad())
                .carrera(carreraVacante.getCarrera())
                .vacantes(carreraVacante.getVacantes())
                .activo(carreraVacante.getActivo())
                .fechaRegistro(carreraVacante.getFechaRegistro())
                .build();
    }
}