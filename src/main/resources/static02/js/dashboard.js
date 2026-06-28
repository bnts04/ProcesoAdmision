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

// Forzamos la URL completa para descartar problemas de rutas
const API_BASE = 'http://localhost:8080';
const API_CONFIG_URL = `${API_BASE}/api/areas-examen/AREA_A/configuracion`;
const API_BANCO_URL = `${API_BASE}/api/banco-preguntas`;

let componentesGlobal = [];

const subcursosPorComponente = {
  'CTA': [
    { value: 'FISICA', label: 'Física' },
    { value: 'QUIMICA', label: 'Química' },
    { value: 'BIOLOGIA', label: 'Biología' }
  ],
  'MATEMATICA': [
    { value: 'ALGEBRA', label: 'Álgebra' },
    { value: 'ARITMETICA', label: 'Aritmética' },
    { value: 'GEOMETRIA', label: 'Geometría' },
    { value: 'TRIGONOMETRIA', label: 'Trigonometría' }
  ],
  'HUMANIDADES': [
    { value: 'HISTORIA', label: 'Historia' },
    { value: 'LENGUAJE', label: 'Lenguaje' },
    { value: 'LITERATURA', label: 'Literatura' },
    { value: 'ECONOMIA', label: 'Economía' },
    { value: 'EDUCACION_CIVICA', label: 'Educación Cívica' },
    { value: 'PSICOLOGIA', label: 'Psicología' },
    { value: 'GEOGRAFIA', label: 'Geografía' }
  ],
  'RAZONAMIENTO_VERBAL': [
    { value: 'RAZONAMIENTO_VERBAL', label: 'Razonamiento Verbal' }
  ],
  'RAZONAMIENTO_MATEMATICO': [
    { value: 'RAZONAMIENTO_MATEMATICO', label: 'Razonamiento Matemático' }
  ]
};

// MODIFICACIÓN: Forzamos la ejecución inmediata pase lo que pase
window.onload = function() {
    console.log("🚀 El script se ha despertado exitosamente.");
    cargarDatosDashboard();
};

async function cargarDatosDashboard() {
    const kpiTotal = document.getElementById('kpi-total-count');
    const contenedorKpis = document.getElementById('contenedor-kpis-componentes');
    const tbodyComponentes = document.getElementById('tabla-resumen-componentes');

    try {
        console.log("📡 Conectando a:", API_CONFIG_URL);
        const response = await fetch(API_CONFIG_URL);
        console.log("🔹 Estado de respuesta del servidor:", response.status);

        if (!response.ok) throw new Error(`Error del servidor: ${response.status}`);

        const data = await response.json();
        console.log("📦 Datos JSON recibidos:", data);

        if (!data || !data.resumenComponentes || data.resumenComponentes.length === 0) {
            if (tbodyComponentes) {
                tbodyComponentes.innerHTML = `<tr><td colspan="3" class="text-center py-6 text-slate-500 font-medium">📂 Base de datos vacía o sin componentes.</td></tr>`;
            }
            if (kpiTotal) kpiTotal.innerText = "0";
            return;
        }

        componentesGlobal = data.resumenComponentes.map(c => {
            return {
                codigo: c.codigo || c.nombre?.substring(0, 3).toUpperCase() || "COMP",
                nombre: c.nombre || c.componente || c.nombreComponente,
                cantidad: c.preguntasDisponibles !== undefined ? c.preguntasDisponibles : (c.cantidadDisponible || c.enBanco || 0)
            };
        });

        const totalBanco = componentesGlobal.reduce((sum, c) => sum + c.cantidad, 0);
        if (kpiTotal) kpiTotal.innerText = totalBanco;

        let htmlKpis = '';
        let htmlTabla = '';

        componentesGlobal.forEach(comp => {
            htmlKpis += `
                <div class="bg-white p-5 rounded-xl shadow-sm border border-slate-200">
                    <h3 class="text-blue-600 font-bold text-xs tracking-wider uppercase mb-3">${comp.codigo}</h3>
                    <div class="flex justify-between items-end">
                        <div>
                            <p class="text-3xl font-bold text-slate-800">${comp.cantidad}</p>
                            <p class="text-[11px] text-slate-400 truncate max-w-[120px]" title="${comp.nombre}">${comp.nombre}</p>
                        </div>
                    </div>
                </div>
            `;

            htmlTabla += `
                <tr class="bg-white border-b border-slate-100 hover:bg-slate-50/50">
                    <td class="px-6 py-4 font-medium text-slate-900">
                        <div class="font-semibold text-slate-700">${comp.nombre}</div>
                        <span class="text-[10px] bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded font-mono uppercase">${comp.codigo}</span>
                    </td>
                    <td class="px-6 py-4 text-center font-bold text-slate-600">${comp.cantidad}</td>
                    <td class="px-6 py-4 text-center">
                        <button onclick="mostrarDetalleSubcursos('${comp.nombre}')"
                                class="bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold px-3 py-1.5 rounded-lg">
                            Ver detalle
                        </button>
                    </td>
                </tr>
            `;
        });

        // MODIFICACIÓN DE SEGURIDAD: Evitamos el conflicto con la clase 'contents' de Tailwind
        if (contenedorKpis) {
            contenedorKpis.classList.remove('contents');
            contenedorKpis.classList.add('grid', 'grid-cols-1', 'sm:grid-cols-2', 'md:grid-cols-4', 'gap-4', 'col-span-full');
            contenedorKpis.innerHTML = htmlKpis;
        }
        if (tbodyComponentes) tbodyComponentes.innerHTML = htmlTabla;

        if (componentesGlobal.length > 0) {
            filtrarYMostrarSubcursos(componentesGlobal[0].nombre, componentesGlobal[0].codigo);
        }

    } catch (error) {
        console.error("❌ Error atrapado en catch del Dashboard:", error);
        if (tbodyComponentes) {
            tbodyComponentes.innerHTML = `<tr><td colspan="3" class="text-center py-4 text-red-500 text-xs font-medium">⚠️ Error: ${error.message}</td></tr>`;
        }
    }
}

