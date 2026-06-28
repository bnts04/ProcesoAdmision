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

// Variable global para almacenar la lista original del servidor
let todosLosExamenes = [];

document.addEventListener('DOMContentLoaded', () => {
    gestionarAccesoSidebar();
    obtenerHistorialExamenes();

    // Configurar los escuchadores de eventos para los filtros
    configurarFiltros();
});

function gestionarAccesoSidebar() {
    const linkVistaPrevia = document.getElementById('linkVistaPrevia');
    const examenIdActual = localStorage.getItem('examenIdActual');
    if (linkVistaPrevia) {
        if (examenIdActual) {
            linkVistaPrevia.classList.remove('pointer-events-none', 'opacity-40');
        } else {
            linkVistaPrevia.classList.add('pointer-events-none', 'opacity-40');
        }
    }
}

async function obtenerHistorialExamenes() {
    const tbody = document.getElementById('tablaHistorial');
    if (!tbody) return;

    try {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center py-8 text-slate-400 italic">Cargando historial de generaciones...</td>
            </tr>
        `;

        const response = await fetch(`${API_BASE_URL}/historial`);
        if (!response.ok) throw new Error("No se pudo recuperar el historial.");

        // Guardamos los datos en nuestra lista global
        todosLosExamenes = await response.json();

        if (todosLosExamenes.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center py-8 text-slate-500 font-medium bg-slate-50">
                        📁 No se encontraron exámenes generados en el historial.
                    </td>
                </tr>
            `;
            actualizarTarjetasResumen([]);
            return;
        }

        // Renderizamos la lista completa inicialmente
        renderizarTabla(todosLosExamenes);
        actualizarTarjetasResumen(todosLosExamenes);

    } catch (error) {
        console.error(error);
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center py-8 text-red-500 font-medium bg-red-50">
                    ⚠️ Error de conexión con el servidor al cargar el historial.
                </td>
            </tr>
        `;
    }
}

// Nueva función modular encargada exclusivamente de pintar las filas en pantalla
function renderizarTabla(listaExamenes) {
    const tbody = document.getElementById('tablaHistorial');
    if (!tbody) return;

    if (listaExamenes.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center py-8 text-slate-400 italic bg-slate-50/50">
                    🔍 No hay registros que coincidan con los filtros aplicados.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = '';
    listaExamenes.forEach(ex => {
        const fila = document.createElement('tr');
        fila.className = "border-b border-slate-50 hover:bg-slate-50 transition text-slate-600";

        const temasTexto = Array.isArray(ex.temas) ? ex.temas.join('-') : '-';
        const fechaFormateada = ex.fechaGeneracion ? new Date(ex.fechaGeneracion).toLocaleDateString('es-PE') : '-';

        let estadoBadge = `<span class="bg-green-100 text-green-700 px-2 py-1 rounded text-xs font-semibold">Generado</span>`;
        if (ex.estado === 'PENDIENTE') {
            estadoBadge = `<span class="bg-orange-100 text-orange-700 px-2 py-1 rounded text-xs font-semibold">Pendiente</span>`;
        }

        const primerTema = Array.isArray(ex.temas) && ex.temas.length > 0 ? ex.temas[0] : 'A';
        const totalTemas = Array.isArray(ex.temas) ? ex.temas.length : 1;

        fila.innerHTML = `
            <td class="px-6 py-4 font-medium text-slate-800">GEN-${ex.examenId}</td>
            <td class="px-6 py-4 max-w-xs truncate" title="${ex.nombreExamen}">${ex.nombreExamen || 'Examen de Admisión'}</td>
            <td class="px-6 py-4">${ex.nombreArea || (ex.area ? ex.area.replace("AREA_", "Área ") : '-')}</td>
            <td class="px-6 py-4 text-center font-medium text-slate-700">${temasTexto}</td>
            <td class="px-6 py-4 text-center">${fechaFormateada}</td>
            <td class="px-6 py-4 text-center">${estadoBadge}</td>
            <td class="px-6 py-4 text-center">
                <button onclick="descargarPdfHistorial(this, ${ex.examenId}, '${primerTema}', 'examen')"
                        class="bg-blue-600 text-white px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-blue-700 flex items-center mx-auto shadow-sm transition">
                    Descargar <svg class="w-3 h-3 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>
                </button>
            </td>
            <td class="px-6 py-4 text-center">
                <button onclick="descargarPdfHistorial(this, ${ex.examenId}, '${primerTema}', 'clave')"
                        class="bg-blue-600 text-white px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-blue-700 flex items-center mx-auto shadow-sm transition">
                    Descargar <svg class="w-3 h-3 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>
                </button>
            </td>
            <td class="px-6 py-4 text-center">
                <button onclick="irAVistaPreviaExistente(${ex.examenId}, '${primerTema}', ${totalTemas})"
                        class="border border-blue-600 text-blue-600 px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-blue-50 flex items-center mx-auto transition">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg> Ver
                </button>
            </td>
        `;
        tbody.appendChild(fila);
    });
}

// NUEVO: Escuchador de los controles de la interfaz
function configurarFiltros() {
    const inputBusqueda = document.getElementById('inputBusqueda');
    const filtroArea = document.getElementById('filtroArea');
    const filtroEstado = document.getElementById('filtroEstado');

    const ejecutarFiltro = () => {
        const textoBusqueda = inputBusqueda.value.toLowerCase().trim();
        const areaSeleccionada = filtroArea.value;
        const estadoSeleccionado = filtroEstado.value;

        // Filtrar el arreglo global en caliente
        const examenesFiltrados = todosLosExamenes.filter(ex => {
            // 1. Filtro por término de texto (ID o Nombre Examen)
            const coincideId = `GEN-${ex.examenId}`.toLowerCase().includes(textoBusqueda);
            const coincideNombre = ex.nombreExamen ? ex.nombreExamen.toLowerCase().includes(textoBusqueda) : false;
            const pasaBusqueda = textoBusqueda === "" || coincideId || coincideNombre;

            // 2. Filtro por Selector de Área
            const pasaArea = areaSeleccionada === "" || ex.area === areaSeleccionada;

            // 3. Filtro por Selector de Estado
            const pasaEstado = estadoSeleccionado === "" || ex.estado === estadoSeleccionado;

            return pasaBusqueda && pasaArea && pasaEstado;
        });

        // Volver a renderizar la tabla con el subgrupo filtrado
        renderizarTabla(examenesFiltrados);
    };

    // Agregar listeners para que reaccionen inmediatamente al cambio
    if (inputBusqueda) inputBusqueda.addEventListener('input', ejecutarFiltro);
    if (filtroArea) filtroArea.addEventListener('change', ejecutarFiltro);
    if (filtroEstado) filtroEstado.addEventListener('change', ejecutarFiltro);
}

function actualizarTarjetasResumen(examenes) {
    const txtExamenes = document.getElementById('contadorExamenes');
    const txtTemas = document.getElementById('contadorTemas');
    const txtPdfExamenes = document.getElementById('contadorPdfExamenes');
    const txtPdfClaves = document.getElementById('contadorPdfClaves');

    if (!txtExamenes) return;

    let totalExamenes = examenes.length;
    let totalTemas = 0;
    let totalPdfExamenesHechos = 0;
    let totalPdfClavesHechas = 0;

    examenes.forEach(ex => {
        totalTemas += Array.isArray(ex.temas) ? ex.temas.length : 0;
        if (ex.pdfExamenDisponible) totalPdfExamenesHechos++;
        if (ex.pdfClaveDisponible) totalPdfClavesHechas++;
    });

    txtExamenes.innerText = totalExamenes;
    txtTemas.innerText = totalTemas;
    txtPdfExamenes.innerText = totalPdfExamenesHechos;
    txtPdfClaves.innerText = totalPdfClavesHechas;
}

async function descargarPdfHistorial(boton, examenId, tema, tipo) {
    try {
        boton.disabled = true;
        const textoOriginal = boton.innerHTML;
        boton.innerHTML = `⏳...`;

        const urlGenerar = `${API_BASE_URL}/${examenId}/temas/${tema}/pdf/${tipo}`;
        const response = await fetch(urlGenerar, { method: 'POST' });

        if (!response.ok) throw new Error(`Error al generar el archivo del ${tipo}.`);

        const dataResponse = await response.json();
        const nombreArchivoGenerado = dataResponse.nombreArchivo;

        if (!nombreArchivoGenerado) throw new Error("El backend no devolvió el nombre de archivo.");

        const linkDescarga = document.createElement('a');
        linkDescarga.href = `${API_BASE_URL}/pdf/descargar/${nombreArchivoGenerado}`;
        linkDescarga.setAttribute('download', nombreArchivoGenerado);
        document.body.appendChild(linkDescarga);
        linkDescarga.click();
        document.body.removeChild(linkDescarga);

        boton.innerHTML = textoOriginal;
        boton.disabled = false;

    } catch (error) {
        console.error(error);
        alert("⚠️ " + error.message);
        boton.innerHTML = `Descargar`;
        boton.disabled = false;
    }
}

function irAVistaPreviaExistente(examenId, primerTema, totalTemas) {
    localStorage.setItem('examenIdActual', examenId);
    localStorage.setItem('temaSeleccionado', primerTema);
    localStorage.setItem('temaInicialFormulario', primerTema);
    localStorage.setItem('cantidadTemasGenerados', totalTemas);
    window.location.href = 'vistaPrevia.html';
}