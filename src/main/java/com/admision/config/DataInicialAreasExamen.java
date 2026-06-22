package com.admision.config;

import com.admision.entity.AreaExamen;
import com.admision.entity.ConfiguracionAreaExamen;
import com.admision.enums.CodigoAreaExamen;
import com.admision.enums.ComponentePregunta;
import com.admision.enums.SubcursoPregunta;
import com.admision.repository.AreaExamenRepository;
import com.admision.repository.ConfiguracionAreaExamenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInicialAreasExamen implements CommandLineRunner {

    private final AreaExamenRepository areaExamenRepository;
    private final ConfiguracionAreaExamenRepository configuracionAreaExamenRepository;

    @Override
    @Transactional
    public void run(String... args) {
        crearAreasSiNoExisten();
        crearConfiguracionAreaA();
        crearConfiguracionAreaB();
        crearConfiguracionAreaC();
    }

    private void crearAreasSiNoExisten() {
        for (CodigoAreaExamen codigo : CodigoAreaExamen.values()) {
            if (!areaExamenRepository.existsByCodigo(codigo)) {
                AreaExamen area = AreaExamen.builder()
                        .codigo(codigo)
                        .nombre(codigo.getNombre())
                        .descripcion(codigo.getDescripcion())
                        .activo(true)
                        .build();

                areaExamenRepository.save(area);
            }
        }
    }

    private void crearConfiguracionAreaA() {
        AreaExamen area = obtenerArea(CodigoAreaExamen.AREA_A);

        if (configuracionAreaExamenRepository.countByArea(area) > 0) {
            return;
        }

        List<ConfiguracionAreaExamen> configuraciones = new ArrayList<>();

        agregar(configuraciones, area, ComponentePregunta.CTA, SubcursoPregunta.BIOLOGIA, 17);
        agregar(configuraciones, area, ComponentePregunta.CTA, SubcursoPregunta.QUIMICA, 8);
        agregar(configuraciones, area, ComponentePregunta.CTA, SubcursoPregunta.FISICA, 5);

        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.HISTORIA, 2);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.GEOGRAFIA, 2);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.ECONOMIA, 1);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.EDUCACION_CIVICA, 2);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.PSICOLOGIA, 2);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.LENGUAJE, 3);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.LITERATURA, 3);

        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.TRIGONOMETRIA, 3);
        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.GEOMETRIA, 4);
        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.ALGEBRA, 4);
        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.ARITMETICA, 4);

        agregar(configuraciones, area, ComponentePregunta.RAZONAMIENTO_VERBAL, SubcursoPregunta.RAZONAMIENTO_VERBAL, 20);
        agregar(configuraciones, area, ComponentePregunta.RAZONAMIENTO_MATEMATICO, SubcursoPregunta.RAZONAMIENTO_MATEMATICO, 20);

        configuracionAreaExamenRepository.saveAll(configuraciones);
    }

    private void crearConfiguracionAreaB() {
        AreaExamen area = obtenerArea(CodigoAreaExamen.AREA_B);

        if (configuracionAreaExamenRepository.countByArea(area) > 0) {
            return;
        }

        List<ConfiguracionAreaExamen> configuraciones = new ArrayList<>();

        agregar(configuraciones, area, ComponentePregunta.CTA, SubcursoPregunta.BIOLOGIA, 8);
        agregar(configuraciones, area, ComponentePregunta.CTA, SubcursoPregunta.QUIMICA, 4);
        agregar(configuraciones, area, ComponentePregunta.CTA, SubcursoPregunta.FISICA, 3);

        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.HISTORIA, 4);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.GEOGRAFIA, 3);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.ECONOMIA, 3);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.EDUCACION_CIVICA, 4);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.PSICOLOGIA, 4);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.LENGUAJE, 6);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.LITERATURA, 6);

        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.TRIGONOMETRIA, 3);
        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.GEOMETRIA, 4);
        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.ALGEBRA, 4);
        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.ARITMETICA, 4);

        agregar(configuraciones, area, ComponentePregunta.RAZONAMIENTO_VERBAL, SubcursoPregunta.RAZONAMIENTO_VERBAL, 20);
        agregar(configuraciones, area, ComponentePregunta.RAZONAMIENTO_MATEMATICO, SubcursoPregunta.RAZONAMIENTO_MATEMATICO, 20);

        configuracionAreaExamenRepository.saveAll(configuraciones);
    }

    private void crearConfiguracionAreaC() {
        AreaExamen area = obtenerArea(CodigoAreaExamen.AREA_C);

        if (configuracionAreaExamenRepository.countByArea(area) > 0) {
            return;
        }

        List<ConfiguracionAreaExamen> configuraciones = new ArrayList<>();

        agregar(configuraciones, area, ComponentePregunta.CTA, SubcursoPregunta.BIOLOGIA, 2);
        agregar(configuraciones, area, ComponentePregunta.CTA, SubcursoPregunta.QUIMICA, 5);
        agregar(configuraciones, area, ComponentePregunta.CTA, SubcursoPregunta.FISICA, 8);

        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.HISTORIA, 2);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.GEOGRAFIA, 2);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.ECONOMIA, 1);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.EDUCACION_CIVICA, 2);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.PSICOLOGIA, 2);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.LENGUAJE, 3);
        agregar(configuraciones, area, ComponentePregunta.HUMANIDADES, SubcursoPregunta.LITERATURA, 3);

        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.TRIGONOMETRIA, 7);
        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.GEOMETRIA, 8);
        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.ALGEBRA, 7);
        agregar(configuraciones, area, ComponentePregunta.MATEMATICA, SubcursoPregunta.ARITMETICA, 8);

        agregar(configuraciones, area, ComponentePregunta.RAZONAMIENTO_VERBAL, SubcursoPregunta.RAZONAMIENTO_VERBAL, 20);
        agregar(configuraciones, area, ComponentePregunta.RAZONAMIENTO_MATEMATICO, SubcursoPregunta.RAZONAMIENTO_MATEMATICO, 20);

        configuracionAreaExamenRepository.saveAll(configuraciones);
    }

    private AreaExamen obtenerArea(CodigoAreaExamen codigo) {
        return areaExamenRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalStateException("No existe el área: " + codigo));
    }

    private void agregar(
            List<ConfiguracionAreaExamen> lista,
            AreaExamen area,
            ComponentePregunta componente,
            SubcursoPregunta subcurso,
            Integer cantidad
    ) {
        lista.add(ConfiguracionAreaExamen.builder()
                .area(area)
                .componente(componente)
                .subcurso(subcurso)
                .cantidadPreguntas(cantidad)
                .build());
    }
}