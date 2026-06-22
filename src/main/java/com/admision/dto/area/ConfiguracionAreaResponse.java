package com.admision.dto.area;

import com.admision.enums.ComponentePregunta;
import com.admision.enums.SubcursoPregunta;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionAreaResponse {

    private ComponentePregunta componente;
    private String nombreComponente;

    private SubcursoPregunta subcurso;
    private String nombreSubcurso;

    private Integer cantidadRequerida;
    private Long cantidadDisponible;
    private Boolean suficiente;
}