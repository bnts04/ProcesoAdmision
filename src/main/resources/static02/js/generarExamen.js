const API_AREAS_URL = 'http://localhost:8080/api/areas-examen';
const API_EXAMEN_URL = 'http://localhost:8080/api/examenes';

let areaSeleccionada = 'AREA_A';

document.addEventListener('DOMContentLoaded', () => {
    inicializarManejadoresArea();
    configurarBotonGenerar();
    inicializarContadoresInteractivos();

    cargarDistribucionPorArea(areaSeleccionada);
});

function inicializarManejadoresArea() {
    const botones = document.querySelectorAll('.btn-area');

    botones.forEach(btn => {
        btn.addEventListener('click', () => {
            // Limpiar estilos activos de todos los botones
            botones.forEach(b => {
                b.className = "btn-area w-10 h-10 flex items-center justify-center rounded-lg bg-white border border-slate-300 text-slate-600 font-bold text-sm hover:bg-slate-50 transition-all";
            });

            // Aplicar estilos activos (azul) al botón presionado
            btn.className = "btn-area w-10 h-10 flex items-center justify-center rounded-lg bg-blue-600 text-white font-bold text-sm transition-all shadow-sm";

            // Actualizar el estado con el atributo data-area ('AREA_A', 'AREA_B', etc.)
            areaSeleccionada = btn.getAttribute('data-area');
            console.log(`Área cambiada a: ${areaSeleccionada}. Consultando backend...`);

            // Consultar dinámicamente la BDD para actualizar los componentes y subcursos
            cargarDistribucionPorArea(areaSeleccionada);
        });
    });
}

async function cargarDistribucionPorArea(codigoArea) {
    const tbodyComponentes = document.getElementById('tablaDistribucionComponentes');
    const tbodySubcursos = document.getElementById('tablaDistribucionSubcursos');
    const indicadorPreguntas = document.getElementById('indicadorPreguntas');
    const tituloDistribucion = document.getElementById('tituloDistribucion');

    if (tbodyComponentes) tbodyComponentes.innerHTML = `<tr><td colspan="3" class="text-center py-4 text-slate-400 text-xs">Cargando componentes...</td></tr>`;
    if (tbodySubcursos) tbodySubcursos.innerHTML = `<tr><td colspan="3" class="text-center py-4 text-slate-400 text-xs">Cargando subcursos...</td></tr>`;

    try {
        const response = await fetch(`${API_AREAS_URL}/${codigoArea}/configuracion`);

        if (!response.ok) {
            throw new Error(`Área no encontrada o error en servidor: ${response.status}`);
        }

        const data = await response.json();
        console.log("JSON recibido del Backend:", data);

        if (tituloDistribucion && data.area) {
            const nombreArea = data.area.nombre || "Área";
            const descArea = data.area.descripcion || "";
            tituloDistribucion.innerText = `Distribución de preguntas para el ${nombreArea}: ${descArea}`;
        }

        if (indicadorPreguntas) {
            indicadorPreguntas.innerText = data.totalPreguntasRequeridas !== undefined ? data.totalPreguntasRequeridas : 0;
        }

        let htmlComponentes = '';
        let totalEnBancoComp = 0;
        let totalUsarComp = 0;

        if (data.resumenComponentes && data.resumenComponentes.length > 0) {
            data.resumenComponentes.forEach(c => {
                const enBanco = c.preguntasDisponibles !== undefined ? c.preguntasDisponibles : (c.cantidadDisponible || c.enBanco || 0);
                const usar = c.preguntasRequeridas !== undefined ? c.preguntasRequeridas : (c.cantidadRequerida || c.usar || 0);

                totalEnBancoComp += enBanco;
                totalUsarComp += usar;

                htmlComponentes += `
                    <tr class="border-b border-slate-50 hover:bg-slate-50/50">
                        <td class="px-6 py-3 text-slate-600 font-medium">${c.nombre || c.componente || c.nombreComponente}</td>
                        <td class="px-6 py-3 text-center text-slate-500">${enBanco}</td>
                        <td class="px-6 py-3 text-center font-semibold text-slate-800">${usar}</td>
                    </tr>
                `;
            });

            htmlComponentes += `
                <tr class="bg-blue-50/50 font-bold text-blue-800 border-t-2 border-slate-200">
                    <td class="px-6 py-3">Total</td>
                    <td class="px-6 py-3 text-center">${totalEnBancoComp}</td>
                    <td class="px-6 py-3 text-center">${totalUsarComp}</td>
                </tr>
            `;
            tbodyComponentes.innerHTML = htmlComponentes;
        } else {
            tbodyComponentes.innerHTML = `<tr><td colspan="3" class="text-center py-4 text-slate-500">Sin componentes para el área ${codigoArea}.</td></tr>`;
        }

        let htmlSubcursos = '';
        if (data.detalleSubcursos && data.detalleSubcursos.length > 0) {
            data.detalleSubcursos.forEach(s => {
                const enBancoSub = s.preguntasDisponibles !== undefined ? s.preguntasDisponibles : (s.cantidadDisponible || s.enBanco || 0);
                const usarSub = s.preguntasRequeridas !== undefined ? s.preguntasRequeridas : (s.cantidadRequerida || s.usar || 0);

                htmlSubcursos += `
                    <tr class="border-b border-slate-50 hover:bg-slate-50/50">
                        <td class="px-6 py-2 pl-10 text-slate-600">${s.nombre || s.subcurso || s.nombreSubcurso}</td>
                        <td class="px-6 py-2 text-center text-slate-400">${enBancoSub}</td>
                        <td class="px-6 py-2 text-center font-medium text-slate-700">${usarSub}</td>
                    </tr>
                `;
            });
            tbodySubcursos.innerHTML = htmlSubcursos;
        } else {
            tbodySubcursos.innerHTML = `<tr><td colspan="3" class="text-center py-4 text-slate-500">Sin subcursos en el prospecto.</td></tr>`;
        }

    } catch (error) {
        console.error("Error cargando configuración:", error);
        if (tbodyComponentes) tbodyComponentes.innerHTML = `<tr><td colspan="3" class="text-center py-4 text-red-500 text-xs">Error al conectar con la base de datos.</td></tr>`;
        if (tbodySubcursos) tbodySubcursos.innerHTML = `<tr><td colspan="3" class="text-center py-4 text-red-500 text-xs">Error al conectar con la base de datos.</td></tr>`;
    }
}

