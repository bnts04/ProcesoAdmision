let vacantesProcesoCache = [];

function escapeHtmlVacantes(valor) {
    if (valor === null || valor === undefined) {
        return "";
    }

    return String(valor)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function setBotonesVacantesProceso(habilitado) {
    const btnActualizar = document.getElementById("btn-actualizar-vacantes-proceso");
    const btnGuardar = document.getElementById("btn-guardar-vacantes-proceso");

    [btnActualizar, btnGuardar].forEach(btn => {
        if (!btn) return;

        btn.disabled = !habilitado;

        if (habilitado) {
            btn.classList.remove("opacity-50", "cursor-not-allowed");
            btn.classList.add("cursor-pointer");
        } else {
            btn.classList.add("opacity-50", "cursor-not-allowed");
            btn.classList.remove("cursor-pointer");
        }
    });
}

function limpiarVacantesProcesoUI(mensaje = "Seleccione o cree un proceso para cargar vacantes.") {
    vacantesProcesoCache = [];

    const tbody = document.getElementById("tabla-vacantes-proceso-body");
    const cantidadCarreras = document.getElementById("cantidad-carreras-vacantes");
    const totalVacantes = document.getElementById("total-vacantes-proceso");
    const estado = document.getElementById("estado-vacantes-proceso");
    const texto = document.getElementById("texto-vacantes-proceso");

    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" class="p-6 text-center text-gray-400">
                    ${escapeHtmlVacantes(mensaje)}
                </td>
            </tr>
        `;
    }

    if (cantidadCarreras) cantidadCarreras.textContent = "0";
    if (totalVacantes) totalVacantes.textContent = "0";
    if (estado) estado.textContent = "Pendiente";

    if (texto) {
        texto.textContent = "Las vacantes se cargan cuando existe un proceso activo. Puede editarlas antes de procesar.";
    }

    setBotonesVacantesProceso(false);
}

async function cargarVacantesProcesoActual(mostrarMensaje = false) {
    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        limpiarVacantesProcesoUI("Seleccione o cree un proceso para cargar vacantes.");
        return;
    }

    const tbody = document.getElementById("tabla-vacantes-proceso-body");

    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" class="p-6 text-center text-gray-400">
                    Cargando vacantes del proceso...
                </td>
            </tr>
        `;
    }

    try {
        const vacantes = await fetchJson(`${API_BASE}/api/procesos/${procesoId}/vacantes`);

        vacantesProcesoCache = Array.isArray(vacantes) ? vacantes : [];

        renderizarVacantesProceso(vacantesProcesoCache);
        actualizarResumenVacantesProceso(vacantesProcesoCache);

        setBotonesVacantesProceso(true);

        const texto = document.getElementById("texto-vacantes-proceso");
        if (texto) {
            texto.textContent = `Vacantes cargadas para el proceso ${procesoId}. Puede editarlas y guardar antes de procesar.`;
        }

        if (mostrarMensaje) {
            mostrarMensajeCarga("Vacantes del proceso cargadas correctamente.", "ok");
        }

    } catch (error) {
        console.error("Error cargando vacantes del proceso:", error);

        limpiarVacantesProcesoUI("No se pudieron cargar las vacantes del proceso.");

        if (mostrarMensaje) {
            alert("No se pudieron cargar las vacantes: " + error.message);
        }
    }
}

