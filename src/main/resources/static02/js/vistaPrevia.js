// --- ACTIVAR ITEM DEL SIDEBAR SEGÚN LA PÁGINA ACTUAL ---
const paginaActual = window.location.pathname.split("/").pop() || "index.html";
const enlacesSidebar = document.querySelectorAll("#sidebar nav a");

enlacesSidebar.forEach(enlace => {
    const rutaEnlace = enlace.getAttribute("href");

    if (paginaActual === rutaEnlace) {
        // Estilos para el enlace ACTIVO (Azul brillante)
        enlace.className = "flex items-center px-3 py-2.5 bg-blue-600 text-white rounded-lg group font-medium text-sm transition-colors";
    } else {
        // Estilos para los enlaces INACTIVOS (Gris/Azul oscuro)
        // Excepto el de Vista Previa si sigue bloqueado, claro
        if (enlace.id !== "linkVistaPrevia") {
            enlace.className = "flex items-center px-3 py-2.5 text-slate-300 hover:bg-white/10 hover:text-white rounded-lg group font-medium text-sm transition-colors";
        }
    }
});
// --------------------------------------------------------

const API_BASE_URL = '/api/examenes';


let preguntasExamen = [];
let mostrandoClaves = false;

// Variables de estado dinámicas
let examenIdActual = localStorage.getItem('examenIdActual');
let temaActual = localStorage.getItem('temaSeleccionado') || 'B';
let cantidadTemas = parseInt(localStorage.getItem('cantidadTemasGenerados')) || 1;
let listaTemas = [];
let indiceTemaActual = 0;

// Captura de Elementos del DOM
const contenedorPreguntas = document.getElementById('contenedorPreguntas');
const txtAreaHeader = document.getElementById('txtAreaHeader');
const txtTemaHeader = document.getElementById('txtTemaHeader');
const txtContadorPreguntas = document.getElementById('txtContadorPreguntas');
const txtSubtituloExamen = document.getElementById('txtSubtituloExamen');
const btnVerClaves = document.getElementById('btnVerClaves');
const linkVistaPrevia = document.getElementById('linkVistaPrevia'); // Enlace del Sidebar
const btnFinalizarProceso = document.getElementById('btnFinalizarProceso'); // Botón de terminar

const txtAreaResumen = document.getElementById('txtAreaResumen');
const txtTemaResumen = document.getElementById('txtTemaResumen');
const txtTotalPreguntasResumen = document.getElementById('txtTotalPreguntasResumen');
const badgePdfExamen = document.getElementById('badgePdfExamen');
const badgePdfClaves = document.getElementById('badgePdfClaves');
const contenedorNavegacionRapida = document.getElementById('contenedorNavegacionRapida');

// Referencias de botones de navegación
let btnTemaAnterior;
let btnTemaSiguiente;

document.addEventListener('DOMContentLoaded', () => {
    // 1. Control de acceso y activación del Sidebar (Opción B)
    gestionarAccesoSidebar();

    // 2. Inicializamos la lista de temas calculada
    calcularListaTemas();

    // 3. Mapeamos de forma segura los botones de la interfaz
    configurarIDsBotonesNavegacion();

    // 4. Si hay un examen en memoria, lo descargamos de inmediato
    if (examenIdActual) {
        cargarExamenGenerado(examenIdActual, temaActual);
    } else {
        contenedorPreguntas.innerHTML = `
            <div class="text-center py-12 text-amber-600 font-medium bg-amber-50 rounded-xl p-6 border border-amber-200">
                ⚠️ No hay ningún examen activo en esta sesión. Por favor, ve a la sección "Generar examen" en el menú lateral para iniciar.
            </div>
        `;
    }

    // 5. Activamos los escuchadores de eventos
    configurarBotonesAccion();
});

// Opción B: Controla si el botón del Sidebar está encendido o apagado
function gestionarAccesoSidebar() {
    if (linkVistaPrevia) {
        if (examenIdActual) {
            linkVistaPrevia.classList.remove('pointer-events-none', 'opacity-40');
        } else {
            linkVistaPrevia.classList.add('pointer-events-none', 'opacity-40');
        }
    }
}

