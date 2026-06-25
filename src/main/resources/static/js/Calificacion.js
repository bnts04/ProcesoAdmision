const CALIFICACION_DEFECTO = {
    puntajeCorrecta: 20.0000,
    puntajeIncorrecta: -1.2500,
    puntajeBlanca: 1.2500,
    factorEscala: 100.0000
};

function formatearNumeroCalificacion(valor) {
    const numero = Number(valor);

    if (Number.isNaN(numero)) {
        return "0.0000";
    }

    return numero.toFixed(4);
}

function setValorInputCalificacion(id, valor) {
    const input = document.getElementById(id);

    if (!input) return;

    input.value = formatearNumeroCalificacion(valor);
}

function obtenerNumeroInputCalificacion(id, defecto) {
    const input = document.getElementById(id);

    if (!input) {
        return defecto;
    }

    const valor = Number(input.value);

    if (Number.isNaN(valor)) {
        return defecto;
    }

    return valor;
}

function pintarConfiguracionEnFormulario(config) {
    const data = config || CALIFICACION_DEFECTO;

    setValorInputCalificacion(
        "input-puntaje-correcta",
        data.puntajeCorrecta ?? CALIFICACION_DEFECTO.puntajeCorrecta
    );

    setValorInputCalificacion(
        "input-puntaje-incorrecta",
        data.puntajeIncorrecta ?? CALIFICACION_DEFECTO.puntajeIncorrecta
    );

    setValorInputCalificacion(
        "input-puntaje-blanca",
        data.puntajeBlanca ?? CALIFICACION_DEFECTO.puntajeBlanca
    );

    setValorInputCalificacion(
        "input-factor-escala",
        data.factorEscala ?? CALIFICACION_DEFECTO.factorEscala
    );

    const texto = document.getElementById("texto-configuracion-calificacion");

    if (!texto) return;

    if (config) {
        texto.textContent =
            `Configuración cargada del proceso: correcta ${formatearNumeroCalificacion(data.puntajeCorrecta)}, ` +
            `incorrecta ${formatearNumeroCalificacion(data.puntajeIncorrecta)}, ` +
            `blanca/nula ${formatearNumeroCalificacion(data.puntajeBlanca)}, ` +
            `factor ${formatearNumeroCalificacion(data.factorEscala)}.`;
    } else {
        texto.textContent =
            "Configuración por defecto: correcta 20.00, incorrecta -1.25, blanca/nula 1.25, factor 100.";
    }
}

function obtenerValoresCalificacionFormulario() {
    const config = {
        puntajeCorrecta: obtenerNumeroInputCalificacion(
            "input-puntaje-correcta",
            CALIFICACION_DEFECTO.puntajeCorrecta
        ),
        puntajeIncorrecta: obtenerNumeroInputCalificacion(
            "input-puntaje-incorrecta",
            CALIFICACION_DEFECTO.puntajeIncorrecta
        ),
        puntajeBlanca: obtenerNumeroInputCalificacion(
            "input-puntaje-blanca",
            CALIFICACION_DEFECTO.puntajeBlanca
        ),
        factorEscala: obtenerNumeroInputCalificacion(
            "input-factor-escala",
            CALIFICACION_DEFECTO.factorEscala
        )
    };

    if (config.puntajeCorrecta <= 0) {
        throw new Error("El puntaje por respuesta correcta debe ser mayor a 0.");
    }

    if (config.factorEscala <= 0) {
        throw new Error("El factor de escala debe ser mayor a 0.");
    }

    return config;
}

function restaurarCalificacionDefecto() {
    pintarConfiguracionEnFormulario(CALIFICACION_DEFECTO);
}

async function cargarConfiguracionCalificacionProcesoActual(mostrarError = false) {
    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        pintarConfiguracionEnFormulario(CALIFICACION_DEFECTO);
        return;
    }

    try {
        const config = await fetchJson(`${API_BASE}/api/procesos/${procesoId}/configuracion-calificacion`);
        pintarConfiguracionEnFormulario(config);

    } catch (error) {
        console.error("No se pudo cargar configuración de calificación:", error);

        if (mostrarError) {
            alert("No se pudo cargar la configuración de calificación: " + error.message);
        }
    }
}

async function guardarConfiguracionCalificacionProcesoActual() {
    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        alert("Primero debes crear o seleccionar un proceso.");
        return;
    }

    try {
        const config = obtenerValoresCalificacionFormulario();

        const yaProcesado =
            window.estadoProcesoActual &&
            window.estadoProcesoActual.dbfProcesados === true;

        if (yaProcesado) {
            const confirmar = confirm(
                "Este proceso ya tiene resultados procesados. " +
                "Si cambias la calificación, debes volver a presionar 'Procesar todo' para recalcular los puntajes. ¿Deseas continuar?"
            );

            if (!confirmar) {
                return;
            }
        }

        const actualizado = await fetchJson(`${API_BASE}/api/procesos/${procesoId}/configuracion-calificacion`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(config)
        });

        pintarConfiguracionEnFormulario(actualizado);

        mostrarMensajeCarga(
            "Configuración de calificación guardada correctamente. Si el proceso ya estaba procesado, debe recalcularse.",
            "ok"
        );

        alert("Configuración de calificación guardada correctamente.");

    } catch (error) {
        console.error("Error guardando configuración de calificación:", error);
        alert("No se pudo guardar la configuración: " + error.message);
    }
}

window.addEventListener("load", async function () {
    pintarConfiguracionEnFormulario(CALIFICACION_DEFECTO);

    const procesoId = getProcesoIdActual();

    if (procesoId) {
        await cargarConfiguracionCalificacionProcesoActual(false);
    }
});