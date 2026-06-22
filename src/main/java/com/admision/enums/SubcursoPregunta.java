package com.admision.enums;

public enum SubcursoPregunta {
    BIOLOGIA(ComponentePregunta.CTA, "Biología"),
    QUIMICA(ComponentePregunta.CTA, "Química"),
    FISICA(ComponentePregunta.CTA, "Física"),

    HISTORIA(ComponentePregunta.HUMANIDADES, "Historia"),
    GEOGRAFIA(ComponentePregunta.HUMANIDADES, "Geografía"),
    ECONOMIA(ComponentePregunta.HUMANIDADES, "Economía"),
    EDUCACION_CIVICA(ComponentePregunta.HUMANIDADES, "Educación Cívica"),
    PSICOLOGIA(ComponentePregunta.HUMANIDADES, "Psicología"),
    LENGUAJE(ComponentePregunta.HUMANIDADES, "Lenguaje"),
    LITERATURA(ComponentePregunta.HUMANIDADES, "Literatura"),

    TRIGONOMETRIA(ComponentePregunta.MATEMATICA, "Trigonometría"),
    GEOMETRIA(ComponentePregunta.MATEMATICA, "Geometría"),
    ALGEBRA(ComponentePregunta.MATEMATICA, "Álgebra"),
    ARITMETICA(ComponentePregunta.MATEMATICA, "Aritmética"),

    RAZONAMIENTO_VERBAL(ComponentePregunta.RAZONAMIENTO_VERBAL, "Razonamiento Verbal"),
    RAZONAMIENTO_MATEMATICO(ComponentePregunta.RAZONAMIENTO_MATEMATICO, "Razonamiento Matemático");

    private final ComponentePregunta componente;
    private final String nombre;

    SubcursoPregunta(ComponentePregunta componente, String nombre) {
        this.componente = componente;
        this.nombre = nombre;
    }

    public ComponentePregunta getComponente() {
        return componente;
    }

    public String getNombre() {
        return nombre;
    }
}