function calcularListaTemas() {
    listaTemas = [];
    let letraInicial = localStorage.getItem('temaInicialFormulario') || 'B';
    let codigoBase = letraInicial.charCodeAt(0);

    for (let i = 0; i < cantidadTemas; i++) {
        let code = codigoBase + i;
        if (code > 90) code = 65 + (code - 91);
        listaTemas.push(String.fromCharCode(code));
    }

    indiceTemaActual = listaTemas.indexOf(temaActual) !== -1 ? listaTemas.indexOf(temaActual) : 0;
}

function configurarIDsBotonesNavegacion() {
    const barraBotones = document.querySelectorAll('button');
    barraBotones.forEach(btn => {
        const texto = btn.textContent.toLowerCase();
        if (texto.includes('anterior')) {
            btn.id = 'btnTemaAnterior';
            btnTemaAnterior = btn;
        }
        if (texto.includes('siguiente')) {
            btn.id = 'btnTemaSiguiente';
            btnTemaSiguiente = btn;
        }
    });
}

// Obtener la data del Backend de forma limpia
async function cargarExamenGenerado(examenId, letraTema) {
    try {
        contenedorPreguntas.innerHTML = `<div class="text-center py-12 text-slate-400 italic">Cargando el Tema ${letraTema}...</div>`;

        const response = await fetch(`${API_BASE_URL}/${examenId}/temas/${letraTema}/vista-previa`, {
            method: 'GET'
        });

        if (!response.ok) throw new Error("No se pudo recuperar el examen generado.");

        const data = await response.json();

        const areaTexto = data.area || 'A';
        const temaTexto = data.tema || letraTema;
        const nombreExamenTexto = data.nombreExamen || 'Examen Generado';
        const totalPreguntasTexto = data.totalPreguntas !== undefined ? data.totalPreguntas : 0;

        if(txtAreaHeader) txtAreaHeader.innerText = areaTexto.replace("AREA_", "");
        if(txtTemaHeader) txtTemaHeader.innerText = temaTexto;
        if(txtSubtituloExamen) txtSubtituloExamen.innerText = `${nombreExamenTexto} - Área ${areaTexto.replace("AREA_", "")} - Tema ${temaTexto}`;
        if(txtContadorPreguntas) txtContadorPreguntas.innerText = `Preguntas: ${totalPreguntasTexto}`;

        // Sincronización del panel resumen derecho
        if(txtAreaResumen) txtAreaResumen.innerText = areaTexto.replace("AREA_", "");
        if(txtTemaResumen) txtTemaResumen.innerText = temaTexto;
        if(txtTotalPreguntasResumen) txtTotalPreguntasResumen.innerText = totalPreguntasTexto;

        // NUEVO: Sincronización de los badges leyendo el historial persistente de descargas
        const llaveExamenExamen = `descargado_examen_${examenIdActual}_${letraTema}`;
        const llaveExamenClaves = `descargado_claves_${examenIdActual}_${letraTema}`;

        if(badgePdfExamen) {
            if (localStorage.getItem(llaveExamenExamen) === 'true') {
                badgePdfExamen.innerText = "listo";
                badgePdfExamen.className = "bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs font-semibold";
            } else {
                badgePdfExamen.innerText = "pendiente";
                badgePdfExamen.className = "bg-slate-100 text-slate-600 px-2 py-0.5 rounded text-xs font-semibold";
            }
        }

        if(badgePdfClaves) {
            if (localStorage.getItem(llaveExamenClaves) === 'true') {
                badgePdfClaves.innerText = "listo";
                badgePdfClaves.className = "bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs font-semibold";
            } else {
                badgePdfClaves.innerText = "pendiente";
                badgePdfClaves.className = "bg-slate-100 text-slate-600 px-2 py-0.5 rounded text-xs font-semibold";
            }
        }

        preguntasExamen = data.preguntas || [];
        renderizarPreguntas();
        actualizarVisibilidadBotones();
    } catch (error) {
        console.error(error);
        contenedorPreguntas.innerHTML = `
            <div class="text-center py-12 text-red-500 font-medium">
                ⚠️ Error al cargar la vista previa del examen. Verifica que el examen ID: ${examenId} con Tema ${letraTema} exista.
            </div>
        `;
    }
}

