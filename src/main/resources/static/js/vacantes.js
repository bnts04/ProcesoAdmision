let carrerasDetectadasDinamicas = [];

async function cargarTablaVacantesDinamica() {
    const procesoId = getProcesoIdActual();
    const tbody = document.getElementById("tabla-vacantes-body");

    if (!tbody) return;

    if (!procesoId) {
        tbody.innerHTML = `<tr><td colspan="5" class="p-8 text-center text-gray-400">Primero debes crear o seleccionar un proceso.</td></tr>`;
        return;
    }

    try {
        const carreras = await fetchJson(`${API_BASE}/api/resultados/proceso/${procesoId}/resumen-carreras`);
        carrerasDetectadasDinamicas = carreras;

        if (!carreras.length) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="p-8 text-center text-gray-400">
                        El proceso aún no tiene resultados. Sube los DBF y presiona “Procesar todo”.
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = carreras.map(item => `
            <tr class="hover:bg-gray-50 transition-colors border-b border-gray-100">
                <td class="p-4 font-medium text-gray-900">${item.facultad || "FACULTAD NO DEFINIDA"}</td>
                <td class="p-4">${item.carrera || ""}</td>
                <td class="p-4 text-center font-bold text-blue-900">${item.vacantes ?? 0}</td>
                <td class="p-4 text-center">${item.totalPostulantes ?? 0}</td>
                <td class="p-4 text-center text-green-700 font-bold">${item.totalIngresantes ?? 0}</td>
            </tr>
        `).join("");

    } catch (error) {
        console.error("Error cargando resumen de carreras:", error);
        tbody.innerHTML = `<tr><td colspan="5" class="p-4 text-center text-red-500 font-medium">No se pudo cargar la información del proceso actual.</td></tr>`;
    }
}
