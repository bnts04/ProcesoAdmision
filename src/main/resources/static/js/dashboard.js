async function jalarDatosDashboard() {
    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        limpiarDashboard();
        return;
    }

    try {
        const data = await fetchJson(`${API_BASE}/api/resultados/proceso/${procesoId}?limite=0`);

        const total = data.length;
        const ingresantes = data.filter(a => a.condicion === "INGRESO").length;
        const noIngresantes = data.filter(a => a.condicion === "NO_INGRESO").length;
        const puntajes = data.map(a => Number(a.puntajeFinal || 0)).filter(p => !isNaN(p));
        const maxPuntaje = puntajes.length ? Math.max(...puntajes) : 0;

        document.getElementById("dash-total").textContent = total;
        document.getElementById("dash-ingresantes").textContent = ingresantes;
        document.getElementById("dash-noingresantes").textContent = noIngresantes;
        document.getElementById("dash-max").textContent = maxPuntaje.toFixed(2);

        const tbody = document.getElementById("dash-tabla-body");
        if (!tbody) return;

        if (!data.length) {
            tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-gray-400">No hay resultados procesados para este proceso.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.map((al, index) => `
            <tr class="border-b border-gray-100 hover:bg-gray-50">
                <td class="p-3 text-center">${index + 1}</td>
                <td class="p-3">${al.codigo || ""}</td>
                <td class="p-3">${al.apellidosNombres || ""}</td>
                <td class="p-3 text-center font-bold">${al.puntajeFinal ?? "0.0000"}</td>
                <td class="p-3 text-center">${al.ome ?? ""}</td>
                <td class="p-3 text-center">${al.omg ?? ""}</td>
                <td class="p-3 text-center">${al.condicion || ""}</td>
            </tr>
        `).join("");

    } catch (error) {
        console.error("Error cargando dashboard:", error);
        limpiarDashboard();
    }
}

function limpiarDashboard() {
    ["dash-total", "dash-ingresantes", "dash-noingresantes"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = "0";
    });
    const max = document.getElementById("dash-max");
    if (max) max.textContent = "0.00";

    const tbody = document.getElementById("dash-tabla-body");
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-gray-400">No hay resultados procesados.</td></tr>`;
    }
}
