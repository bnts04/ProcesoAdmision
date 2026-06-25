async function cargarCarreras() {
    const procesoId = getProcesoIdActual();
    const selector = document.getElementById("selector-carrera");

    if (!selector) return;

    selector.innerHTML = '<option value="">GENERAL</option>';

    if (!procesoId) return;

    try {
        const carreras = await fetchJson(`${API_BASE}/api/resultados/proceso/${procesoId}/resumen-carreras`);

        carreras.forEach(c => {
            selector.innerHTML += `<option value="${c.carrera}">${c.carrera}</option>`;
        });

    } catch (error) {
        console.error("Error cargando carreras:", error);
    }
}

async function filtrarPorCarrera() {
    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        alert("Primero debes crear o seleccionar un proceso.");
        return;
    }

    const carrera = document.getElementById("selector-carrera").value;
    const url = carrera
        ? `${API_BASE}/api/resultados/proceso/${procesoId}/carrera?nombre=${encodeURIComponent(carrera)}&limite=0`
        : `${API_BASE}/api/resultados/proceso/${procesoId}?limite=0`;

    try {
        const data = await fetchJson(url);

        document.getElementById("vp-total").textContent = data.length;

        const ingresantes = data.filter(a => a.condicion === "INGRESO").length;
        const noIngresantes = data.filter(a => a.condicion === "NO_INGRESO").length;
        const puntajes = data.map(a => Number(a.puntajeFinal || 0)).filter(p => !isNaN(p));
        const maximo = puntajes.length ? Math.max(...puntajes) : 0;
        const promedio = puntajes.length ? puntajes.reduce((acc, p) => acc + p, 0) / puntajes.length : 0;

        document.getElementById("vp-ingresantes").textContent = ingresantes;
        document.getElementById("vp-noingresantes").textContent = noIngresantes;
        document.getElementById("vp-max").textContent = maximo.toFixed(4);
        document.getElementById("vp-promedio").textContent = promedio.toFixed(4);

        const tbody = document.getElementById("tabla-vista-previa");
        if (!tbody) return;

        if (!data.length) {
            tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-gray-400">No hay resultados para mostrar.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.map((al, index) => `
            <tr class="border-b border-gray-100 hover:bg-gray-50">
                <td class="p-3 text-center">${index + 1}</td>
                <td class="p-3">${al.codigo || ""}</td>
                <td class="p-3">${al.apellidosNombres || ""}</td>
                <td class="p-3 text-center">${al.puntajeFinal ?? "0.0000"}</td>
                <td class="p-3 text-center">${al.ome ?? ""}</td>
                <td class="p-3 text-center">${al.omg ?? ""}</td>
                <td class="p-3 text-center">${al.condicion || ""}</td>
            </tr>
        `).join("");

    } catch (error) {
        console.error("Error cargando vista previa:", error);
        alert("No se pudo cargar la vista previa por carrera.");
    }
}

function limpiarVistaPrevia() {
    ["vp-total", "vp-ingresantes", "vp-noingresantes", "vp-max", "vp-promedio"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = "0";
    });

    const tbody = document.getElementById("tabla-vista-previa");
    if (tbody) tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-gray-400">No hay resultados para mostrar.</td></tr>`;

    const selector = document.getElementById("selector-carrera");
    if (selector) selector.innerHTML = '<option value="">GENERAL</option>';
}
