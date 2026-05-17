package com.admision.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearCarreraVacanteRequest {

    @NotBlank(message = "La facultad es obligatoria")
    private String facultad;

    @NotBlank(message = "La carrera es obligatoria")
    private String carrera;

    @NotNull(message = "La cantidad de vacantes es obligatoria")
    @Min(value = 1, message = "La cantidad de vacantes debe ser mayor a 0")
    private Integer vacantes;
}