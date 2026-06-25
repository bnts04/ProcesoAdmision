// Usamos el endpoint que ya conocemos y sabemos que funciona
const API_CONFIG_URL = 'http://localhost:8080/api/areas-examen/AREA_A/configuracion';
const API_BANCO_URL = 'http://localhost:8080/api/banco-preguntas';

// Guardamos los componentes de forma global para la interacción de la tabla
let componentesGlobal = [];

// Tu diccionario exacto de subcursos aceptados por el sistema
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
    { value: 'LITERATURA', label: 'Literatura' }, // <-- Agregada coma faltante
    { value: 'ECONOMIA', label: 'Economía' },     // <-- Agregada coma faltante
    { value: 'EDUCACION_CIVICA', label: 'Educación Cívica' }, // <-- Agregada coma faltante
    { value: 'PSICOLOGIA', label: 'Psicología' },   // <-- Agregada coma faltante
    { value: 'GEOGRAFIA', label: 'Geografía' }
  ],
  'RAZONAMIENTO_VERBAL': [
    { value: 'RAZONAMIENTO_VERBAL', label: 'Razonamiento Verbal' }
  ],
  'RAZONAMIENTO_MATEMATICO': [
    { value: 'RAZONAMIENTO_MATEMATICO', label: 'Razonamiento Matemático' }
  ]
};

document.addEventListener('DOMContentLoaded', () => {
    cargarDatosDashboard();
});

async function cargarDatosDashboard() {
    const kpiTotal = document.getElementById('kpi-total-count');
    const contenedorKpis = document.getElementById('contenedor-kpis-componentes');
    const tbodyComponentes = document.getElementById('tabla-resumen-componentes');

    try {
        const response = await fetch(API_CONFIG_URL);
        if (!response.ok) throw new Error(`Error: ${response.status}`);

        const data = await response.json();
        console.log("Datos recibidos para el Dashboard:", data);

        // 1. Mapeamos los componentes usando los campos conocidos desde la base de datos
        componentesGlobal = (data.resumenComponentes || []).map(c => {
            return {
                codigo: c.codigo || c.nombre?.substring(0, 3).toUpperCase() || "COMP",
                nombre: c.nombre || c.componente || c.nombreComponente,
                cantidad: c.preguntasDisponibles !== undefined ? c.preguntasDisponibles : (c.cantidadDisponible || c.enBanco || 0)
            };
        });

        // 2. Calcular el Gran Total sumando el banco de cada componente
        const totalBanco = componentesGlobal.reduce((sum, c) => sum + c.cantidad, 0);
        if (kpiTotal) kpiTotal.innerText = totalBanco;

        // 3. Renderizar las Tarjetas de los Componentes (Arriba) y las Filas de la Tabla (Abajo)
        let htmlKpis = '';
        let htmlTabla = '';

        componentesGlobal.forEach(comp => {
            htmlKpis += `
                <div class="bg-white p-5 rounded-xl shadow-sm border border-slate-200 transition-all hover:shadow-md">
                    <h3 class="text-blue-600 font-bold text-xs tracking-wider uppercase mb-3">${comp.codigo}</h3>
                    <div class="flex justify-between items-end">
                        <div>
                            <p class="text-3xl font-bold text-slate-800">${comp.cantidad}</p>
                            <p class="text-[11px] text-slate-400 truncate max-w-[120px]" title="${comp.nombre}">${comp.nombre}</p>
                        </div>
                        <div class="w-8 h-8 rounded-full bg-blue-50 flex items-center justify-center text-blue-600 shadow-sm">
                            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                                <path d="M9 4.804A7.968 7.968 0 005.5 4c-1.255 0-2.443.29-3.5.804v10A7.969 7.969 0 015.5 14c1.669 0 3.218.51 4.5 1.385A7.962 7.962 0 0114.5 14c1.255 0 2.443.29 3.5.804v-10A7.968 7.968 0 0014.5 4c-1.255 0-2.443.29-3.5.804V12a1 1 0 11-2 0V4.804z"></path>
                            </svg>
                        </div>
                    </div>
                </div>
            `;

            htmlTabla += `
                <tr class="bg-white border-b border-slate-100 hover:bg-slate-50/50 transition-colors">
                    <td class="px-6 py-4 font-medium text-slate-900">
                        <div class="font-semibold text-slate-700">${comp.nombre}</div>
                        <span class="text-[10px] bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded font-mono uppercase">${comp.codigo}</span>
                    </td>
                    <td class="px-6 py-4 text-center font-bold text-slate-600">${comp.cantidad}</td>
                    <td class="px-6 py-4 text-center">
                        <button onclick="mostrarDetalleSubcursos('${comp.nombre}')"
                                class="bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold px-3 py-1.5 rounded-lg transition-all shadow-sm active:scale-95">
                            Ver detalle
                        </button>
                    </td>
                </tr>
            `;
        });

        if (contenedorKpis) contenedorKpis.innerHTML = htmlKpis;
        if (tbodyComponentes) tbodyComponentes.innerHTML = htmlTabla;

        // Cargar por defecto el desglose del primer componente de la lista usando la distribución estática
        if (componentesGlobal.length > 0) {
            filtrarYMostrarSubcursos(componentesGlobal[0].nombre, componentesGlobal[0].codigo);
        }

    } catch (error) {
        console.error("Error en Dashboard:", error);
        if (tbodyComponentes) tbodyComponentes.innerHTML = `<tr><td colspan="3" class="text-center py-4 text-red-500 text-xs font-medium">Error al sincronizar con el banco de preguntas.</td></tr>`;
    }
}

// 4. Interacción al hacer clic en "Ver detalle"
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
             // El bucle 'for...of' irá consultando uno a uno de forma dinámica sin importar cuántos añadas
             for (const sub of listaSubcursosEstaticos) {
                 let cantidadReal = 0;

                 const response = await fetch(`${API_BANCO_URL}?subcurso=${sub.value}`);

                 if (response.ok) {
                     const datosPreguntas = await response.json();

                     if (Array.isArray(datosPreguntas)) {
                         amountReal = datosPreguntas.length;
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
         <div class="bg-white rounded-xl shadow-sm border border-blue-200 overflow-hidden animate-fadeIn">
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