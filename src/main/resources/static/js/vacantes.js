/**
 * Gestión de vacantes y llamadas de ejecución masiva
 */

// Variable global que guardará dinámicamente las carreras que el Backend devuelva
let carrerasDetectadasDinamicas = [];

function cargarTablaVacantesDinamica() {
    fetch("http://localhost:8080/api/resultados/proceso/1/resumen-carreras")
        .then(res => {
            if (!res.ok) throw new Error("Error en la respuesta del servidor");
            return res.json();
        })
        .then(carreras => {
            carrerasDetectadasDinamicas = carreras;
            const tbody = document.getElementById('tabla-vacantes-body');
            if (!tbody) return;

            tbody.innerHTML = '';

            if (carreras.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="3" class="p-8 text-center text-gray-400">
                            <i data-lucide="alert-circle" class="w-8 h-8 mx-auto mb-2 text-gray-300"></i>
                            No se han detectado carreras para este proceso. Asegúrate de haber cargado el PDF Guía.
                        </td>
                    </tr>
                `;
                if (typeof lucide !== 'undefined') lucide.createIcons();
                return;
            }

            tbody.innerHTML = carreras.map((item, index) => `
                <tr class="hover:bg-gray-50 transition-colors border-b border-gray-100">
                    <td class="p-4 font-medium text-gray-900">${item.facultad || 'FACULTAD NO DEFINIDA'}</td>
                    <td class="p-4">${item.carrera}</td>
                    <td class="p-4 text-center">
                        <input type="number"
                               min="0"
                               value="${item.vacantes || 0}"
                               data-index="${index}"
                               class="input-vacante w-24 text-center bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 p-2 font-semibold"
                        />
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => {
            console.error("Error al jalar las carreras detectadas:", error);
            const tbody = document.getElementById('tabla-vacantes-body');
            if (tbody) {
                tbody.innerHTML = `<tr><td colspan="3" class="p-4 text-center text-red-500 font-medium">Error al conectar con el servidor de vacantes.</td></tr>`;
            }
        });
}

function guardarVacantesMasivo() {
    const inputs = document.querySelectorAll('.input-vacante');
    const payloadEnvio = [];

    inputs.forEach(input => {
        const index = input.getAttribute('data-index');
        let nuevaVacante = parseInt(input.value, 10) || 1;

        if (nuevaVacante < 1) {
            nuevaVacante = 1;
        }

        payloadEnvio.push({
            facultad: carrerasDetectadasDinamicas[index].facultad || "GENERAL",
            carrera: carrerasDetectadasDinamicas[index].carrera,
            vacantes: nuevaVacante
        });
    });

    console.log("JSON validado enviado al Backend:", payloadEnvio);

    fetch('http://localhost:8080/api/vacantes/masivo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payloadEnvio)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(`Error en Vacantes (${response.status}): ${text}`)
            });
        }
        console.log("Vacantes guardadas con éxito. Ejecutando procesamiento global...");
        return fetch('http://localhost:8080/api/procesamiento/proceso/1/ejecutar-todo', {
            method: 'POST'
        });
    })
    .then(responseProcesamiento => {
        if (!responseProcesamiento.ok) {
            return responseProcesamiento.text().then(text => {
                throw new Error(`Error en Ejecutar Todo (${responseProcesamiento.status}): ${text}`)
            });
        }
        alert('¡Vacantes configuradas y resultados recalculados exitosamente!');
        jalarDatosDashboard();
    })
    .catch(error => {
        console.error('--- ERROR DE INTEGRACIÓN ---');
        console.error(error.message);
        alert('Hubo un problema: ' + error.message);
    });
}