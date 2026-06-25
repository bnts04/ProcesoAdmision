package com.admision.dto.area;

import com.admision.enums.ComponentePregunta;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenComponenteAreaResponse {

    private ComponentePregunta componente;
    private String nombreComponente;

    private Integer cantidadRequerida;
    private Long cantidadDisponible;
    private Boolean suficiente;
}