// Renderizar dinámicamente las preguntas
function renderizarPreguntas() {
    if (!preguntasExamen || preguntasExamen.length === 0) {
        contenedorPreguntas.innerHTML = `<p class="text-slate-400 italic text-center py-8">El examen no contiene preguntas vigentes.</p>`;
        return;
    }

    contenedorPreguntas.innerHTML = '';

    const estilosComponentes = {
        'CTA': 'bg-blue-100 text-blue-700',
        'MATEMATICA': 'bg-green-100 text-green-700',
        'HUMANIDADES': 'bg-amber-100 text-amber-700',
        'RAZONAMIENTO_VERBAL': 'bg-purple-100 text-purple-700',
        'RAZONAMIENTO_MATEMATICO': 'bg-indigo-100 text-indigo-700'
    };

    preguntasExamen.forEach((p, index) => {
        const divPregunta = document.createElement('div');
        divPregunta.id = `pregunta-num-${index + 1}`;
        divPregunta.className = `flex items-start p-4 rounded-lg transition-all duration-500 border-b border-slate-100 ${mostrandoClaves ? 'bg-slate-50/60' : ''}`;

        const badgeClass = estilosComponentes[p.componente] || 'bg-slate-100 text-slate-700';
        const labelComponente = p.nombreComponente || p.componente;

        let alternativasHTML = '';
        const letras = ['A', 'B', 'C', 'D', 'E'];

        if (p.alternativas && p.alternativas.length > 0) {
            p.alternativas.forEach((alt, i) => {
                const letra = letras[i] || '-';
                const esCorrecta = (letra === p.respuestaCorrecta);

                const estiloLetra = (mostrandoClaves && esCorrecta) ? 'font-bold text-green-600' : 'font-semibold';
                const estiloTexto = (mostrandoClaves && esCorrecta) ? 'text-green-700 font-medium bg-green-50 px-1.5 py-0.5 rounded' : '';

                alternativasHTML += `
                    <p class="${estiloTexto}"><span class="${estiloLetra} mr-2">${letra}.</span>${alt.texto || alt}</p>
                `;
            });
        }

        divPregunta.innerHTML = `
            <div class="w-8 h-8 shrink-0 rounded-full bg-slate-200 text-slate-700 font-bold flex items-center justify-center mr-4">
                ${index + 1}
            </div>
            <div class="flex-1">
                <div class="flex justify-between items-start mb-3">
                    <div class="flex flex-col gap-2">
                        <p class="text-slate-800 font-medium leading-relaxed">${p.enunciado}</p>
                        ${p.imagenUrl ? `<img src="http://localhost:8080${p.imagenUrl}" class="max-h-48 object-contain rounded-lg border mt-2 self-start" alt="Imagen adjunta">` : ''}
                    </div>
                    <span class="${badgeClass} text-xs font-semibold px-2 py-1 rounded ml-4 whitespace-nowrap">${labelComponente}</span>
                </div>
                <div class="space-y-2 text-sm text-slate-700">
                    ${alternativasHTML}
                </div>
            </div>
        `;
        contenedorPreguntas.appendChild(divPregunta);
    });
    generarMatrizNavegacion();
}

function actualizarVisibilidadBotones() {
    if (btnTemaAnterior) {
        btnTemaAnterior.disabled = (indiceTemaActual === 0);
        btnTemaAnterior.style.opacity = (indiceTemaActual === 0) ? '0.4' : '1';
    }
    if (btnTemaSiguiente) {
        btnTemaSiguiente.disabled = (indiceTemaActual === listaTemas.length - 1);
        btnTemaSiguiente.style.opacity = (indiceTemaActual === listaTemas.length - 1) ? '0.4' : '1';
    }
}

