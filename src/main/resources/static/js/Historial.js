async function cargarHistorial() {
    const procesoId = getProcesoIdActual();
    const tbody = document.getElementById("tabla-historial-body");

    if (!tbody) return;

    if (!procesoId) {
        tbody.innerHTML = `<tr><td colspan="5" class="p-8 text-center text-gray-400">No hay proceso seleccionado.</td></tr>`;
        return;
    }

    try {
        const data = await fetchJson(`${API_BASE}/api/pdf/proceso/${procesoId}/historial`);

        if (!data || !data.length) {
            tbody.innerHTML = `<tr><td colspan="5" class="p-8 text-center text-gray-400">Todavía no se generaron PDFs para este proceso.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.map(item => `
            <tr class="border-b border-gray-100 hover:bg-gray-50 transition-colors text-xs">
                <td class="p-3 font-bold text-blue-900">${item.tipoPdf || ""}</td>
                <td class="p-3">${item.carrera || "GENERAL"}</td>
                <td class="p-3">${item.nombreArchivo || ""}</td>
                <td class="p-3 text-center">${item.fechaGeneracion || ""}</td>
                <td class="p-3 text-center">
                    <button onclick="window.open('${item.urlVer}', '_blank')" class="text-blue-600 hover:text-blue-800 bg-blue-50 border border-blue-100 p-1.5 rounded transition-colors mr-1" title="Ver PDF">
                        <i data-lucide="eye" class="w-4 h-4"></i>
                    </button>
                    <button onclick="window.open('${item.urlDescargar}', '_blank')" class="text-gray-600 hover:text-gray-800 bg-white border border-gray-200 p-1.5 rounded shadow-sm transition-colors" title="Descargar PDF">
                        <i data-lucide="download" class="w-4 h-4"></i>
                    </button>
                </td>
            </tr>
        `).join("");

        if (typeof lucide !== "undefined") lucide.createIcons();

    } catch (error) {
        console.error("Error cargando historial de PDFs:", error);
        tbody.innerHTML = `<tr><td colspan="5" class="p-8 text-center text-red-500 text-xs font-semibold">No se pudo cargar el historial de PDFs.</td></tr>`;
    }
}
