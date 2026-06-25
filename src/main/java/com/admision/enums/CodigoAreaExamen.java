package com.admision.enums;

public enum CodigoAreaExamen {
    AREA_A("Área A", "Ciencias de la Salud"),
    AREA_B("Área B", "Ciencias Sociales y Humanidades"),
    AREA_C("Área C", "Ciencias e Ingeniería");

    private final String nombre;
    private final String descripcion;

    CodigoAreaExamen(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}