// Manejo de Eventos
function configurarBotonesAccion() {
    if (btnTemaAnterior) {
        btnTemaAnterior.onclick = () => {
            if (indiceTemaActual > 0) {
                indiceTemaActual--;
                temaActual = listaTemas[indiceTemaActual];
                localStorage.setItem('temaSeleccionado', temaActual);
                cargarExamenGenerado(examenIdActual, temaActual);
            }
        };
    }

    if (btnTemaSiguiente) {
        btnTemaSiguiente.onclick = () => {
            if (indiceTemaActual < listaTemas.length - 1) {
                indiceTemaActual++;
                temaActual = listaTemas[indiceTemaActual];
                localStorage.setItem('temaSeleccionado', temaActual);
                cargarExamenGenerado(examenIdActual, temaActual);
            }
        };
    }

    if (btnVerClaves) {
        btnVerClaves.onclick = () => {
            mostrandoClaves = !mostrandoClaves;
            if (mostrandoClaves) {
                btnVerClaves.innerHTML = `
                    <svg class="w-4 h-4 mr-2 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"></path>
                        <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.523 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"></path>
                    </svg Ocultar claves
                `;
            } else {
                btnVerClaves.innerHTML = `
                    <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                    </svg> Ver claves
                `;
            }
            renderizarPreguntas();
        };
    }

    // ACCIÓN DE FINALIZACIÓN
    if (btnFinalizarProceso) {
        btnFinalizarProceso.onclick = () => {
            const confirmar = confirm("¿Estás seguro de que deseas finalizar este proceso? Esto limpiará la vista previa hasta que generes un nuevo examen.");
            if (confirmar) {
                // NUEVO: Limpiar las llaves del historial de descargas al terminar todo el flujo
                Object.keys(localStorage).forEach(key => {
                    if (key.startsWith('descargado_examen_') || key.startsWith('descargado_claves_')) {
                        localStorage.removeItem(key);
                    }
                });

                localStorage.removeItem('examenIdActual');
                localStorage.removeItem('temaSeleccionado');
                localStorage.removeItem('temaInicialFormulario');
                localStorage.removeItem('cantidadTemasGenerados');

                window.location.href = 'generarExamen.html';
            }
        };
    }

    // CONEXIÓN REAL: Descargar PDF del Examen (POST -> GET)
    const btnDescargarExamen = document.getElementById('btnDescargarExamen');
    if (btnDescargarExamen && examenIdActual) {
        btnDescargarExamen.onclick = async () => {
            try {
                btnDescargarExamen.disabled = true;
                const textoOriginal = btnDescargarExamen.innerHTML;
                btnDescargarExamen.innerHTML = `⏳ Generando...`;

                const urlGenerar = `${API_BASE_URL}/${examenIdActual}/temas/${temaActual}/pdf/examen`;
                const response = await fetch(urlGenerar, { method: 'POST' });

                if (!response.ok) throw new Error("Error al mandar a generar el PDF del examen.");

                const dataResponse = await response.json();
                const nombreArchivoGenerado = dataResponse.nombreArchivo;

                if (!nombreArchivoGenerado) {
                    throw new Error("El backend no devolvió el nombre del archivo generado.");
                }

                if (badgePdfExamen) {
                    badgePdfExamen.innerText = "listo";
                    badgePdfExamen.className = "bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs font-semibold";
                }

                // NUEVO: Guardar persistentemente que este examen/tema ya se descargó
                localStorage.setItem(`descargado_examen_${examenIdActual}_${temaActual}`, 'true');

                // Paso 3: Descarga real segura mediante un ancla oculta (Evita romper el contexto CORS)
                const linkDescarga = document.createElement('a');
                linkDescarga.href = `${API_BASE_URL}/pdf/descargar/${nombreArchivoGenerado}`;
                linkDescarga.setAttribute('download', nombreArchivoGenerado);
                document.body.appendChild(linkDescarga);
                linkDescarga.click();
                document.body.removeChild(linkDescarga);

                btnDescargarExamen.innerHTML = textoOriginal;
                btnDescargarExamen.disabled = false;

            } catch (error) {
                console.error(error);
                alert("⚠️ " + error.message);
                btnDescargarExamen.disabled = false;
            }
        };
    }

    // CONEXIÓN REAL: Descargar PDF de Claves (POST -> GET)
    const btnDescargarClaves = document.getElementById('btnDescargarClaves');
    if (btnDescargarClaves && examenIdActual) {
        btnDescargarClaves.onclick = async () => {
            try {
                btnDescargarClaves.disabled = true;
                const textoOriginal = btnDescargarClaves.innerHTML;
                btnDescargarClaves.innerHTML = `⏳ Generando...`;

                const urlGenerar = `${API_BASE_URL}/${examenIdActual}/temas/${temaActual}/pdf/clave`;
                const response = await fetch(urlGenerar, { method: 'POST' });

                if (!response.ok) throw new Error("Error al mandar a generar el PDF de claves.");

                const dataResponse = await response.json();
                const nombreArchivoGenerado = dataResponse.nombreArchivo;

                if (!nombreArchivoGenerado) {
                    throw new Error("El backend no devolvió el nombre del archivo generado.");
                }

                if (badgePdfClaves) {
                    badgePdfClaves.innerText = "listo";
                    badgePdfClaves.className = "bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs font-semibold";
                }

                // NUEVO: Guardar persistentemente que estas claves ya se descargaron
                localStorage.setItem(`descargado_claves_${examenIdActual}_${temaActual}`, 'true');

                // Paso 3: Descarga real segura mediante un ancla oculta (Evita romper el contexto CORS)
                const linkDescarga = document.createElement('a');
                linkDescarga.href = `${API_BASE_URL}/pdf/descargar/${nombreArchivoGenerado}`;
                linkDescarga.setAttribute('download', nombreArchivoGenerado);
                document.body.appendChild(linkDescarga);
                linkDescarga.click();
                document.body.removeChild(linkDescarga);

                btnDescargarClaves.innerHTML = textoOriginal;
                btnDescargarClaves.disabled = false;

            } catch (error) {
                console.error(error);
                alert("⚠️ " + error.message);
                btnDescargarClaves.disabled = false;
            }
        };
    }
}

