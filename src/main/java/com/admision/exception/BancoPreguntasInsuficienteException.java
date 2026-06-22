package com.admision.exception;

import com.admision.dto.examen.FaltanteSubcursoResponse;
import com.admision.enums.CodigoAreaExamen;

import java.util.List;

public class BancoPreguntasInsuficienteException extends RuntimeException {

    private final CodigoAreaExamen area;
    private final List<FaltanteSubcursoResponse> faltantes;

    public BancoPreguntasInsuficienteException(
            CodigoAreaExamen area,
            List<FaltanteSubcursoResponse> faltantes
    ) {
        super("El banco no tiene suficientes preguntas para generar el examen.");
        this.area = area;
        this.faltantes = faltantes;
    }

    public CodigoAreaExamen getArea() {
        return area;
    }

    public List<FaltanteSubcursoResponse> getFaltantes() {
        return faltantes;
    }
}