// ==========================================
// CONTROL DE HISTORIAL DE PROCESOS
// ==========================================

function cargarHistorial() {
    console.log("[HISTORIAL] Solicitando lista de procesos anteriores...");

    const tbody = document.getElementById("tabla-historial-body");
    if (!tbody) {
        console.error("[HISTORIAL] Error: No se encontró el elemento 'tabla-historial-body' en el HTML.");
        return;
    }

    // Petición al endpoint general de procesos para listar TODO lo guardado en BD
    fetch("http://localhost:8080/api/procesos")
        .then(res => {
            console.log("[HISTORIAL] Respuesta del servidor - Estado:", res.status);
            if (!res.ok) throw new Error(`Error en el servidor (${res.status})`);
            return res.json();
        })
        .then(data => {
            console.log("[HISTORIAL] Datos recibidos para historial:", data);

            if (!data || data.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="p-8 text-center text-gray-400 text-xs">
                            No se registran procesos previos en la base de datos.
                        </td>
                    </tr>
                `;
                return;
            }

            // Mapeamos absolutamente todos los registros encontrados en la BD
            tbody.innerHTML = data.map(item => {
                const estadoActual = item.estado || "Completado";
                let estadoClass = "";
                let iconoEstado = "";

                // Evaluación estricta de estados visuales
                if (estadoActual === "Completado" || estadoActual === "ACTIVO" || estadoActual === "PROCESADO") {
                    estadoClass = "bg-green-50 text-green-700 border-green-200";
                    iconoEstado = "check-circle-2";
                } else if (estadoActual === "Anulado" || estadoActual === "ANULADO") {
                    estadoClass = "bg-red-50 text-red-700 border-red-200";
                    iconoEstado = "ban";
                } else {
                    estadoClass = "bg-amber-50 text-amber-700 border-amber-200";
                    iconoEstado = "alert-triangle";
                }

                // Fallbacks lógicos para mapear el DTO de Spring Boot
                const nombreProceso = item.proceso || item.nombreProceso || item.nombre || "Proceso sin nombre";
                const fecha = item.fecha || item.fechaRegistro || "---";
                const totalPostulantes = item.totalPostulantes || item.cantidadPostulantes || 0;
                const archivosUsados = item.archivosUsados || 4; // Cambiado a 4 por tus archivos (IDENTIFI, CLAVES, RESPUEST, PDF)

                return `
                <tr class="border-b border-gray-100 hover:bg-gray-50 transition-colors text-xs">
                    <td class="p-3">
                        <p class="font-bold text-gray-800">${nombreProceso}</p>
                        <p class="text-[10px] text-gray-400">Modalidad: ${item.modalidad || 'ORDINARIO'}</p>
                    </td>
                    <td class="p-3 text-center text-gray-600">${fecha}</td>
                    <td class="p-3">
                        <div class="flex items-center justify-center gap-1.5 text-gray-600 text-[11px] font-medium">
                            <div class="bg-green-100 text-green-700 p-1 rounded"><i data-lucide="sheet" class="w-3 h-3"></i></div>
                            <div class="bg-gray-100 text-gray-600 p-1 rounded"><i data-lucide="file-text" class="w-3 h-3"></i></div>
                            <span>${archivosUsados} archivos</span>
                        </div>
                    </td>
                    <td class="p-3 text-center font-bold text-gray-800">${totalPostulantes}</td>
                    <td class="p-3 text-center">
                        <span class="${estadoClass} border px-2.5 py-1 rounded-full text-[10px] font-bold flex items-center justify-center gap-1 w-max mx-auto tracking-wide">
                            <i data-lucide="${iconoEstado}" class="w-3 h-3"></i> ${estadoActual}
                        </span>
                    </td>
                    <td class="p-3">
                        <div class="flex items-center justify-center gap-1.5">
                            <button onclick="localStorage.setItem('procesoActivoId', ${item.id}); if(typeof navegarModulo === 'function') navegarModulo('dashboard', 'Dashboard');" class="text-blue-600 hover:text-blue-800 bg-blue-50 border border-blue-100 p-1.5 rounded transition-colors" title="Cargar este proceso al Dashboard">
                                <i data-lucide="eye" class="w-4 h-4"></i>
                            </button>
                            <button class="text-gray-600 hover:text-gray-800 bg-white border border-gray-200 p-1.5 rounded shadow-sm transition-colors" title="Descargar PDF">
                                <i data-lucide="download" class="w-4 h-4"></i>
                            </button>
                        </div>
                    </td>
                </tr>
                `;
            }).join('');

            // Re-renderizar los iconos de Lucide cargados dinámicamente
            if (typeof lucide !== 'undefined') lucide.createIcons();
        })
        .catch(error => {
            console.error("[HISTORIAL] Error crítico cargando el historial:", error);
            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="p-8 text-center text-red-500 text-xs font-semibold">
                            Error de conexión con el servidor. No se pudo cargar el historial.
                        </td>
                    </tr>
                `;
            }
        });
}

// 🌟 Registro global de la función para evitar fallos de inicialización en main.js
window.cargarHistorial = cargarHistorial;

// Ejecutar si el DOM ya se encuentra listo
if (document.readyState === "complete" || document.readyState === "interactive") {
    cargarHistorial();
}