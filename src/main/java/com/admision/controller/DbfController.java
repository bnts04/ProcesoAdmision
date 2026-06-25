package com.admision.controller;

import com.admision.dto.DbfLecturaResponse;
import com.admision.enums.TipoArchivo;
import com.admision.service.dbf.DbfLecturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dbf")
@RequiredArgsConstructor
public class DbfController {

    private final DbfLecturaService dbfLecturaService;

    @GetMapping("/leer/{procesoId}")
    public DbfLecturaResponse leerDbf(
            @PathVariable Long procesoId,
            @RequestParam TipoArchivo tipoArchivo,
            @RequestParam(defaultValue = "10") Integer limite
    ) {
        return dbfLecturaService.leerDbf(procesoId, tipoArchivo, limite);
    }
}