// Genera los botones numéricos 1, 2, 3... según la cantidad real de preguntas
function generarMatrizNavegacion() {
    if (!contenedorNavegacionRapida) return;
    contenedorNavegacionRapida.innerHTML = '';

    if (preguntasExamen.length === 0) {
        contenedorNavegacionRapida.innerHTML = '<span class="text-xs text-slate-400 italic col-span-5 text-center">Sin preguntas</span>';
        return;
    }

    preguntasExamen.forEach((_, index) => {
        const numeroPregunta = index + 1;
        const btnNumero = document.createElement('button');
        btnNumero.className = "h-8 rounded bg-slate-100 text-slate-700 hover:bg-[#0A1931] hover:text-white font-medium text-xs flex items-center justify-center transition border border-slate-200/60 shadow-sm";
        btnNumero.innerText = numeroPregunta;

        btnNumero.onclick = () => {
            const elementoPregunta = document.getElementById(`pregunta-num-${numeroPregunta}`);
            if (elementoPregunta) {
                elementoPregunta.scrollIntoView({ behavior: 'smooth', block: 'center' });
                elementoPregunta.classList.add('bg-yellow-50', 'ring-2', 'ring-amber-300');

                setTimeout(() => {
                    elementoPregunta.classList.remove('bg-yellow-50', 'ring-2', 'ring-amber-300');
                }, 1200);
            }
        };

        contenedorNavegacionRapida.appendChild(btnNumero);
    });
}