function configurarBotonGenerar() {
    const btnGenerar = document.getElementById('btnGenerarTemas');
    const inputNombre = document.getElementById('txtNombreExamen');
    const inputTemaInicial = document.getElementById('txtTemaInicial');
    const inputCantidad = document.getElementById('txtCantidadTemas');

    const modal = document.getElementById('modalBancoInsuficiente');
    const modalCuerpo = document.getElementById('modalCuerpo');
    const btnCerrarModal = document.getElementById('btnCerrarModalError');

    if (btnCerrarModal && modal && modalCuerpo) {
        btnCerrarModal.addEventListener('click', () => {
            modalCuerpo.classList.add('scale-95', 'opacity-0');
            setTimeout(() => modal.classList.add('hidden'), 200);
        });
    }

    if (!btnGenerar) return;

    btnGenerar.addEventListener('click', async () => {
        const nombreExamen = inputNombre ? inputNombre.value.trim() : "";
        const temaInicial = inputTemaInicial ? inputTemaInicial.value.trim().toUpperCase() : "";
        const cantidadTemas = inputCantidad ? parseInt(inputCantidad.value) : 0;

        if (!nombreExamen) {
            alert("Por favor, ingresa el nombre o proceso del examen.");
            inputNombre.focus();
            return;
        }
        if (!temaInicial || temaInicial.length !== 1 || !/[A-Z]/.test(temaInicial)) {
            alert("Por favor, ingresa una única letra válida como Tema Inicial.");
            return;
        }
        if (isNaN(cantidadTemas) || cantidadTemas <= 0) {
            alert("La cantidad de temas debe ser un número mayor a 0.");
            return;
        }

        const requestData = {
            nombreProceso: nombreExamen,
            nombre: nombreExamen,
            nombreExamen: nombreExamen,
            area: areaSeleccionada.replace('AREA_', ''),
            temaInicial: temaInicial,
            cantidadTemas: cantidadTemas
        };

        const textoOriginal = btnGenerar.innerHTML;
        btnGenerar.disabled = true;
        btnGenerar.innerText = "Generando examen...";

        try {
            const response = await fetch(`${API_EXAMEN_URL}/generar`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData)
            });

            if (response.status === 201 || response.ok) {
                const data = await response.json();
                alert(`¡Éxito! Examen generado correctamente.`);
                localStorage.setItem('ultimoExamenGeneradoId', data.id || data.examenId);
                window.location.href = 'vistaPrevia.html';

            } else if (response.status === 409) {
                const errorData = await response.json();

                if (modal && modalCuerpo) {
                    const infoArea = document.getElementById('modalInfoArea');
                    const mensajeError = document.getElementById('modalMensajeError');
                    const listaFaltantesContenedor = document.getElementById('modalListaDetalleFaltantes');

                    if (infoArea) infoArea.innerText = `Área seleccionada: ${errorData.area || areaSeleccionada}`;
                    if (mensajeError) mensajeError.innerText = errorData.mensaje || "Revisa el déficit de preguntas en el banco:";

                    if (listaFaltantesContenedor) {
                        listaFaltantesContenedor.innerHTML = ''; // Limpiamos contenido anterior

                        const lista = errorData.faltantes;

                        if (Array.isArray(lista) && lista.length > 0) {
                            let htmlLista = '';

                            lista.forEach(item => {
                                const nombreCurso = item.nombre || item.subcurso || item.componente || item.nombreSubcurso || "Materia requerida";
                                const cantidadFaltante = item.faltantes !== undefined ? item.faltantes : (item.cantidad || item.cantidadFaltante || "?");

                                htmlLista += `
                                    <div class="flex items-center justify-between bg-rose-50/50 border border-rose-100 rounded-xl p-3">
                                        <span class="text-sm text-slate-700 font-medium">${nombreCurso}</span>
                                        <span class="text-xs font-bold text-rose-600 bg-rose-100/80 px-2.5 py-1 rounded-md border border-rose-200">
                                            Faltan ${cantidadFaltante}
                                        </span>
                                    </div>
                                `;
                            });
                            listaFaltantesContenedor.innerHTML = htmlLista;
                        } else {
                            listaFaltantesContenedor.innerHTML = `
                                <div class="flex items-center justify-between bg-slate-50 border border-slate-100 rounded-xl p-4">
                                    <span class="text-sm text-slate-500 font-medium">Preguntas faltantes:</span>
                                    <span class="text-xl font-black text-rose-600 bg-rose-50 px-3 py-1 rounded-lg border border-rose-100">${lista}</span>
                                </div>
                            `;
                        }
                    }

                    modal.classList.remove('hidden');
                    setTimeout(() => {
                        modalCuerpo.classList.remove('scale-95', 'opacity-0');
                    }, 10);
                } else {
                    alert(`No se pudo generar:\n${errorData.mensaje}`);
                }

            } else {
                const errorData = await response.json();
                alert(`Error (${response.status}): ${errorData.mensaje || 'Error interno del servidor.'}`);
            }

        } catch (error) {
            console.error("Error en la petición:", error);
            alert("No se pudo conectar con el servidor. Asegúrate de que el backend esté corriendo.");
        } finally {
            btnGenerar.disabled = false;
            btnGenerar.innerHTML = textoOriginal;
        }
    });
}


