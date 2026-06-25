package com.admision.dto.area;

import com.admision.enums.CodigoAreaExamen;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaExamenResponse {

    private Long id;
    private CodigoAreaExamen codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}