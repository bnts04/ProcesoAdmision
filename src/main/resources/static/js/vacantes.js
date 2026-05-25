let carrerasDetectadasDinamicas = [];

function cargarTablaVacantesDinamica() {
    console.log("[VACANTES] Extrayendo carreras reales del Proceso 1...");

    // Forzamos el ID al Proceso 1 para tu ejemplo
    const procesoId = "1";
    // Apuntamos al ID exacto del tbody de tu <section id="vista-reporte">
    const tbody = document.getElementById('tabla-vacantes-body');

    if (!tbody) {
        console.error("[VACANTES] No se encontró el contenedor 'tabla-vacantes-body' en el DOM.");
        return;
    }

    // Consultamos directamente tu endpoint de resumen de carreras de la BD
    fetch(`http://localhost:8080/api/resultados/proceso/${procesoId}/resumen-carreras`)
        .then(res => {
            if (!res.ok) throw new Error("Error obteniendo datos del servidor");
            return res.json();
        })
        .then(carreras => {
            carrerasDetectadasDinamicas = carreras;
            tbody.innerHTML = '';

            if (carreras.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="3" class="p-8 text-center text-gray-400 text-xs">
                            No se han detectado carreras para el Proceso 1 en la base de datos. Verifique que los DBF contengan datos.
                        </td>
                    </tr>
                `;
                return;
            }

            // 🌟 RENDERIZADO EXCLUSIVO PARA TU TABLA DE 3 COLUMNAS
            tbody.innerHTML = carreras.map(item => `
                <tr class="hover:bg-gray-50 transition-colors border-b border-gray-200 text-sm text-gray-600">
                    <td class="p-4 font-semibold text-gray-700">
                        ${item.facultad || 'FACULTAD NO DEFINIDA'}
                    </td>
                    <td class="p-4 text-gray-900">
                        ${item.carrera}
                    </td>
                    <td class="p-4 text-center font-bold text-blue-900 bg-blue-50/40 w-40 text-base">
                        ${item.vacantes || 0}
                    </td>
                </tr>
            `).join('');

            console.log(`[VACANTES] Sincronización exitosa. Mostrando ${carreras.length} carreras reales.`);
        })
        .catch(error => {
            console.error("Error al jalar las carreras:", error);
            tbody.innerHTML = `<tr><td colspan="3" class="p-4 text-center text-red-500 text-xs">Error al conectar con el servidor.</td></tr>`;
        });
}

// Aseguramos que la ventana registre la función globalmente para evitar el ReferenceError de main.js
window.cargarTablaVacantesDinamica = cargarTablaVacantesDinamica;