function renderizarVacantesProceso(vacantes) {
    const tbody = document.getElementById("tabla-vacantes-proceso-body");

    if (!tbody) return;

    if (!vacantes || vacantes.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" class="p-6 text-center text-gray-400">
                    No hay vacantes configuradas para este proceso.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = vacantes.map((vacante, index) => {
        const facultad = escapeHtmlVacantes(vacante.facultad || "-");
        const carrera = escapeHtmlVacantes(vacante.carrera || "-");
        const vacantesValor = Number(vacante.vacantes || 0);

        return `
            <tr class="border-b border-gray-100 hover:bg-gray-50" data-vacante-index="${index}">
                <td class="p-3 text-gray-700">
                    ${facultad}
                </td>

                <td class="p-3 font-semibold text-gray-800">
                    ${carrera}
                </td>

                <td class="p-3 text-center">
                    <input type="number"
                           min="1"
                           step="1"
                           data-vacante-input="${index}"
                           value="${vacantesValor}"
                           class="w-24 text-center bg-white border border-gray-300 text-gray-900 text-xs rounded-lg p-2 outline-none focus:ring-blue-500 focus:border-blue-500"
                           onchange="actualizarResumenVacantesDesdeInputs()">
                </td>
            </tr>
        `;
    }).join("");
}

function actualizarResumenVacantesProceso(vacantes) {
    const cantidadCarreras = document.getElementById("cantidad-carreras-vacantes");
    const totalVacantes = document.getElementById("total-vacantes-proceso");
    const estado = document.getElementById("estado-vacantes-proceso");

    const totalCarreras = vacantes ? vacantes.length : 0;
    const total = (vacantes || []).reduce((acc, item) => {
        return acc + Number(item.vacantes || 0);
    }, 0);

    if (cantidadCarreras) cantidadCarreras.textContent = totalCarreras;
    if (totalVacantes) totalVacantes.textContent = total;
    if (estado) estado.textContent = totalCarreras > 0 ? "Configuradas" : "Pendiente";
}

function actualizarResumenVacantesDesdeInputs() {
    const inputs = document.querySelectorAll("[data-vacante-input]");
    let total = 0;

    inputs.forEach(input => {
        total += Number(input.value || 0);
    });

    const totalVacantes = document.getElementById("total-vacantes-proceso");

    if (totalVacantes) {
        totalVacantes.textContent = total;
    }
}

function obtenerVacantesFormulario() {
    if (!vacantesProcesoCache || vacantesProcesoCache.length === 0) {
        throw new Error("No hay vacantes cargadas para guardar.");
    }

    return vacantesProcesoCache.map((vacante, index) => {
        const input = document.querySelector(`[data-vacante-input="${index}"]`);
        const cantidad = Number(input ? input.value : vacante.vacantes);

        if (Number.isNaN(cantidad) || cantidad <= 0) {
            throw new Error(`La carrera ${vacante.carrera} debe tener vacantes mayores a 0.`);
        }

        return {
            facultad: vacante.facultad,
            carrera: vacante.carrera,
            vacantes: cantidad
        };
    });
}

async function guardarVacantesProcesoActual() {
    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        alert("Primero debes crear o seleccionar un proceso.");
        return;
    }

    try {
        const payload = obtenerVacantesFormulario();

        const yaProcesado =
            window.estadoProcesoActual &&
            window.estadoProcesoActual.dbfProcesados === true;

        if (yaProcesado) {
            const confirmar = confirm(
                "Este proceso ya tiene resultados procesados. " +
                "Si cambias las vacantes, debes volver a presionar 'Procesar todo' para recalcular la condición de ingreso. ¿Deseas continuar?"
            );

            if (!confirmar) {
                return;
            }
        }

        const actualizadas = await fetchJson(`${API_BASE}/api/procesos/${procesoId}/vacantes`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        vacantesProcesoCache = Array.isArray(actualizadas) ? actualizadas : [];

        renderizarVacantesProceso(vacantesProcesoCache);
        actualizarResumenVacantesProceso(vacantesProcesoCache);

        mostrarMensajeCarga(
            "Vacantes del proceso guardadas correctamente. Si el proceso ya estaba procesado, debe recalcularse.",
            "ok"
        );

        alert("Vacantes guardadas correctamente.");

    } catch (error) {
        console.error("Error guardando vacantes:", error);
        alert("No se pudieron guardar las vacantes: " + error.message);
    }
}

window.addEventListener("load", async function () {
    const procesoId = getProcesoIdActual();

    if (procesoId) {
        await cargarVacantesProcesoActual(false);
    } else {
        limpiarVacantesProcesoUI();
    }
});