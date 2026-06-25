const API_BASE = "http://localhost:8080";

window.estadoProcesoActual = null;
window.modulosBloqueados = new Set();

function getProcesoIdActual() {
    return localStorage.getItem("procesoIdActual") || localStorage.getItem("procesoActivoId");
}

function setProcesoIdActual(procesoId) {
    if (!procesoId) return;

    localStorage.setItem("procesoIdActual", procesoId);
    localStorage.setItem("procesoActivoId", procesoId);

    actualizarBadgeProceso();
}

function limpiarProcesoIdActual() {
    localStorage.removeItem("procesoIdActual");
    localStorage.removeItem("procesoActivoId");

    window.estadoProcesoActual = null;

    actualizarBadgeProceso();
}

function actualizarBadgeProceso() {
    const badge = document.getElementById("badge-proceso-actual");
    const procesoId = getProcesoIdActual();

    if (!badge) return;

    if (procesoId) {
        badge.textContent = `Proceso ID: ${procesoId}`;
        badge.classList.remove("hidden");
    } else {
        badge.classList.add("hidden");
    }
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Error HTTP ${response.status}`);
    }

    return await response.json();
}

function mostrarMensajeCarga(mensaje, tipo = "info") {
    const box = document.getElementById("estado-proceso-box");

    if (!box) return;

    const clases = {
        info: "bg-blue-50 border-blue-200 text-blue-900",
        ok: "bg-green-50 border-green-200 text-green-900",
        warn: "bg-yellow-50 border-yellow-200 text-yellow-900",
        error: "bg-red-50 border-red-200 text-red-900"
    };

    box.className = `border p-4 rounded-xl text-sm ${clases[tipo] || clases.info}`;
    box.textContent = mensaje;
}

function actualizarResumenVistaPrevia(data) {
    const cont = document.getElementById("resumen-vista-previa-final");

    if (!cont) return;

    if (!data) {
        cont.innerHTML = "Todavía no hay proceso seleccionado.";
        return;
    }

    cont.innerHTML = `
        <div class="grid grid-cols-2 gap-2">
            <div><span class="font-semibold">Proceso:</span> ${data.nombreProceso || "-"}</div>
            <div><span class="font-semibold">Estado:</span> ${data.estadoProceso || "-"}</div>
            <div><span class="font-semibold">Postulantes:</span> ${data.totalPostulantes ?? 0}</div>
            <div><span class="font-semibold">Ingresantes:</span> ${data.totalIngresantes ?? 0}</div>
            <div><span class="font-semibold">DBF procesados:</span> ${data.dbfProcesados ? "Sí" : "No"}</div>
            <div><span class="font-semibold">PDF guía:</span> ${data.pdfGuiaDisponible ? "Disponible" : "No disponible"}</div>
        </div>
        <div class="mt-3 text-[11px] text-gray-500">
            ${(data.observaciones || []).join(" ")}
        </div>
    `;
}

function setBotonBloqueado(idBoton, bloquear) {
    const boton = document.getElementById(idBoton);

    if (!boton) return;

    boton.disabled = bloquear;

    if (bloquear) {
        boton.classList.add("opacity-40", "cursor-not-allowed");
        boton.classList.remove("cursor-pointer");
    } else {
        boton.classList.remove("opacity-40", "cursor-not-allowed");
        boton.classList.add("cursor-pointer");
    }
}

function bloquearModulosHastaProcesar() {
    window.modulosBloqueados = new Set(["dashboard", "vista-previa", "historial"]);

    setBotonBloqueado("btn-dashboard", true);
    setBotonBloqueado("btn-vista-previa", true);
    setBotonBloqueado("btn-historial", true);
}

function desbloquearModulosProcesados() {
    window.modulosBloqueados = new Set();

    setBotonBloqueado("btn-dashboard", false);
    setBotonBloqueado("btn-vista-previa", false);
    setBotonBloqueado("btn-historial", false);
}

function actualizarBotonProcesar(data) {
    const btn = document.getElementById("btn-procesar-todo");

    if (!btn) return;

    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        btn.disabled = true;
        btn.classList.add("opacity-40", "cursor-not-allowed");
        btn.classList.remove("cursor-pointer");
        return;
    }

    btn.disabled = false;
    btn.classList.remove("opacity-40", "cursor-not-allowed");
    btn.classList.add("cursor-pointer");
}

async function verificarEstadoProcesoActual({ navegar = true } = {}) {
    actualizarBadgeProceso();

    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        bloquearModulosHastaProcesar();
        actualizarBotonProcesar(null);
        actualizarResumenVistaPrevia(null);

        if (typeof actualizarModoCreacionProceso === "function") {
            actualizarModoCreacionProceso(false);
        }

        if (typeof restaurarCalificacionDefecto === "function") {
            restaurarCalificacionDefecto();
        }

        if (typeof limpiarVacantesProcesoUI === "function") {
            limpiarVacantesProcesoUI();
        }

        mostrarMensajeCarga("No hay proceso seleccionado. Crea un proceso nuevo para continuar.", "info");

        if (navegar && typeof navegarModulo === "function") {
            navegarModulo("carga", "Gestión de proceso");
        }

        return null;
    }

    try {
        const data = await fetchJson(`${API_BASE}/api/procesamiento/proceso/${procesoId}/vista-previa-final`);

        window.estadoProcesoActual = data;

        if (typeof actualizarModoCreacionProceso === "function") {
            actualizarModoCreacionProceso(true);
        }

        actualizarResumenVistaPrevia(data);
        actualizarBotonProcesar(data);

        if (typeof cargarConfiguracionCalificacionProcesoActual === "function") {
            await cargarConfiguracionCalificacionProcesoActual(false);
        }

        if (typeof cargarVacantesProcesoActual === "function") {
            await cargarVacantesProcesoActual(false);
        }

        if (data.mostrarDashboard === true || data.dbfProcesados === true) {
            desbloquearModulosProcesados();

            mostrarMensajeCarga(
                data.mensaje || "Proceso procesado correctamente.",
                "ok"
            );

            await Promise.allSettled([
                typeof jalarDatosDashboard === "function" ? jalarDatosDashboard() : Promise.resolve(),
                typeof cargarCarreras === "function" ? cargarCarreras() : Promise.resolve(),
                typeof cargarTablaVacantesDinamica === "function" ? cargarTablaVacantesDinamica() : Promise.resolve()
            ]);

        } else {
            bloquearModulosHastaProcesar();
            actualizarBotonProcesar(data);

            mostrarMensajeCarga(
                data.mensaje || "Proceso creado. Pendiente de ejecutar procesamiento completo.",
                data.puedeProcesar ? "warn" : "info"
            );

            if (navegar && typeof navegarModulo === "function") {
                navegarModulo("carga", "Gestión de proceso");
            }
        }

        return data;

    } catch (error) {
        console.error("No se pudo verificar el estado del proceso:", error);

        bloquearModulosHastaProcesar();
        actualizarBotonProcesar(null);

        if (typeof limpiarVacantesProcesoUI === "function") {
            limpiarVacantesProcesoUI("No se pudieron cargar las vacantes del proceso.");
        }

        mostrarMensajeCarga(
            "El proceso existe, pero todavía falta cargar o validar archivos DBF.",
            "warn"
        );

        if (navegar && typeof navegarModulo === "function") {
            navegarModulo("carga", "Gestión de proceso");
        }

        return null;
    }
}

async function ejecutarProcesamientoCompletoActual() {
    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        alert("Primero debes crear o seleccionar un proceso.");
        return;
    }

    const btn = document.getElementById("btn-procesar-todo");

    try {
        if (btn) {
            btn.disabled = true;
            btn.classList.add("opacity-40", "cursor-not-allowed");
            btn.innerHTML = `
                <i data-lucide="loader-circle" class="w-4 h-4 animate-spin"></i>
                Procesando...
            `;

            if (typeof lucide !== "undefined") {
                lucide.createIcons();
            }
        }

        mostrarMensajeCarga("Ejecutando procesamiento completo. Espere unos segundos...", "warn");

        const data = await fetchJson(`${API_BASE}/api/procesamiento/proceso/${procesoId}/ejecutar-todo`, {
            method: "POST"
        });

        alert(data.mensaje || "Procesamiento completo ejecutado correctamente.");

        await verificarEstadoProcesoActual({ navegar: false });

        if (data.ejecutadoCorrectamente === true) {
            desbloquearModulosProcesados();

            await Promise.allSettled([
                typeof jalarDatosDashboard === "function" ? jalarDatosDashboard() : Promise.resolve(),
                typeof cargarCarreras === "function" ? cargarCarreras() : Promise.resolve(),
                typeof cargarTablaVacantesDinamica === "function" ? cargarTablaVacantesDinamica() : Promise.resolve()
            ]);

            if (typeof navegarModulo === "function") {
                navegarModulo("dashboard", "Dashboard global");
            }
        }

    } catch (error) {
        console.error("Error ejecutando procesamiento completo:", error);

        alert("Error ejecutando procesamiento completo: " + error.message);

        await verificarEstadoProcesoActual({ navegar: true });

    } finally {
        if (btn) {
            btn.innerHTML = `
                <i data-lucide="play-circle" class="w-4 h-4"></i>
                Procesar todo
            `;

            btn.disabled = false;
            btn.classList.remove("opacity-40", "cursor-not-allowed");
            btn.classList.add("cursor-pointer");

            if (typeof lucide !== "undefined") {
                lucide.createIcons();
            }
        }
    }
}

function limpiarProcesoActualUI() {
    limpiarProcesoIdActual();

    if (typeof limpiarDashboard === "function") {
        limpiarDashboard();
    }

    if (typeof limpiarVistaPrevia === "function") {
        limpiarVistaPrevia();
    }

    bloquearModulosHastaProcesar();
    actualizarBotonProcesar(null);

    if (typeof actualizarModoCreacionProceso === "function") {
        actualizarModoCreacionProceso(false);
    }

    if (typeof restaurarCalificacionDefecto === "function") {
        restaurarCalificacionDefecto();
    }

    if (typeof limpiarVacantesProcesoUI === "function") {
        limpiarVacantesProcesoUI();
    }

    mostrarMensajeCarga("Selección limpiada. Crea un proceso nuevo para continuar.", "info");

    if (typeof navegarModulo === "function") {
        navegarModulo("carga", "Gestión de proceso");
    }
}

window.addEventListener("load", async function () {
    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }

    actualizarBadgeProceso();

    await verificarEstadoProcesoActual();
});