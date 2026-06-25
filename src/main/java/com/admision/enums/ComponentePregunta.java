package com.admision.enums;

public enum ComponentePregunta {
    CTA("Ciencia, Tecnología y Ambiente"),
    HUMANIDADES("Humanidades"),
    MATEMATICA("Matemática"),
    RAZONAMIENTO_VERBAL("Razonamiento Verbal"),
    RAZONAMIENTO_MATEMATICO("Razonamiento Matemático");

    private final String nombre;

    ComponentePregunta(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}