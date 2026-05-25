async function cargarHistorial() {
    const tbody = document.getElementById("tabla-historial-procesos-body");

    if (!tbody) return;

    tbody.innerHTML = `
        <tr>
            <td colspan="10" class="p-8 text-center text-gray-400">
                Cargando historial de procesos...
            </td>
        </tr>
    `;

    try {
        const procesos = await fetchJson(`${API_BASE}/api/procesos`);

        if (!procesos || procesos.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="10" class="p-8 text-center text-gray-400">
                        Todavía no existen procesos registrados.
                    </td>
                </tr>
            `;

            actualizarResumenHistorial([], []);
            return;
        }

        const procesosOrdenados = [...procesos].sort((a, b) => {
            const idA = Number(a.id || 0);
            const idB = Number(b.id || 0);
            return idB - idA;
        });

        const procesosDetalle = await Promise.all(
            procesosOrdenados.map(async (proceso) => {
                return await obtenerDetalleProcesoHistorial(proceso);
            })
        );

        renderizarHistorialProcesos(procesosDetalle);
        actualizarResumenHistorial(procesosOrdenados, procesosDetalle);

    } catch (error) {
        console.error("Error cargando historial de procesos:", error);

        tbody.innerHTML = `
            <tr>
                <td colspan="10" class="p-8 text-center text-red-500 font-semibold">
                    No se pudo cargar el historial de procesos.
                </td>
            </tr>
        `;
    }
}

async function obtenerDetalleProcesoHistorial(proceso) {
    const procesoId = proceso.id;

    let vistaPrevia = null;
    let pdfs = [];
    let anulaciones = [];

    try {
        vistaPrevia = await fetchJson(`${API_BASE}/api/procesamiento/proceso/${procesoId}/vista-previa-final`);
    } catch (error) {
        console.warn(`No se pudo cargar vista previa del proceso ${procesoId}`, error);
    }

    try {
        pdfs = await fetchJson(`${API_BASE}/api/pdf/proceso/${procesoId}/historial`);
    } catch (error) {
        console.warn(`No se pudo cargar historial PDF del proceso ${procesoId}`, error);
    }

    try {
        anulaciones = await fetchJson(`${API_BASE}/api/anulaciones-postulante/proceso/${procesoId}`);
    } catch (error) {
        console.warn(`No se pudo cargar anulaciones del proceso ${procesoId}`, error);
    }

    return {
        proceso,
        vistaPrevia,
        pdfs: Array.isArray(pdfs) ? pdfs : [],
        anulaciones: Array.isArray(anulaciones) ? anulaciones : []
    };
}

function renderizarHistorialProcesos(detalles) {
    const tbody = document.getElementById("tabla-historial-procesos-body");

    if (!tbody) return;

    if (!detalles || detalles.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="10" class="p-8 text-center text-gray-400">
                    No hay procesos para mostrar.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = detalles.map(item => {
        const proceso = item.proceso || {};
        const vista = item.vistaPrevia || {};

        const procesoId = proceso.id;
        const nombreProceso = proceso.nombreProceso || vista.nombreProceso || "-";
        const modalidad = proceso.modalidad || vista.modalidad || "-";
        const estado = vista.estadoProceso || proceso.estado || "-";

        const totalPostulantes = vista.totalPostulantes ?? 0;
        const totalIngresantes = vista.totalIngresantes ?? 0;
        const totalNoIngresantes = vista.totalNoIngresantes ?? 0;

        const totalPdfs = item.pdfs.length;
        const totalAnulaciones = item.anulaciones.length;

        const estadoClass = obtenerClaseEstadoProceso(estado);

        return `
            <tr class="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <td class="p-3 text-center font-bold text-blue-900">${procesoId}</td>

                <td class="p-3">
                    <div class="font-semibold text-gray-900">${nombreProceso}</div>
                    <div class="text-[11px] text-gray-400">Proceso ID: ${procesoId}</div>
                </td>

                <td class="p-3">${modalidad}</td>

                <td class="p-3 text-center">
                    <span class="px-2 py-1 rounded-full text-[11px] font-bold ${estadoClass}">
                        ${estado}
                    </span>
                </td>

                <td class="p-3 text-center font-semibold">${totalPostulantes}</td>
                <td class="p-3 text-center font-semibold text-green-700">${totalIngresantes}</td>
                <td class="p-3 text-center font-semibold text-red-700">${totalNoIngresantes}</td>

                <td class="p-3 text-center">
                    <span class="font-semibold">${totalPdfs}</span>
                </td>

                <td class="p-3 text-center">
                    <span class="font-semibold ${totalAnulaciones > 0 ? "text-red-700" : "text-gray-600"}">
                        ${totalAnulaciones}
                    </span>
                </td>

                <td class="p-3">
                    <div class="flex items-center justify-center gap-1 flex-wrap">
                        <button onclick="seleccionarProcesoDesdeHistorial(${procesoId})"
                                class="bg-blue-50 hover:bg-blue-100 text-blue-700 border border-blue-100 px-2 py-1 rounded text-[11px] font-semibold">
                            Seleccionar
                        </button>

                        ${vista.dbfProcesados ? `
                            <button onclick="verResultadosProcesoHistorial(${procesoId})"
                                    class="bg-green-50 hover:bg-green-100 text-green-700 border border-green-100 px-2 py-1 rounded text-[11px] font-semibold">
                                Ver
                            </button>
                        ` : `
                            <button onclick="procesarProcesoDesdeHistorial(${procesoId})"
                                    class="bg-amber-50 hover:bg-amber-100 text-amber-700 border border-amber-100 px-2 py-1 rounded text-[11px] font-semibold">
                                Procesar
                            </button>
                        `}

                        ${totalPdfs > 0 ? `
                            <button onclick="abrirUltimoPdfProceso(${procesoId})"
                                    class="bg-red-50 hover:bg-red-100 text-red-700 border border-red-100 px-2 py-1 rounded text-[11px] font-semibold">
                                PDF
                            </button>
                        ` : `
                            <button onclick="generarPdfProcesoDesdeHistorial(${procesoId})"
                                    class="bg-gray-50 hover:bg-gray-100 text-gray-700 border border-gray-200 px-2 py-1 rounded text-[11px] font-semibold">
                                Gen. PDF
                            </button>
                        `}
                    </div>
                </td>
            </tr>
        `;
    }).join("");

    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}

function actualizarResumenHistorial(procesos, detalles) {
    const totalProcesos = detalles.length;

    const completados = detalles.filter(item => {
        const vista = item.vistaPrevia || {};
        return vista.dbfProcesados === true || vista.estadoProceso === "COMPLETADO";
    }).length;

    const pendientes = totalProcesos - completados;

    const totalAnulaciones = detalles.reduce((acc, item) => {
        return acc + item.anulaciones.length;
    }, 0);

    const elTotal = document.getElementById("hist-total-procesos");
    const elCompletados = document.getElementById("hist-completados");
    const elPendientes = document.getElementById("hist-pendientes");
    const elAnulaciones = document.getElementById("hist-anulaciones");

    if (elTotal) elTotal.textContent = totalProcesos;
    if (elCompletados) elCompletados.textContent = completados;
    if (elPendientes) elPendientes.textContent = pendientes;
    if (elAnulaciones) elAnulaciones.textContent = totalAnulaciones;
}

function obtenerClaseEstadoProceso(estado) {
    if (!estado) {
        return "bg-gray-100 text-gray-700";
    }

    const valor = estado.toUpperCase();

    if (valor.includes("COMPLETADO")) {
        return "bg-green-100 text-green-700";
    }

    if (valor.includes("ARCHIVOS")) {
        return "bg-blue-100 text-blue-700";
    }

    if (valor.includes("PENDIENTE")) {
        return "bg-amber-100 text-amber-700";
    }

    if (valor.includes("ANULADO")) {
        return "bg-red-100 text-red-700";
    }

    return "bg-gray-100 text-gray-700";
}

async function seleccionarProcesoDesdeHistorial(procesoId) {
    setProcesoIdActual(procesoId);

    await verificarEstadoProcesoActual({ navegar: false });

    alert(`Proceso ${procesoId} seleccionado correctamente.`);

    const estado = window.estadoProcesoActual;

    if (estado && estado.dbfProcesados === true) {
        navegarModulo("dashboard", "Dashboard global");
    } else {
        navegarModulo("carga", "Gestión de proceso");
    }
}

async function verResultadosProcesoHistorial(procesoId) {
    setProcesoIdActual(procesoId);

    await verificarEstadoProcesoActual({ navegar: false });

    if (window.estadoProcesoActual && window.estadoProcesoActual.dbfProcesados === true) {
        navegarModulo("dashboard", "Dashboard global");
    } else {
        alert("Este proceso todavía no tiene resultados procesados.");
        navegarModulo("carga", "Gestión de proceso");
    }
}

async function procesarProcesoDesdeHistorial(procesoId) {
    setProcesoIdActual(procesoId);

    await verificarEstadoProcesoActual({ navegar: false });

    await ejecutarProcesamientoCompletoActual();

    await cargarHistorial();
}

async function generarPdfProcesoDesdeHistorial(procesoId) {
    setProcesoIdActual(procesoId);

    await verificarEstadoProcesoActual({ navegar: false });

    if (!window.estadoProcesoActual || window.estadoProcesoActual.dbfProcesados !== true) {
        alert("Primero debes procesar el proceso antes de generar PDF.");
        return;
    }

    await generarPdfGeneral();

    await cargarHistorial();
}

async function abrirUltimoPdfProceso(procesoId) {
    try {
        const pdfs = await fetchJson(`${API_BASE}/api/pdf/proceso/${procesoId}/historial`);

        if (!pdfs || pdfs.length === 0) {
            alert("Este proceso no tiene PDFs generados.");
            return;
        }

        const ultimoPdf = pdfs[0];

        if (ultimoPdf.urlVer) {
            window.open(ultimoPdf.urlVer, "_blank");
        } else {
            alert("El PDF no tiene URL de visualización.");
        }

    } catch (error) {
        console.error("Error abriendo PDF:", error);
        alert("No se pudo abrir el PDF del proceso.");
    }
}