// ... Las funciones mostrarDetalleSubcursos y filtrarYMostrarSubcursos se quedan exactamente igual abajo ...

async function mostrarDetalleSubcursos(nombreComponente) {
    try {
        const compObj = componentesGlobal.find(c => c.nombre === nombreComponente);
        const codigoComponente = compObj ? compObj.codigo : "";
        filtrarYMostrarSubcursos(nombreComponente, codigoComponente);
    } catch (e) {
        console.error("Error al refrescar subcursos", e);
    }
}

async function filtrarYMostrarSubcursos(nombreComponente, codigoComponente) {
     const panelDerecho = document.getElementById('panel-detalle-subcursos');
     if (!panelDerecho) return;

     panelDerecho.innerHTML = `
         <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-8 text-center text-slate-500 text-xs">
             <span class="animate-pulse">Consultando cantidades reales en la base de datos...</span>
         </div>
     `;

     let htmlSubcursosRows = '';
     let sumaTotalReal = 0;

     const codigoLimpio = (codigoComponente || "").toUpperCase().trim();
     const nombreLimpio = (nombreComponente || "").toUpperCase().trim();

     let claveDiccionario = '';
     if (subcursosPorComponente[codigoLimpio]) {
         claveDiccionario = codigoLimpio;
     } else if (nombreLimpio.includes('MATEMÁTICA') || nombreLimpio.includes('MATEMATICA')) {
         claveDiccionario = 'MATEMATICA';
     } else if (nombreLimpio.includes('CTA') || nombreLimpio.includes('CIENCIA')) {
         claveDiccionario = 'CTA';
     } else if (nombreLimpio.includes('HUMANIDADES')) {
         claveDiccionario = 'HUMANIDADES';
     } else if (nombreLimpio.includes('VERBAL')) {
         claveDiccionario = 'RAZONAMIENTO_VERBAL';
     } else if (nombreLimpio.includes('MATEMÁTICO') || nombreLimpio.includes('MATEMATICO')) {
         claveDiccionario = 'RAZONAMIENTO_MATEMATICO';
     }

     const listaSubcursosEstaticos = subcursosPorComponente[claveDiccionario] || [];

     if (listaSubcursosEstaticos.length > 0) {
         try {
             for (const sub of listaSubcursosEstaticos) {
                 let cantidadReal = 0;
                 const response = await fetch(`${API_BANCO_URL}?subcurso=${sub.value}`);

                 if (response.ok) {
                     const datosPreguntas = await response.json();
                     if (Array.isArray(datosPreguntas)) {
                         cantidadReal = datosPreguntas.length;
                     } else if (datosPreguntas && typeof datosPreguntas === 'object') {
                         cantidadReal = datosPreguntas.total || datosPreguntas.cantidad || 0;
                     } else if (typeof datosPreguntas === 'number') {
                         cantidadReal = datosPreguntas;
                     }
                 }

                 sumaTotalReal += cantidadReal;

                 htmlSubcursosRows += `
                     <tr class="border-b border-slate-100 hover:bg-slate-50/30 transition-colors">
                         <td class="px-4 py-2.5 text-slate-600 font-medium">${sub.label}</td>
                         <td class="px-4 py-2.5 text-right font-semibold text-slate-800">${cantidadReal}</td>
                     </tr>
                 `;
             }
         } catch (error) {
             console.error("Error al consultar subcursos específicos:", error);
             htmlSubcursosRows = `
                 <tr>
                     <td colspan="2" class="px-4 py-8 text-center text-red-500 text-xs font-medium">
                         Error al conectar con el endpoint del banco de preguntas.
                     </td>
                 </tr>
             `;
         }
     } else {
         htmlSubcursosRows = `
             <tr>
                 <td colspan="2" class="px-4 py-8 text-center text-slate-400 text-xs italic">
                     No se han definido subcursos para el componente ${nombreComponente}.
                 </td>
             </tr>
         `;
     }

     panelDerecho.innerHTML = `
         <div class="bg-white rounded-xl shadow-sm border border-blue-200 overflow-hidden">
             <div class="p-4 bg-blue-50 border-b border-blue-100 flex justify-between items-center">
                 <div class="flex items-center space-x-2 text-blue-800 font-bold text-sm">
                     <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                         <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                     </svg>
                     <span>Detalle por subcurso: ${nombreComponente}</span>
                 </div>
                 <span class="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full font-bold uppercase">${codigoLimpio || 'COMP'}</span>
             </div>
             <div class="p-2">
                 <table class="w-full text-sm">
                     <tbody>
                         ${htmlSubcursosRows}
                         <tr class="bg-blue-50/40 font-bold text-blue-900 border-t border-blue-100">
                             <td class="px-4 py-3">Total Preguntas Real</td>
                             <td class="px-4 py-3 text-right text-base">${sumaTotalReal}</td>
                         </tr>
                     </tbody>
                 </table>
             </div>
         </div>
     `;
 }