function inicializarContadoresInteractivos() {
    const inputTemaInicial = document.getElementById('txtTemaInicial');
    const inputCantidad = document.getElementById('txtCantidadTemas');

    const indicadorCantidad = document.getElementById('indicadorCantidadTemas');
    const indicadorInicial = document.getElementById('indicadorTemaInicial');
    const indicadorResultantes = document.getElementById('indicadorTemasResultantes');

    function recalcularLetras() {
        let letra = inputTemaInicial.value.trim().toUpperCase();
        let cant = parseInt(inputCantidad.value);

        if (!letra || isNaN(cant) || cant <= 0) return;

        if (indicadorCantidad) indicadorCantidad.innerText = cant;
        if (indicadorInicial) indicadorInicial.innerText = letra;

        let letras = [];
        let codigoBase = letra.charCodeAt(0);

        for (let i = 0; i < cant; i++) {
            let code = codigoBase + i;
            if (code > 90) code = 65 + (code - 91);
            letras.push(String.fromCharCode(code));
        }

        if (indicadorResultantes) {
            indicadorResultantes.innerText = letras.join(', ');
        }
    }

    if (inputTemaInicial && inputCantidad) {
        inputTemaInicial.addEventListener('input', recalcularLetras);
        inputCantidad.addEventListener('input', recalcularLetras);

        document.getElementById('btnIncrementarTemas')?.addEventListener('click', () => {
            inputCantidad.value = parseInt(inputCantidad.value) + 1;
            recalcularLetras();
        });
        document.getElementById('btnDecrementarTemas')?.addEventListener('click', () => {
            if (parseInt(inputCantidad.value) > 1) {
                inputCantidad.value = parseInt(inputCantidad.value) - 1;
                recalcularLetras();
            }
        });
    }
}